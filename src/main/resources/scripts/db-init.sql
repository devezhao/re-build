-- !!! MYSQL VERSION NOTICE !!!
-- IN 5.7 OR ABOVE, YOU SHOULD REMOVED THESE SQL_MODES IN my.cnf/my.ini FIRST.
-- ONLY_FULL_GROUP_BY
-- IN 8.0 OR ABOVE, ONLY SUPPORTS mysql_native_password AUTHENTICATION MODE
-- default_authentication_plugin=mysql_native_password

-- #1 database/user
-- 首次使用请移除以下注释以创建数据库和用户
/*
CREATE DATABASE rebuild10 COLLATE utf8mb4_general_ci;
CREATE USER 'rebuild'@'127.0.0.1' IDENTIFIED BY 'rebuild';
GRANT ALL PRIVILEGES ON rebuild10.* TO 'rebuild'@'127.0.0.1';
FLUSH PRIVILEGES;
USE rebuild10;
*/

-- #2 schemas
-- Generated by SchemaGen.java

-- ************ Entity [User] DDL ************
create table if not exists `user` (
  `USER_ID`            char(20) not null,
  `LOGIN_NAME`         varchar(100) not null comment '登录名',
  `PASSWORD`           varchar(100) not null comment '登录密码',
  `EMAIL`              varchar(100) comment '邮箱',
  `FULL_NAME`          varchar(100) comment '姓名',
  `AVATAR_URL`         varchar(200) comment '头像',
  `JOB_TITLE`          varchar(100) comment '职务',
  `WORKPHONE`          varchar(100) comment '电话',
  `DEPT_ID`            char(20) comment '部门',
  `ROLE_ID`            char(20) comment '角色',
  `IS_DISABLED`        char(1) default 'F' comment '是否停用',
  `QUICK_CODE`         varchar(70),
  `MODIFIED_BY`        char(20) not null comment '修改人',
  `MODIFIED_ON`        timestamp not null default current_timestamp comment '修改时间',
  `CREATED_ON`         timestamp not null default current_timestamp comment '创建时间',
  `CREATED_BY`         char(20) not null comment '创建人',
  primary key  (`USER_ID`),
  unique index UIX0_user (`LOGIN_NAME`),
  unique index UIX1_user (`EMAIL`),
  index IX2_user (`QUICK_CODE`, `FULL_NAME`, `EMAIL`)
)Engine=InnoDB;

-- ************ Entity [Department] DDL ************
create table if not exists `department` (
  `DEPT_ID`            char(20) not null,
  `NAME`               varchar(100) not null comment '部门名称',
  `PARENT_DEPT`        char(20) comment '父级部门',
  `PRINCIPAL_ID`       char(20) comment '负责人',
  `IS_DISABLED`        char(1) default 'F' comment '是否停用',
  `QUICK_CODE`         varchar(70),
  `CREATED_BY`         char(20) not null comment '创建人',
  `CREATED_ON`         timestamp not null default current_timestamp comment '创建时间',
  `MODIFIED_ON`        timestamp not null default current_timestamp comment '修改时间',
  `MODIFIED_BY`        char(20) not null comment '修改人',
  primary key  (`DEPT_ID`)
)Engine=InnoDB;

-- ************ Entity [Role] DDL ************
create table if not exists `role` (
  `ROLE_ID`            char(20) not null,
  `NAME`               varchar(100) not null comment '角色名称',
  `IS_DISABLED`        char(1) default 'F' comment '是否停用',
  `QUICK_CODE`         varchar(70),
  `CREATED_ON`         timestamp not null default current_timestamp comment '创建时间',
  `CREATED_BY`         char(20) not null comment '创建人',
  `MODIFIED_ON`        timestamp not null default current_timestamp comment '修改时间',
  `MODIFIED_BY`        char(20) not null comment '修改人',
  primary key  (`ROLE_ID`)
)Engine=InnoDB;

-- ************ Entity [RolePrivileges] DDL ************
create table if not exists `role_privileges` (
  `PRIVILEGES_ID`      char(20) not null,
  `ROLE_ID`            char(20) not null,
  `ENTITY`             int(11) not null default '0' comment '哪个实体',
  `ZERO_KEY`           varchar(50) comment '其他权限KEY',
  `DEFINITION`         varchar(100) comment '权限定义',
  primary key  (`PRIVILEGES_ID`),
  unique index UIX0_role_privileges (`ROLE_ID`, `ENTITY`, `ZERO_KEY`)
)Engine=InnoDB;

-- ************ Entity [RoleMember] DDL ************
create table if not exists `role_member` (
  `MEMBER_ID`          char(20) not null,
  `ROLE_ID`            char(20) not null,
  `USER_ID`            char(20) not null,
  primary key  (`MEMBER_ID`),
  unique index UIX0_role_member (`ROLE_ID`, `USER_ID`)
)Engine=InnoDB;

