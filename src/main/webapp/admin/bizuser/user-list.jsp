<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<%@ include file="/_include/Head.jsp"%>
<title>${entityLabel}管理</title>
</head>
<body>
<div class="rb-wrapper rb-fixed-sidebar rb-collapsible-sidebar rb-collapsible-sidebar-hide-logo rb-aside rb-color-header">
	<jsp:include page="/_include/NavTop.jsp">
		<jsp:param value="${entityLabel}管理" name="pageTitle"/>
	</jsp:include>
	<jsp:include page="/_include/NavLeftAdmin.jsp">
		<jsp:param value="users" name="activeNav"/>
	</jsp:include>
	<div class="rb-content">
		<aside class="page-aside">
			<div class="rb-scroller">
				<div class="dept-tree"></div>
			</div>
		</aside>
		<div class="main-content container-fluid">
			<ul class="nav nav-tabs nav-tabs-classic">
				<li class="nav-item"><a href="users" class="nav-link active"><span class="icon zmdi zmdi-account"></span> ${entityLabel}</a></li>
				<li class="nav-item"><a href="departments" class="nav-link"><span class="icon zmdi zmdi-accounts"></span> 部门</a></li>
			</ul>
			<div class="card card-table">
				<div class="card-body">
					<div class="dataTables_wrapper container-fluid">
						<div class="row rb-datatable-header">
							<div class="col-12 col-lg-5">
								<div class="dataTables_filter">
									<div class="input-group input-search" data-qfields="loginName,fullName,email,quickCode">
										<input class="form-control" type="text" placeholder="查询${entityLabel}" maxlength="40">
										<span class="input-group-btn"><button class="btn btn-secondary" type="button"><i class="icon zmdi zmdi-search"></i></button></span>
									</div>
								</div>
							</div>
							<div class="col-12 col-lg-7">
								<div class="dataTables_oper">
									<button class="btn btn-space btn-secondary J_view" disabled="disabled"><i class="icon zmdi zmdi-folder"></i> 打开</button>
									<div class="btn-group btn-space">
										<button class="btn btn-primary J_new" type="button"><i class="icon zmdi zmdi-account-add"></i> 新建${entityLabel}</button>
										<button class="btn btn-primary dropdown-toggle auto" type="button" data-toggle="dropdown"><span class="icon zmdi zmdi-chevron-down"></span></button>
										<div class="dropdown-menu dropdown-menu-primary dropdown-menu-right">
											<a class="dropdown-item J_new-dept"><i class="icon zmdi zmdi-accounts-add"></i> 新建部门</a>
                      					</div>
									</div>
									<div class="btn-group btn-space">
										<button class="btn btn-secondary dropdown-toggle" type="button" data-toggle="dropdown">更多 <i class="icon zmdi zmdi-more-vert"></i></button>
										<div class="dropdown-menu dropdown-menu-right">
											<a class="dropdown-item J_columns"><i class="icon zmdi zmdi-code-setting"></i> 列显示</a>
										</div>
									</div>
								</div>
							</div>
						</div>
						<div id="react-list" class="rb-loading rb-loading-active data-list">
							<%@ include file="/_include/spinner.jsp"%>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
<%@ include file="/_include/Foot.jsp"%>
<script>
window.__PageConfig = {
	type: 'RecordList',
	entity: ['User','${entityLabel}','${entityIcon}'],
	privileges: ${entityPrivileges},
	listConfig: ${DataListConfig},
	advFilter: false
}
</script>
<script src="${baseUrl}/assets/js/rb-datalist.jsx" type="text/babel"></script>
<script src="${baseUrl}/assets/js/rb-forms.jsx" type="text/babel"></script>
<script src="${baseUrl}/assets/js/rb-forms-ext.jsx" type="text/babel"></script>
<script src="${baseUrl}/assets/js/bizuser/dept-tree.js"></script>
<script type="text/babel">
let formPostType = 1
RbForm.postAfter = function(){
	if (formPostType == 1) RbListPage._RbList.reload()
	else loadDeptTree()
}
$(document).ready(function(){
	loadDeptTree()

	$('.J_new').click(function(){ formPostType = 1 })
	$('.J_new-dept').click(function(){
		formPostType = 2
		rb.RbFormModal({ title: '新建部门', entity: 'Department', icon: 'accounts' })
	})
})
clickDept = function(depts) {
	if (depts[0] == '$ALL$') depts = []
	let exp = { items: [], values: {} }
	exp.items.push({ op:'in', field: 'deptId', value:'{2}' })
	exp.values['2'] = depts
	RbListPage._RbList.search(exp)
}
</script>
</body>
</html>
