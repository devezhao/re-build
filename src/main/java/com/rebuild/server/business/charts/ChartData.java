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

package com.rebuild.server.business.charts;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rebuild.server.Application;
import com.rebuild.server.metadata.EntityHelper;
import com.rebuild.server.metadata.MetadataHelper;
import com.rebuild.server.metadata.entityhub.DisplayType;
import com.rebuild.server.metadata.entityhub.EasyMeta;
import com.rebuild.server.portals.value.FieldValueWrapper;
import com.rebuild.server.service.bizz.UserHelper;
import com.rebuild.server.service.bizz.UserService;
import com.rebuild.server.service.query.AdvFilterParser;

import cn.devezhao.commons.ObjectUtils;
import cn.devezhao.persist4j.Entity;
import cn.devezhao.persist4j.Query;
import cn.devezhao.persist4j.engine.ID;

/**
 * 图表数据
 * 
 * @author devezhao
 * @since 12/14/2018
 */
public abstract class ChartData {
	
	protected final JSONObject config;
	protected final ID user;
	
	private boolean fromPreview = false;
	
	/**
	 * @param config
	 */
	protected ChartData(JSONObject config) {
		this(config, Application.getCurrentUser());
	}
	
	/**
	 * @param config
	 * @param user
	 */
	protected ChartData(JSONObject config, ID user) {
		this.config = config;
		this.user = user;
	}
	
	protected boolean isFromPreview() {
		return fromPreview;
	}
	
	/**
	 * 源实体
	 * 
	 * @return
	 */
	public Entity getSourceEntity() {
		String e = config.getString("entity");
		return MetadataHelper.getEntity(e);
	}
	
	/**
	 * 标题
	 * 
	 * @return
	 */
	public String getTitle() {
		return StringUtils.defaultIfBlank(config.getString("title"), "未命名图表");
	}
	
	/**
	 * 维度轴
	 * 
	 * @return
	 */
	public Dimension[] getDimensions() {
		JSONObject axis = config.getJSONObject("axis");
		JSONArray items = axis.getJSONArray("dimension");
		if (items == null || items.isEmpty()) {
			return new Dimension[0];
		}
		
		List<Dimension> list = new ArrayList<>();
		for (Object o : items) {
			JSONObject item = (JSONObject) o;
			String field = item.getString("field");
			if (!getSourceEntity().containsField(field)) {
				throw new ChartsException("字段 [" + field.toUpperCase() + " ] 已被删除");
			}
			
			FormatSort sort = FormatSort.NONE;
			FormatCalc calc = FormatCalc.NONE;
			if (StringUtils.isNotBlank(item.getString("sort"))) {
				sort = FormatSort.valueOf(item.getString("sort"));
			}
			if (StringUtils.isNotBlank(item.getString("calc"))) {
				calc = FormatCalc.valueOf(item.getString("calc"));
			}
			Dimension dim = new Dimension(getSourceEntity().getField(field), sort, calc, item.getString("label"));
			list.add(dim);
		}
		return list.toArray(new Dimension[list.size()]);
	}
	
	/**
	 * 数值轴
	 * 
	 * @return
	 */
	public Numerical[] getNumericals() {
		JSONObject axis = config.getJSONObject("axis");
		JSONArray items = axis.getJSONArray("numerical");
		if (items == null || items.isEmpty()) {
			return new Numerical[0];
		}
		
		List<Numerical> list = new ArrayList<>();
		for (Object o : items) {
			JSONObject item = (JSONObject) o;
			String field = item.getString("field");
			if (!getSourceEntity().containsField(field)) {
				throw new ChartsException("字段 [" + field.toUpperCase() + " ] 已被删除");
			}
			
			FormatSort sort = FormatSort.NONE;
			FormatCalc calc = FormatCalc.NONE;
			if (StringUtils.isNotBlank(item.getString("sort"))) {
				sort = FormatSort.valueOf(item.getString("sort"));
			}
			if (StringUtils.isNotBlank(item.getString("calc"))) {
				calc = FormatCalc.valueOf(item.getString("calc"));
			}
			
			Numerical num = new Numerical(getSourceEntity().getField(field), sort, calc, item.getString("label"), item.getInteger("scale"));
			list.add(num);
		}
		return list.toArray(new Numerical[list.size()]);
	}
	
