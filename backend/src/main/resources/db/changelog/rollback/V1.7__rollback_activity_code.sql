--liquibase formatted sql
--changeset Shakhzodbek:1.7_rollback


DROP TABLE IF EXISTS activity_code;
DROP SEQUENCE IF EXISTS activity_code_seq;