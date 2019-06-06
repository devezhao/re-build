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

package com.rebuild.server.service.configuration;

import com.rebuild.server.Application;
import com.rebuild.server.configuration.portals.DashboardManager;
import com.rebuild.server.metadata.EntityHelper;
import com.rebuild.server.service.DataSpecificationException;

import cn.devezhao.persist4j.PersistManagerFactory;
import cn.devezhao.persist4j.Record;
import cn.devezhao.persist4j.engine.ID;

/**
 * @author devezhao-mbp zhaofang123@gmail.com
 * @since 2019/06/04
 */
public class DashboardConfigService extends CleanableCacheService {

	protected DashboardConfigService(PersistManagerFactory aPMFactory) {
		super(aPMFactory);
	}
	
	@Override
	public int getEntityCode() {
		return EntityHelper.DashboardConfig;
	}
	
	@Override
	public int delete(ID recordId) {
		ID user = Application.getCurrentUser();
		if (!DashboardManager.instance.isEditable(user, recordId)) {
			throw new DataSpecificationException("无权删除他人的仪表盘");
		}
		return super.delete(recordId);
	}
	
	@Override
	public Record update(Record record) {
		ID user = Application.getCurrentUser();
		if (!DashboardManager.instance.isEditable(user, record.getPrimary())) {
			throw new DataSpecificationException("无权修改他人的仪表盘");
		}
		return super.update(record);
	}
	
	@Override
	protected void cleanCache(ID configId) {
		DashboardManager.instance.clean(configId);
	}
}