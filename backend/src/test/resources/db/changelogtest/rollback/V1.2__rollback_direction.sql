--liquibase formatted sql
--changeset Shakhzodbek:1.2_rollback


DROP TABLE IF EXISTS direction;
DROP SEQUENCE IF EXISTS direction_seq;