/*
Copyright (c) REBUILD <https://getrebuild.com/> and its owners. All rights reserved.

rebuild is dual-licensed under commercial and open source licenses (GPLv3).
See LICENSE and COMMERCIAL in the project root for license information.
*/

package com.rebuild.web.project;

import cn.devezhao.bizz.privileges.PrivilegesException;
import cn.devezhao.commons.CalendarUtils;
import cn.devezhao.commons.web.ServletUtils;
import cn.devezhao.persist4j.Record;
import cn.devezhao.persist4j.engine.ID;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rebuild.server.Application;
import com.rebuild.server.configuration.ConfigEntry;
import com.rebuild.server.configuration.ProjectManager;
import com.rebuild.server.helper.ConfigurationException;
import com.rebuild.server.metadata.EntityHelper;
import com.rebuild.server.service.bizz.UserHelper;
import com.rebuild.server.service.project.ProjectTaskService;
import com.rebuild.utils.JSONUtils;
import com.rebuild.web.BasePageControll;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

/**
 * 任务
 *
 * @author devezhao
 * @since 2020/6/29
 */
@Controller
public class ProjectTaskControll extends BasePageControll {

    @RequestMapping("/project/task/{taskId}")
    public ModelAndView pageTask(@PathVariable String taskId,
                                 HttpServletRequest request, HttpServletResponse response) throws IOException {
        ID taskId2 = ID.isId(taskId) ? ID.valueOf(taskId) : null;
        if (taskId2 == null) {
            response.sendError(404);
            return null;
        }

        ConfigEntry project;
        try {
            project = ProjectManager.instance.getProjectByTask(taskId2, getRequestUser(request));
        } catch (ConfigurationException ex) {
            response.sendError(404);
            return null;
        } catch (PrivilegesException ex) {
            response.sendError(403);
            return null;
        }

        ModelAndView mv = createModelAndView("/project/task-view.jsp");
        mv.getModelMap().put("id", taskId2.toLiteral());
        mv.getModelMap().put("projectIcon", project.getString("iconName"));
        return mv;
    }

    @RequestMapping("/project/tasks/list")
    public void taskList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ID planId = getIdParameterNotNull(request, "plan");
        Object[][] tasks = Application.createQuery(
                "select " + BASE_FIELDS + " from ProjectTask where projectPlanId = ? order by seq asc")
                .setParameter(1, planId)
                .array();

        JSONArray alist = new JSONArray();
        for (Object[] o : tasks) {
            alist.add(formatTask(o));
        }

        JSON ret = JSONUtils.toJSONObject(new String[]{"count", "tasks"}, new Object[]{tasks.length, alist});
        writeSuccess(response, ret);
    }

    @RequestMapping("/project/tasks/post")
    public void taskPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JSONObject post = (JSONObject) ServletUtils.getRequestJson(request);
        Record record = EntityHelper.parse(post, getRequestUser(request));
        Application.getBean(ProjectTaskService.class).createOrUpdate(record);
        writeSuccess(response);
    }

    @RequestMapping("/project/tasks/delete")
    public void taskDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
    }

    @RequestMapping("/project/tasks/get")
    public void taskGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ID taskId = getIdParameterNotNull(request, "task");
        Object[] task = Application.createQuery(
                "select " + BASE_FIELDS + " from ProjectTask where taskId = ?")
                .setParameter(1, taskId)
                .unique();

        writeSuccess(response, formatTask(task));
    }

    @RequestMapping("/project/tasks/detail")
    public void taskDetail(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ID taskId = getIdParameterNotNull(request, "task");
        Object[] task = Application.createQuery(
                "select " + BASE_FIELDS + ",projectId,projectPlanId from ProjectTask where taskId = ?")
                .setParameter(1, taskId)
                .unique();

        JSONObject details = formatTask(task);

        // 面板（状态）
        ID projectId = (ID) task[11];
        ID currentPlanId = (ID) task[12];
        ConfigEntry[] plans = ProjectManager.instance.getPlansOfProject(projectId);
        JSONArray stateOfPlans = new JSONArray();
        for (ConfigEntry e : plans) {
            ID planId = e.getID("id");
            stateOfPlans.add(JSONUtils.toJSONObject(new String[] { "id", "text" },
                    new Object[] { planId, e.getString("planName") }));

            if (planId.equals(currentPlanId)) {
                details.put("nextStateOfPlans", e.get("flowNexts", Set.class));
            }
        }
        details.put("stateOfPlans", stateOfPlans);
        details.put("projectPlanId", currentPlanId);

        writeSuccess(response, details);
    }

    private static final String BASE_FIELDS = "projectId.projectCode,taskNumber,taskId,taskName,createdOn,deadline,executor,status,seq,priority,endTime";
    /**
     * @param o
     * @return
     */
    private JSONObject formatTask(Object[] o) {
        String taskNumber = o[1].toString();
        if (StringUtils.isNotBlank((String) o[0])) taskNumber = o[0] + "-" + taskNumber;

        String createdOn = formatUTCWithZone((Date) o[4]);
        String deadline = formatUTCWithZone((Date) o[5]);
        String endTime = formatUTCWithZone((Date) o[10]);

        Object[] executor = o[6] == null ? null : new Object[]{ o[6], UserHelper.getName((ID) o[6]) };

        return JSONUtils.toJSONObject(
                new String[] { "id", "taskNumber", "taskName", "createdOn", "deadline", "executor", "status", "seq", "priority", "endTime" },
                new Object[] { o[2], taskNumber, o[3], createdOn, deadline, executor, o[7], o[8], o[9], endTime });
    }

    /**
     * @param date
     * @return
     */
    private String formatUTCWithZone(Date date) {
        if (date == null) return null;
        int offset = CalendarUtils.getInstance().get(Calendar.ZONE_OFFSET);
        offset = offset / 1000 / 60 / 60;  // hours
        return CalendarUtils.getUTCDateTimeFormat().format(date) + " UTC" + (offset > 0 ? "+" : "") + offset;
    }
}