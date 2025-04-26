--liquibase formatted sql
--changeset Shakhzodbek:1.7


CREATE SEQUENCE activity_code_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO CYCLE;

CREATE TABLE activity_code (
    id BIGINT PRIMARY KEY DEFAULT nextval('activity_code_seq'),
    code VARCHAR(255) NOT NULL
);
