--liquibase formatted sql
--changeset Shakhzodbek:1.2


CREATE SEQUENCE direction_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO CYCLE;

CREATE TABLE direction (
    id BIGINT PRIMARY KEY DEFAULT nextval('direction_seq'),
    transcript VARCHAR(255),
    number VARCHAR(50),
    faculty_name VARCHAR(255)
);

