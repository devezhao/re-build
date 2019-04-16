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

package com.rebuild.server.helper.task;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rebuild.server.Application;
import com.rebuild.server.service.bizz.CurrentCaller;
import com.rebuild.web.OnlineSessionStore;

import cn.devezhao.commons.CalendarUtils;
import cn.devezhao.persist4j.engine.ID;

/**
 * 耗时操作可通过此类进行，例如大批量删除/修改等。此类提供了进度相关的约定，如总计执行条目，已完成条目/百分比。
 * 集成此类应该处理线程的 <code>isInterrupted</code> 方法，以便任务可以被终止 
 * 
 * @author devezhao
 * @since 09/29/2018
 */
public abstract class BulkTask implements Runnable {
	
	protected static final Log LOG = LogFactory.getLog(BulkTask.class);
	
	volatile private boolean interrupt = false;
	volatile private boolean interruptState = false;
	
	private int total = -1;
	private int complete = 0;
	
	private Date beginTime;
	private Date completedTime;
	
	private ID userInThread;
	
	/**
	 */
	protected BulkTask() {
		this.beginTime = CalendarUtils.now();
	}
	
	/**
	 * 设置当前线程用户
	 * 
	 * @param user
	 * @see CurrentCaller
	 * @see OnlineSessionStore
	 * @see #completedAfter()
	 */
	protected void setThreadUser(ID user) {
		this.userInThread = user;
		Application.getSessionStore().set(user);
	}
	
	/**
	 * @param total
	 */
	protected void setTotal(int total) {
		this.total = total;
	}
	
	/**
	 * @param complete
	 */
	protected void setComplete(int complete) {
		this.complete = complete;
	}
	
	/**
	 */
	protected void setCompleteOne() {
		this.complete++;
	}

	/**
	 * 子类应该在执行完毕后调用此方法。任何清空下，都应保证此方法被调用！
	 */
	protected void completedAfter() {
		this.completedTime = CalendarUtils.now();
		if (this.userInThread != null) {
			Application.getSessionStore().clean();
		}
	}

	/**
	 * 任务启动时间
	 * 
	 * @return
	 */
	protected Date getBeginTime() {
		return beginTime;
	}
	
	/**
	 * 任务完成时间
	 * 
	 * @return
	 */
	protected Date getCompletedTime() {
		return completedTime;
	}
	
	/**
	 * 总计执行条目
	 * 
	 * @return
	 */
	public int getTotal() {
		return total;
	}
	
	/**
	 * 已完成条目
	 * 
	 * @return
	 */
	public int getComplete() {
		return complete;
	}
	
	/**
	 * 完成率
	 * 
	 * @return
	 */
	public double getCompletePercent() {
		if (total == -1 || complete == 0) {
			return 0;
		}
		if (complete >= total) {
			return 1;
		}
		return complete * 1d / total;
	}
	
	/**
	 * 是否完成?
	 * 
	 * @return
	 */
	public boolean isCompleted() {
		return total != -1 && getComplete() >= getTotal();
	}
	
	/**
	 * 任务已耗时（ms）
	 * 
	 * @return
	 */
	public long getElapsedTime() {
		if (completedTime != null) {
			return completedTime.getTime() - beginTime.getTime();
		} else {
			return CalendarUtils.now().getTime() - beginTime.getTime();
		}
	}
	
	// -- for Thread
	
	public void interrupt() {
		this.interrupt = true;
	}
	protected boolean isInterrupt() {
		return interrupt;
	}
	
	protected void setInterrupted() {
		this.interruptState = true;
	}
	public boolean isInterrupted() {
		return interruptState;
	}
}