-- ************ Entity [Team] DDL ************
create table if not exists `team` (
  `TEAM_ID`            char(20) not null,
  `NAME`               varchar(100) not null comment '团队名称',
  `PRINCIPAL_ID`       char(20) comment '负责人',
  `IS_DISABLED`        char(1) default 'F' comment '是否停用',
  `QUICK_CODE`         varchar(70),
  `MODIFIED_ON`        timestamp not null default current_timestamp comment '修改时间',
  `CREATED_BY`         char(20) not null comment '创建人',
  `CREATED_ON`         timestamp not null default current_timestamp comment '创建时间',
  `MODIFIED_BY`        char(20) not null comment '修改人',
  primary key  (`TEAM_ID`)
)Engine=InnoDB;

-- ************ Entity [TeamMember] DDL ************
create table if not exists `team_member` (
  `MEMBER_ID`          char(20) not null,
  `TEAM_ID`            char(20) not null,
  `USER_ID`            char(20) not null,
  primary key  (`MEMBER_ID`),
  unique index UIX0_team_member (`TEAM_ID`, `USER_ID`)
)Engine=InnoDB;

-- ************ Entity [MetaEntity] DDL ************
create table if not exists `meta_entity` (
  `ENTITY_ID`          char(20) not null,
  `TYPE_CODE`          smallint(6) not null,
  `ENTITY_NAME`        varchar(100) not null,
  `PHYSICAL_NAME`      varchar(100) not null,
  `ENTITY_LABEL`       varchar(100) not null comment 'for description',
  `COMMENTS`           varchar(200),
  `ICON`               varchar(60),
  `NAME_FIELD`         varchar(100),
  `MASTER_ENTITY`      varchar(100) comment '明细实体的所属主实体',
  `CREATED_ON`         timestamp not null default current_timestamp comment '创建时间',
  `CREATED_BY`         char(20) not null comment '创建人',
  `MODIFIED_BY`        char(20) not null comment '修改人',
  `MODIFIED_ON`        timestamp not null default current_timestamp comment '修改时间',
  primary key  (`ENTITY_ID`),
  unique index UIX0_meta_entity (`TYPE_CODE`),
  unique index UIX1_meta_entity (`ENTITY_NAME`),
  unique index UIX2_meta_entity (`PHYSICAL_NAME`)
)Engine=InnoDB;

-- ************ Entity [MetaField] DDL ************
create table if not exists `meta_field` (
  `FIELD_ID`           char(20) not null,
  `BELONG_ENTITY`      varchar(100) not null,
  `FIELD_NAME`         varchar(100) not null,
  `PHYSICAL_NAME`      varchar(100) not null,
  `FIELD_LABEL`        varchar(100) not null comment 'for description',
  `DISPLAY_TYPE`       varchar(100) comment '显示类型. 详见 DisplayType',
  `NULLABLE`           char(1) default 'T',
  `CREATABLE`          char(1) default 'T',
  `UPDATABLE`          char(1) default 'T',
  `REPEATABLE`         char(1) default 'T',
  `DEFAULT_VALUE`      varchar(300) comment '此值不影响数据库默认值',
  `MAX_LENGTH`         smallint(6) default '300',
  `REF_ENTITY`         varchar(100),
  `CASCADE`            varchar(20),
  `COMMENTS`           varchar(300),
  `EXT_CONFIG`         varchar(700) comment '更多扩展配置, JSON格式KV',
  `CREATED_BY`         char(20) not null comment '创建人',
  `MODIFIED_BY`        char(20) not null comment '修改人',
  `CREATED_ON`         timestamp not null default current_timestamp comment '创建时间',
  `MODIFIED_ON`        timestamp not null default current_timestamp comment '修改时间',
  primary key  (`FIELD_ID`),
  unique index UIX0_meta_field (`BELONG_ENTITY`, `FIELD_NAME`),
  unique index UIX1_meta_field (`BELONG_ENTITY`, `PHYSICAL_NAME`)
)Engine=InnoDB;

-- ************ Entity [PickList] DDL ************
create table if not exists `pick_list` (
  `ITEM_ID`            char(20) not null,
  `BELONG_ENTITY`      varchar(100) not null,
  `BELONG_FIELD`       varchar(100) not null,
  `TEXT`               varchar(100) not null,
  `SEQ`                int(11) default '0' comment '排序, 小到大',
  `IS_DEFAULT`         char(1) default 'F',
  `IS_HIDE`            char(1) default 'F',
  `MASK_VALUE`         bigint(20) default '0' comment 'MultiSelect专用',
  `CREATED_BY`         char(20) not null comment '创建人',
  `CREATED_ON`         timestamp not null default current_timestamp comment '创建时间',
  `MODIFIED_ON`        timestamp not null default current_timestamp comment '修改时间',
  `MODIFIED_BY`        char(20) not null comment '修改人',
  primary key  (`ITEM_ID`),
  index IX0_pick_list (`BELONG_ENTITY`, `BELONG_FIELD`)
)Engine=InnoDB;

