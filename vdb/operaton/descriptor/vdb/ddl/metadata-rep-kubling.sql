create table ACT_RE_CASE_DEF (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    CATEGORY_ varchar(255),
    NAME_ varchar(255),
    KEY_ varchar(255) NOT NULL,
    VERSION_ integer NOT NULL,
    DEPLOYMENT_ID_ varchar(64),
    RESOURCE_NAME_ varchar(4000),
    DGRM_RESOURCE_NAME_ varchar(4000),
    TENANT_ID_ varchar(64),
    HISTORY_TTL_ integer,
    primary key (ID_)
);

create index ACT_IDX_CASE_DEF_TENANT_ID on ACT_RE_CASE_DEF(TENANT_ID_);

-- create decision definition table --
create table ACT_RE_DECISION_DEF (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    CATEGORY_ varchar(255),
    NAME_ varchar(255),
    KEY_ varchar(255) NOT NULL,
    VERSION_ integer NOT NULL,
    DEPLOYMENT_ID_ varchar(64),
    RESOURCE_NAME_ varchar(4000),
    DGRM_RESOURCE_NAME_ varchar(4000),
    DEC_REQ_ID_ varchar(64),
    DEC_REQ_KEY_ varchar(255),
    TENANT_ID_ varchar(64),
    HISTORY_TTL_ integer,
    VERSION_TAG_ varchar(64),
    primary key (ID_)
);

-- create decision requirements definition table --
create table ACT_RE_DECISION_REQ_DEF (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    CATEGORY_ varchar(255),
    NAME_ varchar(255),
    KEY_ varchar(255) NOT NULL,
    VERSION_ integer NOT NULL,
    DEPLOYMENT_ID_ varchar(64),
    RESOURCE_NAME_ varchar(4000),
    DGRM_RESOURCE_NAME_ varchar(4000),
    TENANT_ID_ varchar(64),
    primary key (ID_)
);

alter table ACT_RE_DECISION_DEF
    add constraint ACT_FK_DEC_REQ
    foreign key (DEC_REQ_ID_)
    references ACT_RE_DECISION_REQ_DEF(ID_);

create index ACT_IDX_DEC_DEF_TENANT_ID on ACT_RE_DECISION_DEF(TENANT_ID_);
create index ACT_IDX_DEC_DEF_REQ_ID on ACT_RE_DECISION_DEF(DEC_REQ_ID_);
create index ACT_IDX_DEC_REQ_DEF_TENANT_ID on ACT_RE_DECISION_REQ_DEF(TENANT_ID_);

create table ACT_RE_DEPLOYMENT (
    ID_ varchar(64),
    NAME_ varchar(255),
    DEPLOY_TIME_ timestamp,
    SOURCE_ varchar(255),
    TENANT_ID_ varchar(64),
    primary key (ID_)
);

create table ACT_RE_PROCDEF (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    CATEGORY_ varchar(255),
    NAME_ varchar(255),
    KEY_ varchar(255) NOT NULL,
    VERSION_ integer NOT NULL,
    DEPLOYMENT_ID_ varchar(64),
    RESOURCE_NAME_ varchar(4000),
    DGRM_RESOURCE_NAME_ varchar(4000),
    HAS_START_FORM_KEY_ bit,
    SUSPENSION_STATE_ integer,
    TENANT_ID_ varchar(64),
    VERSION_TAG_ varchar(64),
    HISTORY_TTL_ integer,
    STARTABLE_ boolean NOT NULL default TRUE,
    primary key (ID_)
);

create table ACT_RE_CAMFORMDEF (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    KEY_ varchar(255) NOT NULL,
    VERSION_ integer NOT NULL,
    DEPLOYMENT_ID_ varchar(64),
    RESOURCE_NAME_ varchar(4000),
    TENANT_ID_ varchar(64),
    primary key (ID_)
);

