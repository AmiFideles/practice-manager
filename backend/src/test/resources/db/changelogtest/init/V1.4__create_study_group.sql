--liquibase formatted sql
--changeset Shakhzodbek:1.4


CREATE SEQUENCE study_group_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO CYCLE;

CREATE TABLE study_group (
    id BIGINT PRIMARY KEY DEFAULT nextval('study_group_seq'),
    number VARCHAR(50),
    direction_id BIGINT REFERENCES direction(id)
);