-- ************ Entity [LayoutConfig] DDL ************
create table if not exists `layout_config` (
  `CONFIG_ID`          char(20) not null,
  `CONFIG`             text(21845) not null comment 'JSON格式配置',
  `SHARE_TO`           varchar(420) default 'SELF' comment '共享给哪些人, 可选值: ALL/SELF/$MemberID(U/D/R)',
  `BELONG_ENTITY`      varchar(100) not null,
  `APPLY_TYPE`         varchar(20) not null comment 'FORM,DATALIST,NAV,TBA,ADD',
  `CONFIG_NAME`        varchar(100),
  `CREATED_ON`         timestamp not null default current_timestamp comment '创建时间',
  `CREATED_BY`         char(20) not null comment '创建人',
  `MODIFIED_BY`        char(20) not null comment '修改人',
  `MODIFIED_ON`        timestamp not null default current_timestamp comment '修改时间',
  primary key  (`CONFIG_ID`)
)Engine=InnoDB;

-- ************ Entity [FilterConfig] DDL ************
create table if not exists `filter_config` (
  `CONFIG_ID`          char(20) not null,
  `CONFIG`             text(21845) not null comment 'JSON格式配置',
  `SHARE_TO`           varchar(420) default 'SELF' comment '共享给哪些人, 可选值: ALL/SELF/$MemberID(U/D/R)',
  `BELONG_ENTITY`      varchar(100) not null,
  `FILTER_NAME`        varchar(100) not null,
  `MODIFIED_BY`        char(20) not null comment '修改人',
  `CREATED_ON`         timestamp not null default current_timestamp comment '创建时间',
  `MODIFIED_ON`        timestamp not null default current_timestamp comment '修改时间',
  `CREATED_BY`         char(20) not null comment '创建人',
  primary key  (`CONFIG_ID`)
)Engine=InnoDB;

-- ************ Entity [DashboardConfig] DDL ************
create table if not exists `dashboard_config` (
  `CONFIG_ID`          char(20) not null,
  `CONFIG`             text(21845) not null comment 'JSON格式配置',
  `SHARE_TO`           varchar(420) default 'SELF' comment '共享给哪些人, 可选值: ALL/SELF/$MemberID(U/D/R)',
  `TITLE`              varchar(100) not null,
  `MODIFIED_ON`        timestamp not null default current_timestamp comment '修改时间',
  `CREATED_BY`         char(20) not null comment '创建人',
  `MODIFIED_BY`        char(20) not null comment '修改人',
  `CREATED_ON`         timestamp not null default current_timestamp comment '创建时间',
  primary key  (`CONFIG_ID`)
)Engine=InnoDB;

-- ************ Entity [ChartConfig] DDL ************
create table if not exists `chart_config` (
  `CHART_ID`           char(20) not null,
  `CONFIG`             text(21845) not null comment 'JSON格式配置',
  `BELONG_ENTITY`      varchar(100) not null,
  `CHART_TYPE`         varchar(100) not null,
  `TITLE`              varchar(100) not null,
  `MODIFIED_ON`        timestamp not null default current_timestamp comment '修改时间',
  `MODIFIED_BY`        char(20) not null comment '修改人',
  `CREATED_BY`         char(20) not null comment '创建人',
  `CREATED_ON`         timestamp not null default current_timestamp comment '创建时间',
  primary key  (`CHART_ID`)
)Engine=InnoDB;

-- ************ Entity [Classification] DDL ************
create table if not exists `classification` (
  `DATA_ID`            char(20) not null,
  `NAME`               varchar(100) not null,
  `DESCRIPTION`        varchar(600),
  `IS_DISABLED`        char(1) default 'F',
  `OPEN_LEVEL`         smallint(6) default '0',
  `MODIFIED_ON`        timestamp not null default current_timestamp comment '修改时间',
  `CREATED_BY`         char(20) not null comment '创建人',
  `MODIFIED_BY`        char(20) not null comment '修改人',
  `CREATED_ON`         timestamp not null default current_timestamp comment '创建时间',
  primary key  (`DATA_ID`)
)Engine=InnoDB;

-- ************ Entity [ClassificationData] DDL ************
create table if not exists `classification_data` (
  `ITEM_ID`            char(20) not null,
  `DATA_ID`            char(20) not null,
  `NAME`               varchar(100) not null,
  `FULL_NAME`          varchar(300) not null comment '包括父级名称, 用点号分割',
  `PARENT`             char(20),
  `CODE`               varchar(50),
  `LEVEL`              smallint(6) default '0',
  `IS_HIDE`            char(1) default 'F',
  `QUICK_CODE`         varchar(70),
  `CREATED_BY`         char(20) not null comment '创建人',
  `MODIFIED_BY`        char(20) not null comment '修改人',
  `CREATED_ON`         timestamp not null default current_timestamp comment '创建时间',
  `MODIFIED_ON`        timestamp not null default current_timestamp comment '修改时间',
  primary key  (`ITEM_ID`),
  index IX0_classification_data (`DATA_ID`, `PARENT`),
  index IX1_classification_data (`DATA_ID`, `FULL_NAME`, `QUICK_CODE`)
)Engine=InnoDB;

