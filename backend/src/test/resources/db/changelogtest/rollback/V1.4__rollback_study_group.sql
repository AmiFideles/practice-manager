--liquibase formatted sql
--changeset Shakhzodbek:1.4_rollback


DROP TABLE IF EXISTS study_group;
DROP SEQUENCE IF EXISTS study_group_seq;