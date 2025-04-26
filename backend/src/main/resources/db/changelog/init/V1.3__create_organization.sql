--liquibase formatted sql
--changeset Shakhzodbek:1.3


CREATE SEQUENCE organization_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO CYCLE;

CREATE TABLE organization (
    id BIGINT PRIMARY KEY DEFAULT nextval('organization_seq'),
    inn BIGINT,
    name VARCHAR(255),
    location VARCHAR(255)
);

CREATE INDEX idx_organization_inn ON organization(inn);