-- ************ Entity [ShareAccess] DDL ************
create table if not exists `share_access` (
  `ACCESS_ID`          char(20) not null,
  `BELONG_ENTITY`      varchar(100) not null comment '哪个实体',
  `RECORD_ID`          char(20) not null comment '记录ID',
  `SHARE_TO`           char(20) not null comment '共享给谁(U/D/R)',
  `RIGHTS`             int(11) not null default '0' comment '共享权限(R=2,U=4,D=8,0=Auto)',
  `MODIFIED_ON`        timestamp not null default current_timestamp comment '修改时间',
  `CREATED_ON`         timestamp not null default current_timestamp comment '创建时间',
  `CREATED_BY`         char(20) not null comment '创建人',
  `MODIFIED_BY`        char(20) not null comment '修改人',
  primary key  (`ACCESS_ID`),
  index IX0_share_access (`BELONG_ENTITY`, `RECORD_ID`, `SHARE_TO`)
)Engine=InnoDB;

-- ************ Entity [SystemConfig] DDL ************
create table if not exists `system_config` (
  `CONFIG_ID`          char(20) not null,
  `ITEM`               varchar(100) not null,
  `VALUE`              varchar(600) not null,
  primary key  (`CONFIG_ID`),
  unique index UIX0_system_config (`ITEM`)
)Engine=InnoDB;

-- ************ Entity [Notification] DDL ************
create table if not exists `notification` (
  `MESSAGE_ID`         char(20) not null,
  `FROM_USER`          char(20) not null,
  `TO_USER`            char(20) not null,
  `MESSAGE`            varchar(3000),
  `UNREAD`             char(1) default 'T',
  `TYPE`               smallint(6) default '0' comment '消息分类',
  `RELATED_RECORD`     char(20) comment '相关业务记录',
  `MODIFIED_ON`        timestamp not null default current_timestamp comment '修改时间',
  `CREATED_BY`         char(20) not null comment '创建人',
  `CREATED_ON`         timestamp not null default current_timestamp comment '创建时间',
  `MODIFIED_BY`        char(20) not null comment '修改人',
  primary key  (`MESSAGE_ID`),
  index IX0_notification (`TO_USER`, `UNREAD`, `CREATED_ON`),
  index IX1_notification (`TO_USER`, `TYPE`, `CREATED_ON`)
)Engine=InnoDB;

-- ************ Entity [Attachment] DDL ************
create table if not exists `attachment` (
  `ATTACHMENT_ID`      char(20) not null,
  `BELONG_ENTITY`      smallint(6) default '0',
  `BELONG_FIELD`       varchar(100),
  `RELATED_RECORD`     char(20) comment '相关业务记录',
  `FILE_PATH`          varchar(200) not null,
  `FILE_TYPE`          varchar(20),
  `FILE_SIZE`          int(11) default '0' comment 'in bytes',
  `IN_FOLDER`          char(20),
  `IS_DELETED`         char(1) default 'F' comment '标记删除',
  `MODIFIED_BY`        char(20) not null comment '修改人',
  `CREATED_ON`         timestamp not null default current_timestamp comment '创建时间',
  `CREATED_BY`         char(20) not null comment '创建人',
  `MODIFIED_ON`        timestamp not null default current_timestamp comment '修改时间',
  primary key  (`ATTACHMENT_ID`),
  index IX0_attachment (`BELONG_ENTITY`, `BELONG_FIELD`, `FILE_PATH`, `IS_DELETED`),
  index IX1_attachment (`IN_FOLDER`, `CREATED_ON`, `FILE_PATH`),
  index IX2_attachment (`RELATED_RECORD`)
)Engine=InnoDB;

-- ************ Entity [AttachmentFolder] DDL ************
create table if not exists `attachment_folder` (
  `FOLDER_ID`          char(20) not null,
  `NAME`               varchar(100) not null,
  `PARENT`             char(20),
  `SCOPE`              varchar(20) default 'ALL' comment '哪些人可见, 可选值: ALL/SELF/$TeamID',
  `MODIFIED_BY`        char(20) not null comment '修改人',
  `CREATED_ON`         timestamp not null default current_timestamp comment '创建时间',
  `MODIFIED_ON`        timestamp not null default current_timestamp comment '修改时间',
  `CREATED_BY`         char(20) not null comment '创建人',
  primary key  (`FOLDER_ID`),
  index IX0_attachment_folder (`SCOPE`, `CREATED_BY`)
)Engine=InnoDB;

