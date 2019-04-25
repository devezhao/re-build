/*
rebuild - Building your system freely.
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

package com.rebuild.server.helper.cache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.rebuild.server.helper.ConfigItem;
import com.rebuild.server.helper.SystemConfig;
import com.rebuild.server.metadata.MetadataHelper;
import com.rebuild.server.portals.value.FieldValueWrapper;

import cn.devezhao.persist4j.engine.ID;

/**
 * 最近使用（搜索）的数据
 * 
 * @author devezhao zhaofang123@gmail.com
 * @since 2019/04/25
 */
public class RecentlySearchCache {
	
	final private CommonCache cacheManager;

	protected RecentlySearchCache(CommonCache commonCache) {
		this.cacheManager = commonCache;
	}
	
	/**
	 * 获取最近使用
	 * 
	 * @param user
	 * @param entity
	 * @param type
	 * @return
	 */
	public ID[] gets(ID user, String entity, String type) {
		return gets(user, entity, type, 5);
	}
	
	/**
	 * 获取最近使用
	 * 
	 * @param user
	 * @param entity
	 * @param type
	 * @param limit
	 * @return
	 */
	public ID[] gets(ID user, String entity, String type, int limit) {
		if (!isTurnOn()) {
			return ID.EMPTY_ID_ARRAY;
		}
		
		String key = formatKey(user, entity, type);
		@SuppressWarnings("unchecked")
		LinkedList<ID> exists = (LinkedList<ID>) cacheManager.getx(key);
		if (exists == null) {
			return ID.EMPTY_ID_ARRAY;
		}
		
		Set<ID> missed = new HashSet<>();
		List<ID> data = new ArrayList<>();
		for (int i = 0; i < limit && i < exists.size(); i++) {
			final ID raw = exists.get(i);
			try {
				String label = FieldValueWrapper.getLabel(raw);
				ID clone = ID.valueOf(raw.toLiteral());
				clone.setLabel(label);
				data.add(clone);
			} catch (NoRecordFoundException ex) {
				missed.add(raw);
			}
		}
		
		if (!missed.isEmpty()) {
			exists.removeAll(missed);
			cacheManager.putx(key, exists, Integer.MAX_VALUE);
		}
		
		return data.toArray(new ID[data.size()]);
	}
	
	/**
	 * 添加搜索缓存
	 * 
	 * @param user
	 * @param id
	 * @param type
	 */
	public void addOne(ID user, ID id, String type) {
		if (!isTurnOn()) {
			return;
		}
		
		String key = formatKey(user, MetadataHelper.getEntityName(id), type);
		@SuppressWarnings("unchecked")
		LinkedList<ID> exists = (LinkedList<ID>) cacheManager.getx(key);
		if (exists == null) {
			exists = new LinkedList<>();
		} else if (exists.contains(id)) {
			exists.remove(id);
		}
		
		if (exists.size() > 50) {
			exists.removeLast();
		}
		exists.addFirst(id);
		cacheManager.putx(key, exists, Integer.MAX_VALUE);
	}
	
	/**
	 * 清理缓存
	 * 
	 * @param user
	 * @param entity
	 * @param type
	 */
	public void clean(ID user, String entity, String type) {
		if (!isTurnOn()) {
			return;
		}
		
		String key = formatKey(user, entity, type);
		cacheManager.evict(key);
	}
	
	/**
	 * @param entity
	 * @param type
	 * @return
	 */
	private String formatKey(ID user, String entity, String type) {
		return String.format("RSR.%s-%s-%s", user, entity, StringUtils.defaultIfBlank(type, StringUtils.EMPTY));
	}
	
	/**
	 * 是否启用
	 * 
	 * @return
	 */
	private boolean isTurnOn() {
		return SystemConfig.getBool(ConfigItem.TurnRecentlySearch, true);
	}
}
