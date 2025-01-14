create table test_case
(
    id                   uuid           NOT NULL PRIMARY KEY,
    jira_project_key     VARCHAR        NOT NULL,
    name                 VARCHAR UNIQUE NOT NULL,
    zepyhr_test_case_key VARCHAR        NOT NULL
);

create table test_report
(
    id     uuid    NOT NULL PRIMARY KEY,
    detail VARCHAR NOT NULL
);

create table test_run
(
    id             uuid                     NOT NULL PRIMARY KEY,
    test_case_id   uuid                     NOT NULL REFERENCES test_case (id),
    test_report_id uuid REFERENCES test_report (id),
    ended_at       TIMESTAMP WITH TIME ZONE,
    environment    VARCHAR                  NOT NULL,
    started_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    test_state     VARCHAR                  NOT NULL
);

create
index test_run_test_case_id on test_run (test_case_id);
create
index test_run_test_report_id on test_run (test_report_id);

create table test_run_parameters
(
    id    uuid    NOT NULL REFERENCES test_run (id),
    name  VARCHAR NOT NULL,
    value VARCHAR NOT NULL,
    UNIQUE (id, name)
);

create
index test_run_parameters_id on test_run_parameters (id);
create
index test_run_parameters_name on test_run_parameters (name);

create table test_log
(
    id          uuid                     NOT NULL PRIMARY KEY,
    test_run_id uuid                     NOT NULL REFERENCES test_run (id),
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    log_level   VARCHAR                  NOT NULL,
    message     VARCHAR                  NOT NULL,
    source      VARCHAR                  NOT NULL
);

create
index test_log_test_run_id on test_log (test_run_id);

create table test_result
(
    id              uuid    NOT NULL PRIMARY KEY,
    test_report_id  uuid    NOT NULL REFERENCES test_report (id),
    name            VARCHAR NOT NULL,
    detail          VARCHAR NOT NULL,
    test_conclusion VARCHAR NOT NULL
);

create
index test_result_test_report_id on test_result (test_report_id);
