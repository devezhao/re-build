/*
rebuild - Building your business-systems freely.
Copyright (C) 2018-2019 devezhao <zhaofang123@gmail.com>

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

package com.rebuild.web.feeds;

import cn.devezhao.commons.CalendarUtils;
import cn.devezhao.persist4j.engine.ID;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rebuild.server.Application;
import com.rebuild.server.business.feeds.FeedsHelper;
import com.rebuild.server.business.feeds.FeedsScope;
import com.rebuild.server.service.bizz.UserHelper;
import com.rebuild.utils.AppUtils;
import com.rebuild.web.BaseControll;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 动态公告
 *
 * @author devezhao
 * @since 2019/12/19
 */
@Controller
public class AnnouncementControll extends BaseControll {

    @RequestMapping("/commons/announcements")
    public void list(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ID user = AppUtils.getRequestUser(request);
        int fromWhere = 0;

        // 1=动态页 2=首页 4=登录页
        String refererUrl = request.getHeader("Referer");
        if (refererUrl.contains("/user/login")) {
            fromWhere =  4;
        } else if (refererUrl.contains("/dashboard/home")) {
            fromWhere =  2;
        } else if (refererUrl.contains("/feeds/")) {
            fromWhere =  1;
        }

        Object[][] array = Application.createQueryNoFilter(
                "select content,contentMore,scope,createdBy,createdOn,feedsId from Feeds where type = 3")
                .array();

        List<JSON> as = new ArrayList<>();
        long timeNow = CalendarUtils.now().getTime();
        for (Object[] o : array) {
            JSONObject options = JSON.parseObject((String) o[1]);

            // 不在指定位置

            int whereMask = options.getIntValue("showWhere");
            if ((fromWhere & whereMask) == 0) {
                continue;
            }

            // 不在公示时间
            Date timeStart = StringUtils.isBlank(options.getString("timeStart")) ? null : CalendarUtils.parse(options.getString("timeStart"));
            if (timeStart != null && timeNow < timeStart.getTime()) {
                continue;
            }
            Date timeEnd = StringUtils.isBlank(options.getString("timeEnd")) ? null : CalendarUtils.parse(options.getString("timeEnd"));
            if (timeEnd != null && timeNow > timeEnd.getTime()) {
                continue;
            }

            // 不可见
            boolean allow = false;
            String scope = (String) o[2];
            if (FeedsScope.ALL.name().equalsIgnoreCase(scope)) {
                allow = true;
            } else if (FeedsScope.SELF.name().equalsIgnoreCase(scope) && o[3].equals(user)) {
                allow = true;
            } else if (ID.isId(scope) && user != null) {
                ID teamId = ID.valueOf(scope);
                if (Application.getUserStore().existsAny(teamId)) {
                    allow = Application.getUserStore().getTeam(teamId).isMember(user);
                }
            }

            if (allow) {
                JSONObject a = new JSONObject();
                a.put("content", FeedsHelper.formatContent((String) o[0]));
                a.put("publishOn", CalendarUtils.getUTCDateTimeFormat().format(o[4]));
                a.put("publishBy", UserHelper.getName((ID) o[3]));
                a.put("id", o[5]);
                as.add(a);
            }
        }

        writeSuccess(response, as);
    }
}
