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

package com.rebuild.server.configuration;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rebuild.server.Application;
import com.rebuild.server.business.robot.ActionContext;
import com.rebuild.server.business.robot.ActionFactory;
import com.rebuild.server.business.robot.TriggerAction;
import com.rebuild.server.business.robot.TriggerWhen;
import com.rebuild.server.metadata.MetadataHelper;
import com.rebuild.server.service.query.AdvFilterParser;

import cn.devezhao.persist4j.Entity;
import cn.devezhao.persist4j.engine.ID;

/**
 * TODO
 * 
 * @author devezhao zhaofang123@gmail.com
 * @since 2019/05/27
 */
public class RobotTriggerManager implements ConfigManager {

	public static final RobotTriggerManager instance = new RobotTriggerManager();
	private RobotTriggerManager() {}

	/**
	 * @param source
	 * @param when
	 * @return
	 */
	public TriggerAction[] getActions(ID source, TriggerWhen... when) {
		return filterActions(MetadataHelper.getEntity(source.getEntityCode()), source, when);
	}
	
	/**
	 * @param entity
	 * @param when
	 * @return
	 */
	public TriggerAction[] getActions(Entity entity, TriggerWhen... when) {
		return filterActions(entity, null, when);
	}
	
	/**
	 * @param source
	 * @param entity
	 * @param when
	 * @return
	 */
	private TriggerAction[] filterActions(Entity entity, ID source, TriggerWhen... when) {
		final List<ConfigEntry> entries = getConfig(entity);
		List<TriggerAction> actions = new ArrayList<>();
		for (ConfigEntry e : entries) {
			if (allowedWhen(e, when)) {
				ActionContext ctx = new ActionContext(entity, source, e.getJSON("actionContent"));
				TriggerAction o = null;
				if (source == null) {
					o = ActionFactory.createAction(e.getString("actionType"), ctx);
				} else if (allowedFilter(e, source)) {
					o = ActionFactory.createAction(e.getString("actionType"), ctx);
				}
				actions.add(o);
			}
		}
		return actions.toArray(new TriggerAction[actions.size()]);
	}
	
	/**
	 * 允许的动作
	 * 
	 * @param entry
	 * @param when
	 * @return
	 */
	private boolean allowedWhen(ConfigEntry entry, TriggerWhen... when) {
		if (when.length == 0) {
			return true;
		}
		int whenMask = entry.getInteger("when");
		for (TriggerWhen w : when) {
			if ((whenMask & w.getMaskValue()) != 0) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 过滤条件
	 * 
	 * @param entry
	 * @param record
	 * @return
	 */
	private boolean allowedFilter(ConfigEntry entry, ID record) {
		JSONObject whenFilter = (JSONObject) entry.getJSON("whenFilter");
		if (whenFilter == null || whenFilter.isEmpty()) {
			return true;
		}
		
		Entity entity = MetadataHelper.getEntity(record.getEntityCode());
		AdvFilterParser filterParser = new AdvFilterParser(whenFilter);
		String sqlWhere = filterParser.toSqlWhere();
		String sql = String.format(
				"select {0} from {1} where ({0} = ?) and ({2})",
				entity.getPrimaryField().getName(), entity.getName(), sqlWhere);
		Object matchs = Application.createQueryNoFilter(sql).setParameter(1, record).unique();
		return matchs != null;
	}
	
	/**
	 * @param entity
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected List<ConfigEntry> getConfig(Entity entity) {
		final String cKey = "RobotTriggerManager-" + entity.getName();
		Object cVal = Application.getCommonCache().getx(cKey);
		if (cVal != null) {
			return (List<ConfigEntry>) cVal;
		}
		
		Object[][] array = Application.createQueryNoFilter(
				"select when,whenFilter,actionType,actionContent from RobotTriggerConfig where belongEntity = ? and when > 0 order by priority desc")
				.setParameter(1, entity.getName())
				.array();
		
		ArrayList<ConfigEntry> entries = new ArrayList<ConfigEntry>();
		for (Object[] o : array) {
			ConfigEntry entry = new ConfigEntry()
					.set("when", o[0])
					.set("whenFilter", JSON.parseObject((String) o[1]))
					.set("actionType", o[2])
					.set("actionContent", JSON.parseObject((String) o[3]));
			entries.add(entry);
		}
		
		Application.getCommonCache().putx(cKey, entries);
		return entries;
	}
	
	/**
	 * @param cacheKey
	 */
	public void clean(Entity cacheKey) {
		final String cKey = "RobotTriggerManager-" + cacheKey.getName();
		Application.getCommonCache().evict(cKey);
	}
}