-- ************ Entity [LoginLog] DDL ************
create table if not exists `login_log` (
  `LOG_ID`             char(20) not null,
  `USER`               char(20) not null comment '登陆用户',
  `IP_ADDR`            varchar(100) comment 'IP地址',
  `USER_AGENT`         varchar(200) comment '客户端',
  `LOGIN_TIME`         timestamp not null default current_timestamp comment '登陆时间',
  `LOGOUT_TIME`        timestamp null default null comment '退出时间',
  primary key  (`LOG_ID`),
  index IX0_login_log (`USER`, `LOGIN_TIME`)
)Engine=InnoDB;

-- ************ Entity [AutoFillinConfig] DDL ************
create table if not exists `auto_fillin_config` (
  `CONFIG_ID`          char(20) not null,
  `BELONG_ENTITY`      varchar(100) not null,
  `BELONG_FIELD`       varchar(100) not null,
  `SOURCE_FIELD`       varchar(100) not null comment '引用实体的字段',
  `TARGET_FIELD`       varchar(100) not null comment '当前实体的字段',
  `EXT_CONFIG`         varchar(700) comment '更多扩展配置, JSON格式KV',
  `CREATED_BY`         char(20) not null comment '创建人',
  `MODIFIED_ON`        timestamp not null default current_timestamp comment '修改时间',
  `CREATED_ON`         timestamp not null default current_timestamp comment '创建时间',
  `MODIFIED_BY`        char(20) not null comment '修改人',
  primary key  (`CONFIG_ID`)
)Engine=InnoDB;

-- ************ Entity [RobotTriggerConfig] DDL ************
create table if not exists `robot_trigger_config` (
  `CONFIG_ID`          char(20) not null,
  `BELONG_ENTITY`      varchar(100) not null,
  `WHEN`               int(11) default '0' comment '动作(累加值)',
  `WHEN_TIMER`         varchar(100) comment '定期执行',
  `WHEN_FILTER`        text(21845) comment '附加过滤器',
  `ACTION_TYPE`        varchar(50) not null comment '预定义的触发操作类型',
  `ACTION_CONTENT`     text(21845) comment '预定义的触发操作类型, JSON KV 对',
  `PRIORITY`           int(11) default '1' comment '执行优先级, 越大越高(越先执行)',
  `NAME`               varchar(100) comment '触发器名称',
  `IS_DISABLED`        char(1) default 'F' comment '是否停用',
  `MODIFIED_ON`        timestamp not null default current_timestamp comment '修改时间',
  `CREATED_ON`         timestamp not null default current_timestamp comment '创建时间',
  `CREATED_BY`         char(20) not null comment '创建人',
  `MODIFIED_BY`        char(20) not null comment '修改人',
  primary key  (`CONFIG_ID`)
)Engine=InnoDB;

-- ************ Entity [RobotApprovalConfig] DDL ************
create table if not exists `robot_approval_config` (
  `CONFIG_ID`          char(20) not null,
  `BELONG_ENTITY`      varchar(100) not null comment '应用实体',
  `NAME`               varchar(100) not null comment '流程名称',
  `FLOW_DEFINITION`    text(21845) comment '流程定义',
  `IS_DISABLED`        char(1) default 'F' comment '是否停用',
  `CREATED_BY`         char(20) not null comment '创建人',
  `MODIFIED_BY`        char(20) not null comment '修改人',
  `MODIFIED_ON`        timestamp not null default current_timestamp comment '修改时间',
  `CREATED_ON`         timestamp not null default current_timestamp comment '创建时间',
  primary key  (`CONFIG_ID`)
)Engine=InnoDB;

-- ************ Entity [RobotApprovalStep] DDL ************
create table if not exists `robot_approval_step` (
  `STEP_ID`            char(20) not null,
  `RECORD_ID`          char(20) not null comment '审批记录',
  `APPROVAL_ID`        char(20) not null comment '审批流程',
  `NODE`               varchar(100) not null comment '审批节点',
  `APPROVER`           char(20) not null comment '审批人',
  `STATE`              smallint(6) default '1' comment '审批结果',
  `REMARK`             varchar(600) comment '批注',
  `APPROVED_TIME`      timestamp null default null comment '审批时间',
  `PREV_NODE`          varchar(100) not null comment '上一审批节点',
  `IS_CANCELED`        char(1) default 'F' comment '是否取消',
  `IS_WAITING`         char(1) default 'F' comment '是否生效',
  `CREATED_BY`         char(20) not null comment '创建人',
  `MODIFIED_BY`        char(20) not null comment '修改人',
  `MODIFIED_ON`        timestamp not null default current_timestamp comment '修改时间',
  `CREATED_ON`         timestamp not null default current_timestamp comment '创建时间',
  primary key  (`STEP_ID`),
  index IX0_robot_approval_step (`RECORD_ID`, `APPROVAL_ID`, `NODE`, `IS_CANCELED`, `IS_WAITING`)
)Engine=InnoDB;

