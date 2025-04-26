--liquibase formatted sql
--changeset Shakhzodbek:1.8_rollback


DROP TABLE IF EXISTS apply;
DROP SEQUENCE IF EXISTS apply_seq;