create index ACT_IDX_DEPLOYMENT_NAME on ACT_RE_DEPLOYMENT(NAME_);
create index ACT_IDX_DEPLOYMENT_TENANT_ID on ACT_RE_DEPLOYMENT(TENANT_ID_);
create index ACT_IDX_PROCDEF_DEPLOYMENT_ID ON ACT_RE_PROCDEF(DEPLOYMENT_ID_);
create index ACT_IDX_PROCDEF_TENANT_ID ON ACT_RE_PROCDEF(TENANT_ID_);
create index ACT_IDX_PROCDEF_VER_TAG ON ACT_RE_PROCDEF(VERSION_TAG_);

create table ACT_GE_PROPERTY (
    NAME_ varchar(64),
    VALUE_ varchar(300),
    REV_ integer,
    primary key (NAME_)
);

insert into ACT_GE_PROPERTY
values ('schema.version', 'fox', 1);

insert into ACT_GE_PROPERTY
values ('schema.history', 'create(fox)', 1);

insert into ACT_GE_PROPERTY
values ('next.dbid', '1', 1);

insert into ACT_GE_PROPERTY
values ('deployment.lock', '0', 1);

insert into ACT_GE_PROPERTY
values ('history.cleanup.job.lock', '0', 1);

insert into ACT_GE_PROPERTY
values ('startup.lock', '0', 1);

insert into ACT_GE_PROPERTY
values ('installationId.lock', '0', 1);

create table ACT_GE_BYTEARRAY (
    ID_ varchar(64),
    REV_ integer,
    NAME_ varchar(255),
    DEPLOYMENT_ID_ varchar(64),
    BYTES_ blob,
    GENERATED_ bit,
    TENANT_ID_ varchar(64),
    TYPE_ integer,
    CREATE_TIME_ timestamp,
    ROOT_PROC_INST_ID_ varchar(64),
    REMOVAL_TIME_ timestamp,
    primary key (ID_)
);

create table ACT_GE_SCHEMA_LOG (
    ID_ varchar(64),
    TIMESTAMP_ timestamp,
    VERSION_ varchar(255),
    primary key (ID_)
);

insert into ACT_GE_SCHEMA_LOG
values ('0', CURRENT_TIMESTAMP, '7.24.0');

create index ACT_IDX_BYTEARRAY_ROOT_PI on ACT_GE_BYTEARRAY(ROOT_PROC_INST_ID_);
create index ACT_IDX_BYTEARRAY_RM_TIME on ACT_GE_BYTEARRAY(REMOVAL_TIME_);
create index ACT_IDX_BYTEARRAY_NAME on ACT_GE_BYTEARRAY(NAME_);

create table ACT_ID_GROUP (
    ID_ varchar(64),
    REV_ integer,
    NAME_ varchar(255),
    TYPE_ varchar(255),
    primary key (ID_)
);

create table ACT_ID_MEMBERSHIP (
    USER_ID_ varchar(64),
    GROUP_ID_ varchar(64),
    primary key (USER_ID_, GROUP_ID_)
);

create table ACT_ID_USER (
    ID_ varchar(64),
    REV_ integer,
    FIRST_ varchar(255),
    LAST_ varchar(255),
    EMAIL_ varchar(255),
    PWD_ varchar(255),
    SALT_ varchar(255),
    LOCK_EXP_TIME_ timestamp,
    ATTEMPTS_ integer,
    PICTURE_ID_ varchar(64),
    primary key (ID_)
);

create table ACT_ID_INFO (
    ID_ varchar(64),
    REV_ integer,
    USER_ID_ varchar(64),
    TYPE_ varchar(64),
    KEY_ varchar(255),
    VALUE_ varchar(255),
    PASSWORD_ longvarbinary,
    PARENT_ID_ varchar(255),
    primary key (ID_)
);

create table ACT_ID_TENANT (
    ID_ varchar(64),
    REV_ integer,
    NAME_ varchar(255),
    primary key (ID_)
);

create table ACT_ID_TENANT_MEMBER (
    ID_ varchar(64) not null,
    TENANT_ID_ varchar(64) not null,
    USER_ID_ varchar(64),
    GROUP_ID_ varchar(64),
    primary key (ID_)
);