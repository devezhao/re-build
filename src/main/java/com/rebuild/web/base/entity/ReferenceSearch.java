/*
rebuild - Building your business-systems freely.
Copyright (C) 2018 devezhao <zhaofang123@gmail.com>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package com.rebuild.web.base.entity;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.rebuild.server.Application;
import com.rebuild.server.metadata.EntityHelper;
import com.rebuild.server.metadata.MetadataHelper;
import com.rebuild.server.portals.value.FieldValueWrapper;
import com.rebuild.server.service.bizz.UserHelper;
import com.rebuild.server.service.bizz.UserService;
import com.rebuild.utils.JSONUtils;
import com.rebuild.web.BaseControll;

import cn.devezhao.persist4j.Entity;
import cn.devezhao.persist4j.Field;
import cn.devezhao.persist4j.dialect.FieldType;
import cn.devezhao.persist4j.engine.ID;

/**
 * 引用字段搜索
 * 
 * @author zhaofang123@gmail.com
 * @since 08/24/2018
 * 
 * @see ClassificationSearch
 */
@Controller
@RequestMapping("/commons/search/")
public class ReferenceSearch extends BaseControll {
	
	// 指定字段搜索
	@RequestMapping("search")
	public void search(HttpServletRequest request, HttpServletResponse response) throws IOException {
		final ID user = getRequestUser(request);
		final String entity = getParameterNotNull(request, "entity");
		
		String q = getParameter(request, "q");
		// 为空则加载最近使用的
		if (StringUtils.isBlank(q)) {
			String type = getParameter(request, "type");
			ID[] recently = Application.getRecentlySearchCache().gets(user, entity, type);
			if (recently.length == 0) {
				writeSuccess(response, JSONUtils.EMPTY_ARRAY);
			} else {
				writeSuccess(response, 
						RecentlySearchControll.formatSelect2(recently, true));
			}
			return;
		}
		q = StringEscapeUtils.escapeSql(q);
		
		Entity metaEntity = MetadataHelper.getEntity(entity);
		Field nameField = MetadataHelper.getNameField(metaEntity);
		if (nameField == null) {
			LOG.warn("No name-field found : " + entity);
			writeSuccess(response, ArrayUtils.EMPTY_STRING_ARRAY);
			return;
		}
		
		// 查询字段，未指定则使用名称字段和 quickCode
		String qfields = getParameter(request, "qfields");
		if (StringUtils.isBlank(qfields)) {
			qfields = nameField.getName();
			if (metaEntity.containsField(EntityHelper.QuickCode)) {
				qfields += "," + EntityHelper.QuickCode;
			}
		}
		
		List<String> or = new ArrayList<>();
		for (String field : qfields.split(",")) {
			if (!metaEntity.containsField(field)) {
				LOG.warn("No field for search : " + field);
			} else {
				or.add(field + " like '%" + q + "%'");
			}
		}
		if (or.isEmpty()) {
			writeSuccess(response, ArrayUtils.EMPTY_STRING_ARRAY);
			return;
		}
		
		String sql = "select {0},{1} from {2} where ({3})";
		sql = MessageFormat.format(sql, 
				metaEntity.getPrimaryField().getName(), nameField.getName(), metaEntity.getName(), StringUtils.join(or, " or "));
		if (metaEntity.containsField(EntityHelper.ModifiedOn)) {
			sql += " order by modifiedOn desc";
		}
		
		List<Object> result = searchResult(metaEntity, sql);
		writeSuccess(response, result);
	}
	
	// 搜索引用字段
	@RequestMapping({ "reference", "quick" })
	public void referenceSearch(HttpServletRequest request, HttpServletResponse response) throws IOException {
		final ID user = getRequestUser(request);
		final String entity = getParameterNotNull(request, "entity");
		final String field = getParameterNotNull(request, "field");
		
		Entity metaEntity = MetadataHelper.getEntity(entity);
		Field referenceField = metaEntity.getField(field);
		if (referenceField.getType() != FieldType.REFERENCE) {
			writeSuccess(response, JSONUtils.EMPTY_ARRAY);
			return;
		}
		
		Entity referenceEntity = referenceField.getReferenceEntity();
		Field referenceNameField = MetadataHelper.getNameField(referenceEntity);
		if (referenceNameField == null) {
			LOG.warn("No name-field found : " + referenceEntity.getName());
			writeSuccess(response, JSONUtils.EMPTY_ARRAY);
			return;
		}
		
		String q = getParameter(request, "q");
		// 为空则加载最近使用的
		if (StringUtils.isBlank(q)) {
			String type = getParameter(request, "type");
			ID[] recently = Application.getRecentlySearchCache().gets(user, referenceEntity.getName(), type);
			if (recently.length == 0) {
				writeSuccess(response, JSONUtils.EMPTY_ARRAY);
			} else {
				writeSuccess(response, 
						RecentlySearchControll.formatSelect2(recently, true));
			}
			return;
		}
		q = StringEscapeUtils.escapeSql(q);
		
		String sql = "select {0},{1} from {2} where ( {1} like ''%{3}%''";
		sql = MessageFormat.format(sql, 
				referenceEntity.getPrimaryField().getName(), referenceNameField.getName(), referenceEntity.getName(), q);
		if (referenceEntity.containsField(EntityHelper.QuickCode) && StringUtils.isAlphanumericSpace(q)) {
			sql += MessageFormat.format(" or quickCode like ''%{0}%'' )", q);
		} else {
			sql += " )";
		}
		if (referenceEntity.containsField(EntityHelper.ModifiedOn)) {
			sql += " order by modifiedOn desc";
		}
		
		List<Object> result = searchResult(metaEntity, sql);
		writeSuccess(response, result);
	}
	
	// 获取记录的名称字段值
	@RequestMapping("read-labels")
	public void referenceLabel(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String ids = getParameter(request, "ids", null);
		if (ids == null) {
			writeSuccess(response);
			return;
		}
		
		Map<String, String> labels = new HashMap<>();
		for (String id : ids.split("\\|")) {
			if (!ID.isId(id)) {
				continue;
			}
			String label = FieldValueWrapper.getLabel(ID.valueOf(id));
			if (label != null) {
				labels.put(id, label);
			}
		}
		writeSuccess(response, labels);
	}
	
	/**
	 * 封装查询结果
	 * 
	 * @param entity
	 * @param sql
	 * @return
	 */
	private List<Object> searchResult(Entity entity, String sql) {
		Object[][] array = Application.createQuery(sql).setLimit(10).array();
		List<Object> result = new ArrayList<>();
		for (Object[] o : array) {
			ID recordId = (ID) o[0];
			if (MetadataHelper.isBizzEntity(entity.getEntityCode())) {
				if (!UserHelper.isActive(recordId) || recordId.equals(UserService.SYSTEM_USER)) {
					continue;
				}
			}
			
			Map<String, Object> map = new HashMap<>();
			map.put("id", recordId);
			String text = o[1] == null ? recordId.toLiteral().toUpperCase() : o[1].toString();
			if (StringUtils.isBlank(text)) {
				text = recordId.toLiteral().toUpperCase();
			}
			map.put("text", text);
			result.add(map);
		}
		return result;
	}
}
