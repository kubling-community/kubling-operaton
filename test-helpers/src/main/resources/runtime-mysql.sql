create table ACT_RU_EXECUTION (
    ID_ varchar(64),
    REV_ integer,
    ROOT_PROC_INST_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    BUSINESS_KEY_ varchar(255),
    PARENT_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    SUPER_EXEC_ varchar(64),
    SUPER_CASE_EXEC_ varchar(64),
    CASE_INST_ID_ varchar(64),
    ACT_ID_ varchar(255),
    ACT_INST_ID_ varchar(64),
    IS_ACTIVE_ TINYINT,
    IS_CONCURRENT_ TINYINT,
    IS_SCOPE_ TINYINT,
    IS_EVENT_SCOPE_ TINYINT,
    SUSPENSION_STATE_ integer,
    CACHED_ENT_STATE_ integer,
    SEQUENCE_COUNTER_ bigint,
    TENANT_ID_ varchar(64),
    PROC_DEF_KEY_ varchar(255),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RU_JOB (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    TYPE_ varchar(255) NOT NULL,
    LOCK_EXP_TIME_ datetime NULL,
    LOCK_OWNER_ varchar(255),
    EXCLUSIVE_ boolean,
    EXECUTION_ID_ varchar(64),
    ROOT_PROC_INST_ID_ varchar(64),
    PROCESS_INSTANCE_ID_ varchar(64),
    PROCESS_DEF_ID_ varchar(64),
    PROCESS_DEF_KEY_ varchar(255),
    RETRIES_ integer,
    EXCEPTION_STACK_ID_ varchar(64),
    EXCEPTION_MSG_ varchar(4000),
    FAILED_ACT_ID_ varchar(255),
    DUEDATE_ datetime NULL,
    REPEAT_ varchar(255),
    REPEAT_OFFSET_ bigint DEFAULT 0,
    HANDLER_TYPE_ varchar(255),
    HANDLER_CFG_ varchar(4000),
    DEPLOYMENT_ID_ varchar(64),
    SUSPENSION_STATE_ integer NOT NULL DEFAULT 1,
    JOB_DEF_ID_ varchar(64),
    PRIORITY_ bigint NOT NULL DEFAULT 0,
    SEQUENCE_COUNTER_ bigint,
    TENANT_ID_ varchar(64),
    CREATE_TIME_ datetime,
    LAST_FAILURE_LOG_ID_ varchar(64),
    BATCH_ID_ varchar(64),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RU_JOBDEF (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    PROC_DEF_ID_ varchar(64),
    PROC_DEF_KEY_ varchar(255),
    ACT_ID_ varchar(255),
    JOB_TYPE_ varchar(255) NOT NULL,
    JOB_CONFIGURATION_ varchar(255),
    SUSPENSION_STATE_ integer,
    JOB_PRIORITY_ bigint,
    TENANT_ID_ varchar(64),
    DEPLOYMENT_ID_ varchar(64),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RU_TASK (
    ID_ varchar(64),
    REV_ integer,
    EXECUTION_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    CASE_EXECUTION_ID_ varchar(64),
    CASE_INST_ID_ varchar(64),
    CASE_DEF_ID_ varchar(64),
    NAME_ varchar(255),
    PARENT_TASK_ID_ varchar(64),
    DESCRIPTION_ varchar(4000),
    TASK_DEF_KEY_ varchar(255),
    OWNER_ varchar(255),
    ASSIGNEE_ varchar(255),
    DELEGATION_ varchar(64),
    PRIORITY_ integer,
    CREATE_TIME_ datetime,
    LAST_UPDATED_ datetime,
    DUE_DATE_ datetime,
    FOLLOW_UP_DATE_ datetime,
    SUSPENSION_STATE_ integer,
    TENANT_ID_ varchar(64),
    TASK_STATE_ varchar(64),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RU_IDENTITYLINK (
    ID_ varchar(64),
    REV_ integer,
    GROUP_ID_ varchar(255),
    TYPE_ varchar(255),
    USER_ID_ varchar(255),
    TASK_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    TENANT_ID_ varchar(64),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RU_VARIABLE (
    ID_ varchar(64) not null,
    REV_ integer,
    TYPE_ varchar(255) not null,
    NAME_ varchar(255) not null,
    EXECUTION_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    CASE_EXECUTION_ID_ varchar(64),
    CASE_INST_ID_ varchar(64),
    TASK_ID_ varchar(64),
    BATCH_ID_ varchar(64),
    BYTEARRAY_ID_ varchar(64),
    DOUBLE_ double,
    LONG_ bigint,
    TEXT_ varchar(4000),
    TEXT2_ varchar(4000),
    VAR_SCOPE_ varchar(64) not null,
    SEQUENCE_COUNTER_ bigint,
    IS_CONCURRENT_LOCAL_ TINYINT,
    TENANT_ID_ varchar(64),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RU_EVENT_SUBSCR (
    ID_ varchar(64) not null,
    REV_ integer,
    EVENT_TYPE_ varchar(255) not null,
    EVENT_NAME_ varchar(255),
    EXECUTION_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    ACTIVITY_ID_ varchar(255),
    CONFIGURATION_ varchar(255),
    CREATED_ datetime not null,
    TENANT_ID_ varchar(64),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RU_INCIDENT (
  ID_ varchar(64) not null,
  REV_ integer not null,
  INCIDENT_TIMESTAMP_ datetime not null,
  INCIDENT_MSG_ varchar(4000),
  INCIDENT_TYPE_ varchar(255) not null,
  EXECUTION_ID_ varchar(64),
  ACTIVITY_ID_ varchar(255),
  FAILED_ACTIVITY_ID_ varchar(255),
  PROC_INST_ID_ varchar(64),
  PROC_DEF_ID_ varchar(64),
  CAUSE_INCIDENT_ID_ varchar(64),
  ROOT_CAUSE_INCIDENT_ID_ varchar(64),
  CONFIGURATION_ varchar(255),
  TENANT_ID_ varchar(64),
  JOB_DEF_ID_ varchar(64),
  ANNOTATION_ varchar(4000),
  primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RU_AUTHORIZATION (
  ID_ varchar(64) not null,
  REV_ integer not null,
  TYPE_ integer not null,
  GROUP_ID_ varchar(255),
  USER_ID_ varchar(255),
  RESOURCE_TYPE_ integer not null,
  RESOURCE_ID_ varchar(255),
  PERMS_ integer,
  REMOVAL_TIME_ datetime,
  ROOT_PROC_INST_ID_ varchar(64),
  primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RU_FILTER (
  ID_ varchar(64) not null,
  REV_ integer not null,
  RESOURCE_TYPE_ varchar(255) not null,
  NAME_ varchar(255) not null,
  OWNER_ varchar(255),
  QUERY_ LONGTEXT not null,
  PROPERTIES_ LONGTEXT,
  primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RU_METER_LOG (
  ID_ varchar(64) not null,
  NAME_ varchar(64) not null,
  REPORTER_ varchar(255),
  VALUE_ bigint,
  TIMESTAMP_ datetime,
  MILLISECONDS_ bigint DEFAULT 0,
  primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RU_TASK_METER_LOG (
  ID_ varchar(64) not null,
  ASSIGNEE_HASH_ bigint,
  TIMESTAMP_ datetime,
  primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RU_EXT_TASK (
  ID_ varchar(64) not null,
  REV_ integer not null,
  WORKER_ID_ varchar(255),
  TOPIC_NAME_ varchar(255),
  RETRIES_ integer,
  ERROR_MSG_ varchar(4000),
  ERROR_DETAILS_ID_ varchar(64),
  LOCK_EXP_TIME_ datetime NULL,
  CREATE_TIME_ datetime NULL,
  SUSPENSION_STATE_ integer,
  EXECUTION_ID_ varchar(64),
  PROC_INST_ID_ varchar(64),
  PROC_DEF_ID_ varchar(64),
  PROC_DEF_KEY_ varchar(255),
  ACT_ID_ varchar(255),
  ACT_INST_ID_ varchar(64),
  TENANT_ID_ varchar(64),
  PRIORITY_ bigint NOT NULL DEFAULT 0,
  LAST_FAILURE_LOG_ID_ varchar(64),
  primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create table ACT_RU_BATCH (
  ID_ varchar(64) not null,
  REV_ integer not null,
  TYPE_ varchar(255),
  TOTAL_JOBS_ integer,
  JOBS_CREATED_ integer,
  JOBS_PER_SEED_ integer,
  INVOCATIONS_PER_JOB_ integer,
  SEED_JOB_DEF_ID_ varchar(64),
  BATCH_JOB_DEF_ID_ varchar(64),
  MONITOR_JOB_DEF_ID_ varchar(64),
  SUSPENSION_STATE_ integer,
  CONFIGURATION_ varchar(255),
  TENANT_ID_ varchar(64),
  CREATE_USER_ID_ varchar(255),
  START_TIME_ datetime,
  EXEC_START_TIME_ datetime,
  primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

-- create case execution table --
create table ACT_RU_CASE_EXECUTION (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    CASE_INST_ID_ varchar(64),
    SUPER_CASE_EXEC_ varchar(64),
    SUPER_EXEC_ varchar(64),
    BUSINESS_KEY_ varchar(255),
    PARENT_ID_ varchar(64),
    CASE_DEF_ID_ varchar(64),
    ACT_ID_ varchar(255),
    PREV_STATE_ integer,
    CURRENT_STATE_ integer,
    REQUIRED_ boolean,
    TENANT_ID_ varchar(64),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

-- create case sentry part table --

create table ACT_RU_CASE_SENTRY_PART (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    CASE_INST_ID_ varchar(64),
    CASE_EXEC_ID_ varchar(64),
    SENTRY_ID_ varchar(255),
    TYPE_ varchar(255),
    SOURCE_CASE_EXEC_ID_ varchar(64),
    STANDARD_EVENT_ varchar(255),
    SOURCE_ varchar(255),
    VARIABLE_EVENT_ varchar(255),
    VARIABLE_NAME_ varchar(255),
    SATISFIED_ boolean,
    TENANT_ID_ varchar(64),
    primary key (ID_)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;

create index ACT_IDX_EXEC_ROOT_PI on ACT_RU_EXECUTION(ROOT_PROC_INST_ID_);
create index ACT_IDX_EXEC_BUSKEY on ACT_RU_EXECUTION(BUSINESS_KEY_);
create index ACT_IDX_EXEC_TENANT_ID on ACT_RU_EXECUTION(TENANT_ID_);
create index ACT_IDX_TASK_CREATE on ACT_RU_TASK(CREATE_TIME_);
create index ACT_IDX_TASK_LAST_UPDATED on ACT_RU_TASK(LAST_UPDATED_);
create index ACT_IDX_TASK_ASSIGNEE on ACT_RU_TASK(ASSIGNEE_);
create index ACT_IDX_TASK_OWNER on ACT_RU_TASK(OWNER_);
create index ACT_IDX_TASK_TENANT_ID on ACT_RU_TASK(TENANT_ID_);
create index ACT_IDX_IDENT_LNK_USER on ACT_RU_IDENTITYLINK(USER_ID_);
create index ACT_IDX_IDENT_LNK_GROUP on ACT_RU_IDENTITYLINK(GROUP_ID_);
create index ACT_IDX_EVENT_SUBSCR_CONFIG_ on ACT_RU_EVENT_SUBSCR(CONFIGURATION_);
create index ACT_IDX_EVENT_SUBSCR_TENANT_ID on ACT_RU_EVENT_SUBSCR(TENANT_ID_);

create index ACT_IDX_VARIABLE_TASK_ID on ACT_RU_VARIABLE(TASK_ID_);
create index ACT_IDX_VARIABLE_TENANT_ID on ACT_RU_VARIABLE(TENANT_ID_);
create index ACT_IDX_VARIABLE_TASK_NAME_TYPE on ACT_RU_VARIABLE(TASK_ID_, NAME_, TYPE_);

create index ACT_IDX_ATHRZ_PROCEDEF on ACT_RU_IDENTITYLINK(PROC_DEF_ID_);
create index ACT_IDX_INC_CONFIGURATION on ACT_RU_INCIDENT(CONFIGURATION_);
create index ACT_IDX_INC_TENANT_ID on ACT_RU_INCIDENT(TENANT_ID_);
-- CAM-5914
create index ACT_IDX_JOB_EXECUTION_ID on ACT_RU_JOB(EXECUTION_ID_);
-- this index needs to be limited in mysql see CAM-6938
create index ACT_IDX_JOB_HANDLER on ACT_RU_JOB(HANDLER_TYPE_(100),HANDLER_CFG_(155));
create index ACT_IDX_JOB_PROCINST on ACT_RU_JOB(PROCESS_INSTANCE_ID_);
create index ACT_IDX_JOB_ROOT_PROCINST on ACT_RU_JOB(ROOT_PROC_INST_ID_);
create index ACT_IDX_JOB_TENANT_ID on ACT_RU_JOB(TENANT_ID_);
create index ACT_IDX_JOBDEF_TENANT_ID on ACT_RU_JOBDEF(TENANT_ID_);

-- new metric milliseconds column
CREATE INDEX ACT_IDX_METER_LOG_MS ON ACT_RU_METER_LOG(MILLISECONDS_);
CREATE INDEX ACT_IDX_METER_LOG_NAME_MS ON ACT_RU_METER_LOG(NAME_, MILLISECONDS_);
CREATE INDEX ACT_IDX_METER_LOG_REPORT ON ACT_RU_METER_LOG(NAME_, REPORTER_, MILLISECONDS_);

-- old metric timestamp column
CREATE INDEX ACT_IDX_METER_LOG_TIME ON ACT_RU_METER_LOG(TIMESTAMP_);
CREATE INDEX ACT_IDX_METER_LOG ON ACT_RU_METER_LOG(NAME_, TIMESTAMP_);

-- task metric timestamp column
CREATE INDEX ACT_IDX_TASK_METER_LOG_TIME ON ACT_RU_TASK_METER_LOG(TIMESTAMP_);

create index ACT_IDX_EXT_TASK_TOPIC on ACT_RU_EXT_TASK(TOPIC_NAME_);
create index ACT_IDX_EXT_TASK_TENANT_ID on ACT_RU_EXT_TASK(TENANT_ID_);
create index ACT_IDX_EXT_TASK_PRIORITY ON ACT_RU_EXT_TASK(PRIORITY_);
create index ACT_IDX_EXT_TASK_ERR_DETAILS ON ACT_RU_EXT_TASK(ERROR_DETAILS_ID_);
create index ACT_IDX_AUTH_GROUP_ID on ACT_RU_AUTHORIZATION(GROUP_ID_);
create index ACT_IDX_JOB_JOB_DEF_ID on ACT_RU_JOB(JOB_DEF_ID_);

create index ACT_IDX_INC_JOB_DEF on ACT_RU_INCIDENT(JOB_DEF_ID_);

create index ACT_IDX_BATCH_SEED_JOB_DEF ON ACT_RU_BATCH(SEED_JOB_DEF_ID_);

create index ACT_IDX_BATCH_MONITOR_JOB_DEF ON ACT_RU_BATCH(MONITOR_JOB_DEF_ID_);

create index ACT_IDX_BATCH_JOB_DEF ON ACT_RU_BATCH(BATCH_JOB_DEF_ID_);

create index ACT_IDX_BATCH_ID ON ACT_RU_VARIABLE(BATCH_ID_);

-- indexes for deadlock problems - https://app.camunda.com/jira/browse/CAM-2567 --
create index ACT_IDX_INC_CAUSEINCID on ACT_RU_INCIDENT(CAUSE_INCIDENT_ID_);
create index ACT_IDX_INC_EXID on ACT_RU_INCIDENT(EXECUTION_ID_);
create index ACT_IDX_INC_PROCDEFID on ACT_RU_INCIDENT(PROC_DEF_ID_);
create index ACT_IDX_INC_PROCINSTID on ACT_RU_INCIDENT(PROC_INST_ID_);
create index ACT_IDX_INC_ROOTCAUSEINCID on ACT_RU_INCIDENT(ROOT_CAUSE_INCIDENT_ID_);
-- index for deadlock problem - https://app.camunda.com/jira/browse/CAM-4440 --
create index ACT_IDX_AUTH_RESOURCE_ID on ACT_RU_AUTHORIZATION(RESOURCE_ID_);
-- index to prevent deadlock on fk constraint - https://app.camunda.com/jira/browse/CAM-5440 --
create index ACT_IDX_EXT_TASK_EXEC on ACT_RU_EXT_TASK(EXECUTION_ID_);

-- indexes to improve deployment
create index ACT_IDX_JOBDEF_PROC_DEF_ID ON ACT_RU_JOBDEF(PROC_DEF_ID_);
create index ACT_IDX_JOB_HANDLER_TYPE ON ACT_RU_JOB(HANDLER_TYPE_);
create index ACT_IDX_EVENT_SUBSCR_EVT_NAME ON ACT_RU_EVENT_SUBSCR(EVENT_NAME_);

-- indices for history cleanup: https://jira.camunda.com/browse/CAM-11616
create index ACT_IDX_AUTH_ROOT_PI on ACT_RU_AUTHORIZATION(ROOT_PROC_INST_ID_);
create index ACT_IDX_AUTH_RM_TIME on ACT_RU_AUTHORIZATION(REMOVAL_TIME_);

-- create index on business key --
create index ACT_IDX_CASE_EXEC_BUSKEY on ACT_RU_CASE_EXECUTION(BUSINESS_KEY_);

-- https://app.camunda.com/jira/browse/CAM-9165
create index ACT_IDX_CASE_EXE_CASE_INST on ACT_RU_CASE_EXECUTION(CASE_INST_ID_);

create index ACT_IDX_CASE_EXEC_TENANT_ID on ACT_RU_CASE_EXECUTION(TENANT_ID_);