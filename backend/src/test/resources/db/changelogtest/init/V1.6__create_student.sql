--liquibase formatted sql
--changeset Shakhzodbek:1.6


CREATE SEQUENCE student_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO CYCLE;

CREATE TABLE student (
    id BIGINT PRIMARY KEY DEFAULT nextval('student_seq'),
    full_name VARCHAR(255) NOT NULL,
    isu_number VARCHAR(20) UNIQUE NOT NULL,
    group_id BIGINT REFERENCES study_group(id),
    approval_status VARCHAR(50),
    is_statement_delivered BOOLEAN,
    is_statement_signed BOOLEAN,
    is_statement_scanned BOOLEAN,
    is_notification_sent BOOLEAN,
    user_id BIGINT UNIQUE REFERENCES users(id),
    comment TEXT,
    individual_assignment_status VARCHAR(50)
);

CREATE INDEX idx_student_isu ON student(isu_number);
CREATE INDEX idx_student_group ON student(group_id);