	/**
	 * 获取过滤 SQL
	 * 
	 * @return
	 */
	protected String getFilterSql() {
		String previewFilter = StringUtils.EMPTY;
		// 限制预览数据量
		if (isFromPreview() && getSourceEntity().containsField(EntityHelper.AutoId)) {
			
			String maxAidSql = String.format("select max(%s) from %s", EntityHelper.AutoId, getSourceEntity().getName());
			Object[] o = Application.createQueryNoFilter(maxAidSql).unique();
			long maxAid = ObjectUtils.toLong(o[0]);
			if (maxAid > 5000) {
				previewFilter = String.format("(%s >= %d) and ", EntityHelper.AutoId, Math.max(maxAid - 2000, 0));
			}
		}
		
		JSONObject filterExp = config.getJSONObject("filter");
		if (filterExp == null) {
			return previewFilter + "(1=1)";
		}
		
		AdvFilterParser filterParser = new AdvFilterParser(filterExp);
		String sqlWhere = filterParser.toSqlWhere();
		if (sqlWhere != null) {
			sqlWhere = previewFilter + sqlWhere;
		}
		return StringUtils.defaultIfBlank(sqlWhere, "(1=1)");
	}
	
	/**
	 * 获取排序 SQL
	 * 
	 * @return
	 */
	protected String getSortSql() {
		Set<String> sorts = new HashSet<>();
		for (Numerical num : getNumericals()) {
			FormatSort fs = num.getFormatSort();
			if (fs != FormatSort.NONE) {
				sorts.add(num.getSqlName() + " " + fs.toString().toLowerCase());
			}
		}
		// 优先数值排序
		if (!sorts.isEmpty()) {
			return String.join(", ", sorts);
		}
		
		for (Axis dim : getDimensions()) {
			FormatSort fs = dim.getFormatSort();
			if (fs != FormatSort.NONE) {
				sorts.add(dim.getSqlName() + " " + fs.toString().toLowerCase());
			}
		}
		return sorts.isEmpty() ? null : String.join(", ", sorts);
	}
	
	/**
	 * @param axis
	 * @param value
	 * @return
	 */
	protected String warpAxisValue(Axis axis, Object value) {
		return axis instanceof Numerical ? warpAxisValue((Numerical) axis, value)
				: warpAxisValue((Dimension) axis, value);  
	}
	
	/**
	 * 格式化数值
	 * 
	 * @param axis
	 * @param value
	 * @return
	 */
	protected String warpAxisValue(Numerical axis, Object value) {
		if (value == null) {
			return "0";
		}
		
		String format = "###";
		if (axis.getScale() > 0) {
			format = "##0.";
			format = StringUtils.rightPad(format, format.length() + axis.getScale(), "0");
		}
		
		if (ID.isId(value)) {
			value = 1;
		}
		return new DecimalFormat(format).format(value);
	}
	
	/**
	 * 获取纬度标签
	 * 
	 * @param axis
	 * @param value
	 * @return
	 */
	protected String warpAxisValue(Dimension axis, Object value) {
		if (value == null) {
			return "无";
		}
		
		EasyMeta axisField = EasyMeta.valueOf(axis.getField());
		DisplayType axisType = axisField.getDisplayType();
		
		String label = null;
		if (axisType == DisplayType.REFERENCE) {
			label = FieldValueWrapper.getLabel((ID) value);
		} else if (axisType == DisplayType.BOOL 
				|| axisType == DisplayType.PICKLIST 
				|| axisType == DisplayType.CLASSIFICATION) {
			label = (String) FieldValueWrapper.wrapFieldValue(value, axisField);
		} else {
			label = value.toString();
		}
		return label;
	}
	
	/**
	 * 构建数据
	 * 
	 * @param fromPreview
	 * @return
	 */
	public JSON build(boolean fromPreview) {
		this.fromPreview = fromPreview;
		try {
			return this.build();
		} finally {
			this.fromPreview = false;
		}
	}
	
	/**
	 * 创建查询。会自动处理权限选项
	 * 
	 * @param sql
	 * @return
	 */
	protected Query createQuery(String sql) {
		if (this.fromPreview) {
			return Application.createQuery(sql, user);
		}
		
		boolean noPrivileges = false;
		JSONObject option = config.getJSONObject("option");
		if (option != null) {
			noPrivileges = option.getBooleanValue("noPrivileges");
		}
		String co = config.getString("chartOwning");
		ID chartOwning = ID.isId(co) ? ID.valueOf(co) : null;
		
		if (chartOwning == null || !noPrivileges) {
			return Application.createQuery(sql, user);
		}
		return Application.createQuery(sql, UserHelper.isAdmin(chartOwning) ? UserService.SYSTEM_USER : user);
	}
	
	/**
	 * 构建数据
	 * 
	 * @return
	 */
	abstract public JSON build();
}
