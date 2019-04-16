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

package com.rebuild.server.business.dataio;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.rebuild.server.Application;
import com.rebuild.server.helper.task.BulkTask;
import com.rebuild.server.metadata.EntityHelper;
import com.rebuild.server.metadata.ExtRecordCreator;
import com.rebuild.server.metadata.entityhub.DisplayType;
import com.rebuild.server.metadata.entityhub.EasyMeta;
import com.rebuild.server.portals.ClassificationManager;
import com.rebuild.server.portals.PickListManager;
import com.rebuild.utils.JSONUtils;

import cn.devezhao.commons.RegexUtils;
import cn.devezhao.commons.excel.Cell;
import cn.devezhao.commons.excel.ExcelReader;
import cn.devezhao.persist4j.Entity;
import cn.devezhao.persist4j.Field;
import cn.devezhao.persist4j.Query;
import cn.devezhao.persist4j.Record;
import cn.devezhao.persist4j.engine.ID;

/**
 * 数据导入
 * 
 * @author devezhao
 * @since 01/09/2019
 * 
 * @see DisplayType
 */
public class DataImporter extends BulkTask {
	
	final private ImportEnter enter;
	
	final private ID owningUser;
	
	private int success = 0;
	private Map<Integer, Object> logging = new LinkedHashMap<>();
	
	/**
	 * @param enter
	 */
	public DataImporter(ImportEnter enter) {
		this(enter, Application.getCurrentUser());
	}
	
	/**
	 * @param enter
	 * @param user
	 */
	public DataImporter(ImportEnter enter, ID user) {
		this.enter = enter;
		this.owningUser = enter.getDefaultOwningUser() == null ? user : enter.getDefaultOwningUser();
	}
	
	/**
	 * @return
	 */
	public ImportEnter getImportEnter() {
		return enter;
	}
	
	@Override
	public void run() {
		DataFileParser fileParser = null;
		try {
			fileParser = new DataFileParser(enter.getSourceFile());
			setTotal(fileParser.getRowsCount() - 1);
			
			ExcelReader reader = fileParser.getExcelReader();
			reader.next();  // Remove head row
			
			setThreadUser(this.owningUser);
			while (reader.hasNext()) {
				if (isInterrupt()) {
					this.setInterrupted();
					break;
				}
				
				try {
					Cell[] cell = reader.next();
					if (cell == null) {  // Last row is null ? (Only .xlsx)
						continue;
					}
					
					Record record = checkoutRecord(cell);
					if (record != null) {
						record = Application.getEntityService(enter.getToEntity().getEntityCode()).createOrUpdate(record);
						this.success++;
						logging.put(reader.getRowIndex(), record.getPrimary());
					}
				} catch (Exception ex) {
					logging.put(reader.getRowIndex(), ex.getLocalizedMessage());
					LOG.warn(reader.getRowIndex() + " > " + ex);
				} finally {
					this.setCompleteOne();
				}
			}
		} finally {
			IOUtils.closeQuietly(fileParser);
			completedAfter();
		}
	}
	
	/**
	 * @return
	 */
	public int getSuccess() {
		return success;
	}

	/**
	 * @return
	 */
	public Map<Integer, Object> getLogging() {
		return logging;
	}
	
	/**
	 * @param cells
	 * @return
	 */
	protected Record checkoutRecord(Cell cells[]) {
		Record recordNew = EntityHelper.forNew(enter.getToEntity().getEntityCode(), this.owningUser);
		
		for (Map.Entry<Field, Integer> e : enter.getFiledsMapping().entrySet()) {
			int cellIndex = e.getValue();
			if (cells.length > cellIndex) {
				Field field = e.getKey();
				Object value = checkoutFieldValue(field, cells[cellIndex], true);
				if (value != null) {
					recordNew.setObjectValue(field.getName(), value);
				}
			}
		}
		
		Record record = recordNew;
		
		// 检查重复
		if (enter.getRepeatOpt() < ImportEnter.REPEAT_OPT_IGNORE) {
			final ID repeat = getRepeatedRecordId(enter.getRepeatFields(), recordNew);
			
			if (repeat != null && enter.getRepeatOpt() == ImportEnter.REPEAT_OPT_SKIP) {
				return null;
			}
			
			if (repeat != null && enter.getRepeatOpt() == ImportEnter.REPEAT_OPT_UPDATE) {
				record = EntityHelper.forUpdate(repeat, this.owningUser);
				for (Iterator<String> iter = recordNew.getAvailableFieldIterator(); iter.hasNext(); ) {
					String field = iter.next();
					if (EntityHelper.OwningUser.equals(field) || EntityHelper.OwningDept.equals(field)
							|| EntityHelper.CreatedBy.equals(field) || EntityHelper.CreatedOn.equals(field)
							|| EntityHelper.ModifiedBy.equals(field) || EntityHelper.ModifiedOn.equals(field)) {
						continue;
					}
					record.setObjectValue(field, recordNew.getObjectValue(field));
				}
			}
		}
	
		// Verify new record
		if (record.getPrimary() == null) {
			ExtRecordCreator verifier = new ExtRecordCreator(enter.getToEntity(), JSONUtils.EMPTY_OBJECT, null);
			verifier.verify(recordNew, true);
		}
		return record;
	}
	
