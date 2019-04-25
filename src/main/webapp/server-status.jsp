<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.rebuild.utils.AppUtils"%>
<%@ page import="com.rebuild.server.helper.SysConfiguration"%>
<%@ page import="org.apache.commons.lang.SystemUtils"%>
<%@ page import="cn.devezhao.commons.CalendarUtils"%>
<%@ page import="com.rebuild.server.ServerListener"%>
<%@ page import="com.rebuild.server.Application"%>
<%@ page import="com.rebuild.server.ServerStatus"%>
<%@ page import="com.rebuild.server.ServerStatus.Status"%>
<!DOCTYPE html>
<html>
<head>
<%@ include file="/_include/Head.jsp"%>
<title>系统状态</title>
<style type="text/css">
.block{margin:0 auto;max-width:1000px;padding:0 14px;margin-top:30px;}
.error{background-color:#ea4335;color:#fff;padding:18px 0;}
.error a{color:#fff;text-decoration:underline;}
</style>
</head>
<body>
<%
	if (!ServerStatus.isStatusOK()) {
%>
<div class="error">
<div class="block mt-0">
	<h4 class="mt-0">系统故障</h4>
	<div>部分服务未能正常启动，请通过快速检查列表排除故障，故障排除后建议重启服务。你也可以获取 <a href="mailto:getrebuild@sina.com?subject=系统故障">技术支持</a></div>
</div>
</div>
<%
	}
%>
<div class="block">
	<h5 class="text-bold">快速检查</h5>
	<table class="table table-bordered table-sm table-hover">
	<tbody>
		<%
			for (Status s : ServerStatus.getLastStatus()) {
		%>
		<tr>
			<th width="30%"><%=s.name%></th>
			<td class="text-danger"><%=s.success ? "<span class='text-success'>OK<span>" : ("ERROR : " + s.error)%></td>
		</tr>
		<%
			}
		%>
		<tr>
			<th>Memory Usage</th>
			<td>n/a</td>
		</tr>
		<tr>
			<th>CPU Usage</th>
			<td>n/a</td>
		</tr>
	</tbody>
	</table>
</div>
<%
	if (AppUtils.getRequestUser(request) != null) {
%>
<div class="block">
	<h5 class="text-bold">系统信息</h5>
	<table class="table table-bordered table-sm table-hover">
	<tbody>
		<tr>
			<th width="30%">Application Version</th>
			<td><a href="https://github.com/getrebuild/rebuild/releases"><%=Application.VER%></a></td>
		</tr>
		<tr>
			<th>Startup Time</th>
			<td><%=ServerListener.getStartupTime()%></td>
		</tr>
		<tr>
			<th>System Time</th>
			<td><%=CalendarUtils.now()%></td>
		</tr>
		<tr>
			<th>OS</th>
			<td><%=SystemUtils.OS_NAME%> (<%=SystemUtils.OS_ARCH%>)</td>
		</tr>
		<tr>
			<th>JVM</th>
			<td><%=SystemUtils.JAVA_VERSION%> (<%=SystemUtils.JAVA_VENDOR%>)</td>
		</tr>
		<tr>
			<th>Catalina Base</th>
			<td><%=System.getProperty("catalina.base")%></td>
		</tr>
		<tr>
			<th>Temp Directory</th>
			<td><%=SysConfiguration.getFileOfTemp("/")%></td>
		</tr>
	</tbody>
	</table>
</div>
<% } %>
<div class="block">
	<div class="text-muted">
		&copy; 2019 <a href="https://getrebuild.com/">REBUILD</a>
		<% if (AppUtils.getRequestUser(request) != null) { %>
		&nbsp;·&nbsp;
		<a href="server-status.json">Status Api</a>
		<% } %>
	</div>
</div>
</body>
</html>