-- ************ Entity [RebuildApi] DDL ************
create table if not exists `rebuild_api` (
  `UNIQUE_ID`          char(20) not null,
  `APP_ID`             varchar(20) not null comment 'APPID',
  `APP_SECRET`         varchar(60) not null comment 'APPSECRET',
  `BIND_USER`          char(20) comment '绑定用户(权限)',
  `BIND_IPS`           varchar(300) comment 'IP白名单',
  `MODIFIED_BY`        char(20) not null comment '修改人',
  `CREATED_ON`         timestamp not null default current_timestamp comment '创建时间',
  `CREATED_BY`         char(20) not null comment '创建人',
  `MODIFIED_ON`        timestamp not null default current_timestamp comment '修改时间',
  primary key  (`UNIQUE_ID`),
  unique index UIX0_rebuild_api (`APP_ID`)
)Engine=InnoDB;

-- ************ Entity [RebuildApiRequest] DDL ************
create table if not exists `rebuild_api_request` (
  `REQUEST_ID`         char(20) not null,
  `APP_ID`             varchar(20) not null comment 'APPID',
  `REMOTE_IP`          varchar(100) not null comment '来源IP',
  `REQUEST_URL`        varchar(300) not null comment '请求URL',
  `REQUEST_BODY`       text(10000) comment '请求数据',
  `RESPONSE_BODY`      text(10000) not null comment '响应数据',
  `REQUEST_TIME`       timestamp not null default current_timestamp comment '请求时间',
  `RESPONSE_TIME`      timestamp not null default current_timestamp comment '响应时间',
  primary key  (`REQUEST_ID`),
  index IX0_rebuild_api_request (`APP_ID`, `REMOTE_IP`, `REQUEST_URL`, `REQUEST_TIME`)
)Engine=InnoDB;

-- ************ Entity [DataReportConfig] DDL ************
create table if not exists `data_report_config` (
  `CONFIG_ID`          char(20) not null,
  `BELONG_ENTITY`      varchar(100) not null comment '应用实体',
  `NAME`               varchar(100) not null comment '报表名称',
  `TEMPLATE_FILE`      varchar(200) comment '模板文件',
  `TEMPLATE_CONTENT`   text(20000) comment '模板内容',
  `IS_DISABLED`        char(1) default 'F' comment '是否停用',
  `CREATED_ON`         timestamp not null default current_timestamp comment '创建时间',
  `MODIFIED_BY`        char(20) not null comment '修改人',
  `MODIFIED_ON`        timestamp not null default current_timestamp comment '修改时间',
  `CREATED_BY`         char(20) not null comment '创建人',
  primary key  (`CONFIG_ID`)
)Engine=InnoDB;

-- ************ Entity [RecycleBin] DDL ************
create table if not exists `recycle_bin` (
  `RECYCLE_ID`         char(20) not null,
  `BELONG_ENTITY`      varchar(100) not null comment '所属实体',
  `RECORD_ID`          char(20) not null comment 'ID字段值',
  `RECORD_NAME`        varchar(200) not null comment '名称字段值',
  `RECORD_CONTENT`     longtext not null comment '数据',
  `DELETED_BY`         char(20) not null comment '删除人',
  `DELETED_ON`         timestamp not null default current_timestamp comment '删除时间',
  `CHANNEL_WITH`       char(20) comment '删除渠道(空为直接删除，否则为关联删除)',
  primary key  (`RECYCLE_ID`),
  index IX0_recycle_bin (`BELONG_ENTITY`, `RECORD_NAME`, `DELETED_BY`, `DELETED_ON`),
  index IX1_recycle_bin (`RECORD_ID`, `CHANNEL_WITH`)
)Engine=InnoDB;

-- ************ Entity [RevisionHistory] DDL ************
create table if not exists `revision_history` (
  `REVISION_ID`        char(20) not null,
  `BELONG_ENTITY`      varchar(100) not null comment '所属实体',
  `RECORD_ID`          char(20) not null comment '记录ID',
  `REVISION_TYPE`      smallint(6) default '1' comment '变更类型',
  `REVISION_CONTENT`   longtext not null comment '变更数据',
  `REVISION_BY`        char(20) not null comment '操作人',
  `REVISION_ON`        timestamp not null default current_timestamp comment '操作时间',
  `CHANNEL_WITH`       char(20) comment '变更渠道(空为直接，否则为关联)',
  primary key  (`REVISION_ID`),
  index IX0_revision_history (`BELONG_ENTITY`, `REVISION_TYPE`, `REVISION_BY`, `REVISION_ON`),
  index IX1_revision_history (`RECORD_ID`, `CHANNEL_WITH`)
)Engine=InnoDB;

