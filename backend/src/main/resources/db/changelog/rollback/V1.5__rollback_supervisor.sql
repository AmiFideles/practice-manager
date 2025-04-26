--liquibase formatted sql
--changeset Shakhzodbek:1.5_rollback


DROP TABLE IF EXISTS supervisor;
DROP SEQUENCE IF EXISTS supervisor_seq;