<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.rebuild.server.Application"%>
<!DOCTYPE html>
<html>
<head>
<%@ include file="/_include/Head.jsp"%>
<title>通用配置</title>
<style type="text/css">
.syscfg a.img-thumbnail{display:inline-block;padding:0.3rem 0;background-color:#fff;line-height:1;font-size:0;cursor:default;}
.syscfg a.img-thumbnail .logo-img{transform: scale(0.8);}
.syscfg h5{background-color:#eee;margin:0;padding:10px;}
.syscfg .table td{padding:10px;}
.syscfg .table td p{margin:0;color:#999;font-weight:normal;font-size:12px;}
</style>
</head>
<body>
<div class="rb-wrapper rb-fixed-sidebar rb-collapsible-sidebar rb-collapsible-sidebar-hide-logo rb-color-header">
	<jsp:include page="/_include/NavTop.jsp">
		<jsp:param value="通用配置" name="pageTitle"/>
	</jsp:include>
	<jsp:include page="/_include/NavLeftAdmin.jsp">
		<jsp:param value="systems" name="activeNav"/>
	</jsp:include>
	<div class="rb-content">
		<div class="main-content container-fluid syscfg">
			<div class="row">
				<div class="col-md-9 col-12">
					<div class="card">
						<div class="card-header card-header-divider">通用配置</div>
						<div class="card-body">
							<h5>通用</h5>
							<table class="table">
							<tbody>
								<tr>
									<td width="40%">名称<p>这将完全替换 REBUILD 的品牌名称</p></td>
									<td id="sc-AppName">REBUILD</td>
								</tr>
								<tr>
									<td>LOGO</td>
									<td>
										<a class="img-thumbnail"><i class="logo-img"></i></a>
										<a class="img-thumbnail bg-primary"><i class="logo-img white"></i></a>
									</td>
								</tr>
								<tr>
									<td>域名/主页地址</td>
									<td><a id="sc-HomeURL" href="https://nightly.getrebuild.com/" class="link" target="_blank">https://nightly.getrebuild.com/</a></td>
								</tr>
								<tr>
									<td>公开注册</td>
									<td id="sc-OpenSignUp">否</td>
								</tr>
							</tbody>
							</table>
							<h5>安全性</h5>
							<table class="table">
							<tbody>
								<tr>
									<td width="40%">登录时需要验证码</td>
									<td>重试3次以后</td>
								</tr>
								<tr>
									<td>登录密码安全策略</td>
									<td>一般（6位字符，无字符类型限制）</td>
								</tr>
							</tbody>
							</table>
						</div>
					</div>
				</div>
				<div class="col-md-3 col-12">
					<div class="card">
						<div class="card-header card-header-divider">关于 REBUILD</div>
						<div class="card-body">
							<p>版本 <a class="link" target="_blank" href="https://github.com/getrebuild/rebuild/releases"><%=Application.VER%></a></p>
							<ul style="line-height:2">
								<li><a class="link" target="_blank" href="https://getrebuild.com/docs/">帮助文档</a></li>
								<li><a class="link" target="_blank" href="mailto:getrebuild@sina.com?subject=技术支持">技术支持</a></li>
								<li><a class="link" target="_blank" href="${baseUrl}/gw/server-status">系统状态</a></li>
								<li><a class="link" target="_blank" href="https://github.com/getrebuild/rebuild">View on GitHub</a></li>
							</ul>
							<div class="text-muted"><i class="zmdi zmdi-info-outline"></i> 本软件系统使用 <a class="link" href="http://www.gnu.org/licenses/gpl-3.0.en.html" target="_blank">GPL-3.0</a> 许可。请遵循许可协议。</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
<%@ include file="/_include/Foot.jsp"%>
</body>
</html>
