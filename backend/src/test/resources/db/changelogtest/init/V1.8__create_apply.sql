--liquibase formatted sql
--changeset Shakhzodbek:1.8


CREATE SEQUENCE apply_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO CYCLE;

CREATE TABLE apply
(
    id              BIGINT PRIMARY KEY DEFAULT nextval('apply_seq'),
    status          VARCHAR(50),
    check_status    VARCHAR(50),
    supervisor_id   BIGINT REFERENCES supervisor (id),
    student_id      BIGINT UNIQUE REFERENCES student (id),
    organization_id BIGINT REFERENCES organization (id),
    practice_type   VARCHAR(50)
);