-- ************ Entity [Feeds] DDL ************
create table if not exists `feeds` (
  `FEEDS_ID`           char(20) not null,
  `TYPE`               smallint(6) not null default '1' comment '类型',
  `CONTENT`            text(3000) not null comment '内容',
  `CONTENT_MORE`       text(3000) comment '不同类型的扩展内容, JSON格式KV',
  `IMAGES`             varchar(700) comment '图片',
  `ATTACHMENTS`        varchar(700) comment '附件',
  `RELATED_RECORD`     char(20) comment '相关业务记录',
  `SCHEDULE_TIME`      timestamp null default null comment '日程时间',
  `SCOPE`              varchar(20) default 'ALL' comment '哪些人可见, 可选值: ALL/SELF/$TeamID',
  `MODIFIED_ON`        timestamp not null default current_timestamp comment '修改时间',
  `MODIFIED_BY`        char(20) not null comment '修改人',
  `CREATED_BY`         char(20) not null comment '创建人',
  `CREATED_ON`         timestamp not null default current_timestamp comment '创建时间',
  primary key  (`FEEDS_ID`),
  index IX0_feeds (`CREATED_ON`, `SCOPE`, `TYPE`, `CREATED_BY`),
  index IX1_feeds (`RELATED_RECORD`),
  index IX2_feeds (`TYPE`, `SCHEDULE_TIME`, `CREATED_BY`),
  fulltext index FIX3_feeds (`CONTENT`)
)Engine=InnoDB;

-- ************ Entity [FeedsComment] DDL ************
create table if not exists `feeds_comment` (
  `COMMENT_ID`         char(20) not null,
  `FEEDS_ID`           char(20) not null comment '哪个动态',
  `CONTENT`            text(3000) not null comment '内容',
  `IMAGES`             varchar(700) comment '图片',
  `ATTACHMENTS`        varchar(700) comment '附件',
  `MODIFIED_BY`        char(20) not null comment '修改人',
  `CREATED_ON`         timestamp not null default current_timestamp comment '创建时间',
  `MODIFIED_ON`        timestamp not null default current_timestamp comment '修改时间',
  `CREATED_BY`         char(20) not null comment '创建人',
  primary key  (`COMMENT_ID`),
  index IX0_feeds_comment (`FEEDS_ID`)
)Engine=InnoDB;

-- ************ Entity [FeedsLike] DDL ************
create table if not exists `feeds_like` (
  `LIKE_ID`            char(20) not null,
  `SOURCE`             char(20) not null comment '哪个动态/评论',
  `CREATED_BY`         char(20) not null comment '创建人',
  `CREATED_ON`         timestamp not null default current_timestamp comment '创建时间',
  primary key  (`LIKE_ID`),
  index IX0_feeds_like (`SOURCE`, `CREATED_BY`)
)Engine=InnoDB;

-- ************ Entity [FeedsMention] DDL ************
create table if not exists `feeds_mention` (
  `MENTION_ID`         char(20) not null,
  `FEEDS_ID`           char(20) not null comment '哪个动态',
  `COMMENT_ID`         char(20) comment '哪个评论',
  `USER`               char(20) not null comment '哪个用户',
  primary key  (`MENTION_ID`),
  index IX0_feeds_mention (`USER`, `FEEDS_ID`, `COMMENT_ID`)
)Engine=InnoDB;

-- ************ Entity [SmsendLog] DDL ************
create table if not exists `smsend_log` (
  `SEND_ID`            char(20) not null,
  `TO`                 varchar(100) not null comment '收件人',
  `CONTENT`            text(21845) not null comment '发送内容',
  `SEND_TIME`          timestamp not null default current_timestamp comment '发送时间',
  `SEND_RESULT`        varchar(200) comment '发送结果(OK:xxx|ERR:xxx)',
  primary key  (`SEND_ID`),
  index IX0_smsend_log (`SEND_TIME`, `SEND_RESULT`)
)Engine=InnoDB;

-- #3 datas

-- User
insert into `user` (`USER_ID`, `LOGIN_NAME`, `PASSWORD`, `FULL_NAME`, `DEPT_ID`, `ROLE_ID`, `IS_DISABLED`, `CREATED_ON`, `CREATED_BY`, `MODIFIED_ON`, `MODIFIED_BY`, `QUICK_CODE`)
  values
  ('001-0000000000000000', 'system', 'system', '系统用户', '002-0000000000000001', '003-0000000000000001', 'T', CURRENT_TIMESTAMP, '001-0000000000000000', CURRENT_TIMESTAMP, '001-0000000000000000', 'XTYH'),
  ('001-0000000000000001', 'admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', '超级管理员', '002-0000000000000001', '003-0000000000000001', 'F', CURRENT_TIMESTAMP, '001-0000000000000000', CURRENT_TIMESTAMP, '001-0000000000000000', 'CJGLY'),
  ('001-9000000000000001', 'rebuild', 'cf44886e54f424ce136dc38e4d9ef5b4b556d06060705262d6fcce02b4322539', 'RB示例用户', '002-9000000000000001', '003-9000000000000001', 'F', CURRENT_TIMESTAMP, '001-0000000000000000', CURRENT_TIMESTAMP, '001-0000000000000000', 'RBSLYH');
