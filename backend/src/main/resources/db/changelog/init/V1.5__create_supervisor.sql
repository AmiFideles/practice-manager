--liquibase formatted sql
--changeset Shakhzodbek:1.5


CREATE SEQUENCE supervisor_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO CYCLE;

CREATE TABLE supervisor (
    id BIGINT PRIMARY KEY DEFAULT nextval('supervisor_seq'),
    name VARCHAR(255),
    mail VARCHAR(255),
    phone VARCHAR(50),
    organization_id BIGINT REFERENCES organization(id)
);