	/**
	 * @param field
	 * @param cell
	 * @param validate
	 * @return
	 */
	protected Object checkoutFieldValue(Field field, Cell cell, boolean validate) {
		final DisplayType dt = EasyMeta.getDisplayType(field);
		if (dt == DisplayType.NUMBER) {
			return cell.asLong();
		} else if (dt == DisplayType.DECIMAL) {
			return cell.asDouble();
		} else if (dt == DisplayType.DATE || dt == DisplayType.DATETIME) {
			return checkoutDateValue(field, cell);
		} else if (dt == DisplayType.PICKLIST) {
			return checkoutPickListValue(field, cell);
		} else if (dt == DisplayType.CLASSIFICATION) {
			return checkoutClassificationValue(field, cell);
		} else if (dt == DisplayType.REFERENCE) {
			return checkoutReferenceValue(field, cell);
		} else if (dt == DisplayType.BOOL) {
			return cell.asBool();
		}
		
		// 格式验证
		if (validate) {
			if (dt == DisplayType.EMAIL) {
				String email = cell.asString();
				return RegexUtils.isEMail(email) ? email : null;
			} else if (dt == DisplayType.URL) {
				String url = cell.asString();
				return RegexUtils.isUrl(url) ? url : null;
			} else if (dt == DisplayType.PHONE) {
				String tel = cell.asString();
				return RegexUtils.isCNMobile(tel) || RegexUtils.isTel(tel)? tel : null;
			}
		}
		return cell.asString();
	}
	
	/**
	 * @param field
	 * @param cell
	 * @return
	 */
	private ID checkoutPickListValue(Field field, Cell cell) {
		String val = cell.asString();
		if (StringUtils.isBlank(val)) {
			return null;
		}
		
		// 支持ID
		if (ID.isId(val) && ID.valueOf(val).getEntityCode() == EntityHelper.PickList) {
			ID iid = ID.valueOf(val);
			if (PickListManager.getLabel(iid) != null) {
				return iid;
			} else {
				LOG.warn("No item of PickList found by ID : " + iid);
				return null;
			}
		} else {
			return PickListManager.findItemByLabel(val, field);
		}
	}
	
	/**
	 * @param field
	 * @param cell
	 * @return
	 */
	private ID checkoutClassificationValue(Field field, Cell cell) {
		String val = cell.asString();
		if (StringUtils.isBlank(val)) {
			return null;
		}
		
		// 支持ID
		if (ID.isId(val) && ID.valueOf(val).getEntityCode() == EntityHelper.ClassificationData) {
			ID iid = ID.valueOf(val);
			if (ClassificationManager.getName(iid) != null) {
				return iid;
			} else {
				LOG.warn("No item of Classification found by ID : " + iid);
				return null;
			}
		} else {
			return ClassificationManager.findItemByName(val, field);
		}
	}
	
	/**
	 * @param field
	 * @param cell
	 * @return
	 */
	private ID checkoutReferenceValue(Field field, Cell cell) {
		String val = cell.asString();
		if (StringUtils.isBlank(val)) {
			return null;
		}
		
		Entity oEntity = field.getReferenceEntity();
		
		// 支持ID导入
		if (ID.isId(val) && ID.valueOf(val).getEntityCode().intValue() == oEntity.getEntityCode()) {
			return ID.valueOf(val);
		}
		
		Object textVal = checkoutFieldValue(oEntity.getNameField(), cell, false);
		if (textVal == null) {
			return null;
		}
		
		String sql = null;
		if (oEntity.getEntityCode() == EntityHelper.User) {
			sql = String.format("select userId from User where loginName = '%s' or email = '%s'", textVal, textVal);
		} else {
			sql = String.format("select %s from %s where %s = '%s'",
					oEntity.getPrimaryField().getName(), oEntity.getName(), oEntity.getNameField().getName());
		}
		Object[] found = Application.createQueryNoFilter(sql).setParameter(1, textVal).unique();
		return found == null ? null : (ID) found[0];
	}
	
	/**
	 * @param field
	 * @param cell
	 * @return
	 */
	private Date checkoutDateValue(Field field, Cell cell) {
		Date date = cell.asDate();
		if (date != null) {
			return date;
		}
		if (cell.isEmpty()) {
			return null;
		}
		
		String date2str = cell.asString();
		// 2017/11/19 11:07
		if (date2str.contains("/")) {
			return cell.asDate(new String[] { "yyyy/M/d H:m:s", "yyyy/M/d H:m", "yyyy/M/d" });
		}
		return null;
	}
	
	/**
	 * @param repeatFields
	 * @param data
	 * @return
	 */
	protected ID getRepeatedRecordId(Field[] repeatFields, Record data) {
		Map<String, Object> wheres = new HashMap<>();
		for (Field c : repeatFields) {
			String cName = c.getName();
			if (data.hasValue(cName)) {
				wheres.put(cName, data.getObjectValue(cName));
			}
		}
		
		LOG.info("Checking repeated : " + wheres);
		if (wheres.isEmpty()) {
			return null;
		}
		
		Entity entity = data.getEntity();
		String sql = String.format("select %s from %s where (1=1)", 
				entity.getPrimaryField().getName(), entity.getName());
		for (String c : wheres.keySet()) {
			sql += " and " + c + " = :" + c;
		}
		
		Query query = Application.createQueryNoFilter(sql);
		for (Map.Entry<String, Object> e : wheres.entrySet()) {
			query.setParameter(e.getKey(), e.getValue());
		}
		
		Object[] exists = query.unique();
		return exists == null ? null : (ID) exists[0];
	}
}
