--liquibase formatted sql
--changeset Shakhzodbek:1.3_rollback


DROP TABLE IF EXISTS organization;
DROP SEQUENCE IF EXISTS organization_seq;
DROP SEQUENCE IF EXISTS idx_organization_inn;
