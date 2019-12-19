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

package com.rebuild.server.service.notification;

import cn.devezhao.persist4j.engine.ID;
import com.rebuild.server.Application;
import com.rebuild.server.configuration.portals.FieldValueWrapper;
import com.rebuild.server.metadata.EntityHelper;
import com.rebuild.utils.AppUtils;
import com.rebuild.utils.MarkdownUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 消息构建
 *
 * @author devezhao zhaofang123@gmail.com
 * @since 2019/07/12
 */
public class MessageBuilder {
	
	/**
	 * @param toUser
	 * @param message
	 * @return
	 */
	public static Message createMessage(ID toUser, String message) {
		return new Message(null, toUser, message, null, Message.TYPE_DEFAULT);
	}

	/**
	 * @param toUser
	 * @param message
	 * @param recordId
	 * @return
	 */
	public static Message createMessage(ID toUser, String message, ID recordId) {
		return new Message(null, toUser, message, recordId, Message.TYPE_DEFAULT);
	}

    /**
	 * @param fromUser
	 * @param toUser
	 * @param message
	 * @param type
	 * @return
	 */
	public static Message createMessage(ID fromUser, ID toUser, String message, int type) {
		return new Message(fromUser, toUser, message, null, type);
	}

	/**
	 * @param toUser
	 * @param message
	 * @return
	 */
	public static Message createApproval(ID toUser, String message) {
		return new Message(null, toUser, message, null, Message.TYPE_APPROVAL);
	}

	/**
	 * @param fromUser
	 * @param toUser
	 * @param message
	 * @param recordId
	 * @return
	 */
	public static Message createApproval(ID fromUser, ID toUser, String message, ID recordId) {
		return new Message(fromUser, toUser, message, recordId, Message.TYPE_APPROVAL);
	}

	// --

    /**
     * Matchs @ID
     */
    public static final Pattern AT_PATTERN = Pattern.compile("(\\@[0-9a-z\\-]{20})");

    /**
     * 格式化消息
     *
     * @param message
     * @return
     */
	public static String formatMessage(String message) {
	   return formatMessage(message, true);
    }

    /**
     * 格式化消息，支持转换 MD 语法
     *
     * @param message
     * @param md2html
     * @return
     * @see MarkdownUtils
     */
	public static String formatMessage(String message, boolean md2html) {
		// 匹配 `@ID`
		Matcher atMatcher = AT_PATTERN.matcher(message);
		while (atMatcher.find()) {
			String at = atMatcher.group();
			String atLabel = parseAtsId(at.substring(1));
			if (atLabel != null && !atLabel.equals(at)) {
				message = message.replace(at, atLabel);
			}
		}

		if (md2html) {
    		message = MarkdownUtils.parse(message);
        }
		return message;
	}
	
	/**
	 * @param atid
	 * @return
	 */
	protected static String parseAtsId(String atid) {
		if (!ID.isId(atid)) {
			return atid;
		}

		final ID id = ID.valueOf(atid);
		if (id.getEntityCode() == EntityHelper.User) {
			if (Application.getUserStore().existsUser(id)) {
				return Application.getUserStore().getUser(id).getFullName();
			} else {
				return "[无效用户]";
			}
		}

        String recordLabel = FieldValueWrapper.getLabelNotry(id);
		String recordUrl = AppUtils.getContextPath() + "/app/list-and-view?id=" + id;
		return String.format("[%s](%s)", recordLabel, recordUrl);
	}
}