-- Department
insert into `department` (`DEPT_ID`, `NAME`, `CREATED_ON`, `CREATED_BY`, `MODIFIED_ON`, `MODIFIED_BY`, `QUICK_CODE`)
  values
  ('002-0000000000000001', '总部', CURRENT_TIMESTAMP, '001-0000000000000000', CURRENT_TIMESTAMP, '001-0000000000000000', 'ZB'),
  ('002-9000000000000001', 'RB示例部门', CURRENT_TIMESTAMP, '001-0000000000000000', CURRENT_TIMESTAMP, '001-0000000000000000', 'RBSLBM');
-- Role
insert into `role` (`ROLE_ID`, `NAME`, `CREATED_ON`, `CREATED_BY`, `MODIFIED_ON`, `MODIFIED_BY`, `QUICK_CODE`)
  values
  ('003-0000000000000001', '管理员', CURRENT_TIMESTAMP, '001-0000000000000000', CURRENT_TIMESTAMP, '001-0000000000000000', 'GLY'),
  ('003-9000000000000001', 'RB示例角色', CURRENT_TIMESTAMP, '001-0000000000000000', CURRENT_TIMESTAMP, '001-0000000000000000', 'RBSLJS');
-- Team
insert into `team` (`TEAM_ID`, `NAME`, `CREATED_ON`, `CREATED_BY`, `MODIFIED_ON`, `MODIFIED_BY`, `QUICK_CODE`)
  values
  ('006-9000000000000001', 'RB示例团队', CURRENT_TIMESTAMP, '001-0000000000000000', CURRENT_TIMESTAMP, '001-0000000000000000', 'RBSLTD');

-- Layouts
insert into `layout_config` (`CONFIG_ID`, `BELONG_ENTITY`, `CONFIG`, `APPLY_TYPE`, `SHARE_TO`, `CREATED_ON`, `CREATED_BY`, `MODIFIED_ON`, `MODIFIED_BY`)
  values
  ('013-9000000000000001', 'Department', '[{"field":"name","isFull":false},{"field":"parentDept","isFull":false},{"field":"isDisabled","isFull":false}]', 'FORM', 'ALL', CURRENT_TIMESTAMP, '001-0000000000000001', CURRENT_TIMESTAMP, '001-0000000000000001'),
  ('013-9000000000000002', 'User', '[{"field":"fullName","isFull":false},{"field":"email","isFull":false},{"field":"loginName","isFull":false},{"field":"password","isFull":false},{"field":"$DIVIDER$","isFull":true},{"field":"deptId","isFull":false},{"field":"roleId","isFull":false},{"field":"isDisabled","isFull":false}]', 'FORM', 'ALL', CURRENT_TIMESTAMP, '001-0000000000000001', CURRENT_TIMESTAMP, '001-0000000000000001'),
  ('013-9000000000000003', 'Role', '[{"field":"name","isFull":false},{"field":"isDisabled","isFull":false}]', 'FORM', 'ALL', CURRENT_TIMESTAMP, '001-0000000000000001', CURRENT_TIMESTAMP, '001-0000000000000001'),
  ('013-9000000000000004', 'Team', '[{"field":"name","isFull":false},{"field":"isDisabled","isFull":false}]', 'FORM', 'ALL', CURRENT_TIMESTAMP, '001-0000000000000001', CURRENT_TIMESTAMP, '001-0000000000000001'),
  ('013-9000000000000005', 'LoginLog', '[{"field":"user"},{"field":"loginTime"},{"field":"userAgent"},{"field":"ipAddr"},{"field":"logoutTime"}]', 'DATALIST', 'ALL', CURRENT_TIMESTAMP, '001-0000000000000001', CURRENT_TIMESTAMP, '001-0000000000000001');

-- Classifications (No data)
insert into `classification` (`DATA_ID`, `NAME`, `DESCRIPTION`, `OPEN_LEVEL`, `IS_DISABLED`, `CREATED_ON`, `CREATED_BY`, `MODIFIED_ON`, `MODIFIED_BY`)
  values
  ('018-0000000000000001', '地区', NULL, 2, 'F', CURRENT_TIMESTAMP, '001-0000000000000001', CURRENT_TIMESTAMP, '001-0000000000000001'),
  ('018-0000000000000002', '行业', NULL, 1, 'F', CURRENT_TIMESTAMP, '001-0000000000000001', CURRENT_TIMESTAMP, '001-0000000000000001');

-- DB Version
insert into `system_config` (`CONFIG_ID`, `ITEM`, `VALUE`)
  values ('021-9000000000000001', 'DBVer', 25);
