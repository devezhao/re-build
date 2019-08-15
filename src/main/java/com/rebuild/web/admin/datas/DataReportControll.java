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

package com.rebuild.web.admin.datas;

import cn.devezhao.commons.CalendarUtils;
import com.rebuild.server.Application;
import com.rebuild.server.metadata.MetadataHelper;
import com.rebuild.server.metadata.entity.EasyMeta;
import com.rebuild.web.BasePageControll;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * TODO
 *
 * @author devezhao
 * @since 2019/8/13
 */
@Controller
@RequestMapping("/admin/datas/")
public class DataReportControll extends BasePageControll {

    @RequestMapping("/data-reports")
    public ModelAndView pageDataReports(HttpServletRequest request) {
        return createModelAndView("/admin/datas/data-reports.jsp");
    }

    @RequestMapping("/data-reports/check-template")
    public void checkTemplate(HttpServletRequest request, HttpServletResponse response) throws IOException {
    }

    @RequestMapping("/data-reports/list")
    public void reportList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String belongEntity = getParameter(request, "entity");
        String sql = "select configId,name,belongEntity,belongEntity,isDisabled,modifiedOn from DataReportConfig";
        if (StringUtils.isNotBlank(belongEntity)) {
            sql += " where belongEntity = '" + StringEscapeUtils.escapeSql(belongEntity) + "'";
        }
        sql += " order by name, modifiedOn desc";

        Object[][] array = Application.createQuery(sql).array();
        for (Object[] o : array) {
            o[3] = EasyMeta.getLabel(MetadataHelper.getEntity((String) o[3]));
            o[5] = CalendarUtils.getUTCDateTimeFormat().format(o[5]);
        }
        writeSuccess(response, array);
    }
}