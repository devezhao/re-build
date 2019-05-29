/*
rebuild - Building your business-systems freely.
Copyright (C) 2019 devezhao <zhaofang123@gmail.com>

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

package com.rebuild.server.business.robot.triggers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rebuild.server.Application;
import com.rebuild.server.business.robot.ActionContext;
import com.rebuild.server.business.robot.ActionType;
import com.rebuild.server.business.robot.TriggerAction;
import com.rebuild.server.business.robot.TriggerException;
import com.rebuild.server.metadata.EntityHelper;
import com.rebuild.server.metadata.MetadataHelper;
import com.rebuild.server.metadata.MetadataSorter;
import com.rebuild.server.metadata.entityhub.DisplayType;
import com.rebuild.server.metadata.entityhub.EasyMeta;
import com.rebuild.server.service.OperatingContext;
import com.rebuild.server.service.bizz.UserService;

import cn.devezhao.commons.ObjectUtils;
import cn.devezhao.persist4j.Entity;
import cn.devezhao.persist4j.Field;
import cn.devezhao.persist4j.Record;
import cn.devezhao.persist4j.engine.ID;

/**
 * @author devezhao zhaofang123@gmail.com
 * @since 2019/05/29
 */
public class FieldAggregation implements TriggerAction {

	final private ActionContext context;
	
	private Entity sourceEntity;
	private Entity targetEntity;
	
	private String masterReffield;
	private ID masterRecordId;
	
	public FieldAggregation(ActionContext context) {
		this.context = context;
	}
	
	@Override
	public ActionType getType() {
		return ActionType.FIELDAGGREGATION;
	}
	
	@Override
	public boolean isUsableSourceEntity(int entityCode) {
		return true;
	}
	
	@Override
	public void execute(OperatingContext operatingContext) throws TriggerException {
		this.prepare(operatingContext);
		if (this.masterRecordId == null) {
			return;
		}
		
		// 更新目标
		Record targetRecord = EntityHelper.forUpdate(masterRecordId, UserService.SYSTEM_USER, false);
		
		JSONArray items = ((JSONObject) context.getActionContent()).getJSONArray("items");
		for (Object o : items) {
			JSONObject item = (JSONObject) o;
			String sourceField = item.getString("sourceField");
			String targetField = item.getString("targetField");
			if (!sourceEntity.containsField(sourceField)) {
				LOG.warn("Unknow field '" + sourceField + "' in '" + sourceEntity.getName() + "'");
				continue;
			}
			if (!targetEntity.containsField(targetField)) {
				LOG.warn("Unknow field '" + targetField + "' in '" + targetEntity.getName() + "'");
				continue;
			}
			
			// 直接利用SQL计算结果
			String calcMode = item.getString("calcMode");
			String calcField = "COUNT".equalsIgnoreCase(calcMode) ? sourceEntity.getPrimaryField().getName() : sourceField;
			
			String sql = String.format("select %s(%s) from %s where %s = ?", 
					calcMode, calcField, sourceEntity.getName(), masterReffield);
			Object[] result = Application.createQueryNoFilter(sql).setParameter(1, masterRecordId).unique();
			Double calcValue = result == null || result[0] == null ? 0d : ObjectUtils.toDouble(result[0]);
			
			DisplayType dt = EasyMeta.getDisplayType(targetEntity.getField(targetField));
			if (dt == DisplayType.NUMBER) {
				targetRecord.setInt(targetField, calcValue.intValue());
			} else if (dt == DisplayType.DECIMAL) {
				targetRecord.setDouble(targetField, calcValue.doubleValue());
			}
		}
		
		if (targetRecord.getAvailableFieldIterator().hasNext()) {
			// TODO 触发器更新的数据不传播
			Application.getCommonService().update(targetRecord, false);
		}
	}
	
	@Override
	public void prepare(OperatingContext operatingContext) throws TriggerException {
		if (sourceEntity != null) {
			return;
		}
		
		String targetEntity2str = ((JSONObject) context.getActionContent()).getString("targetEntity");
		if (!MetadataHelper.containsEntity(targetEntity2str)) {
			return;
		}
		this.sourceEntity = context.getSourceEntity();
		this.targetEntity = MetadataHelper.getEntity(targetEntity2str);
		
		// 找到主纪录
		
		for (Field field : MetadataSorter.sortFields(sourceEntity, DisplayType.REFERENCE)) {
			if (field.getReferenceEntity().equals(targetEntity)) {
				this.masterReffield = field.getName();
				break;
			}
		}
		
		if (this.masterReffield == null || context.getSourceRecord() == null) {
			return;
		}
		
		String sql = String.format("select %s from %s where %s = ?",
				masterReffield, sourceEntity.getName(), sourceEntity.getPrimaryField().getName());
		Object refid[] = Application.createQueryNoFilter(sql).setParameter(1, context.getSourceRecord()).unique();
		if (refid != null && refid[0] != null) {
			this.masterRecordId = (ID) refid[0];
		}
	}
}