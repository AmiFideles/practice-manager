--liquibase formatted sql
--changeset Shakhzodbek:1.1_rollback


DROP INDEX IF EXISTS idx_user_telegram;
DROP TABLE IF EXISTS users;
DROP SEQUENCE IF EXISTS user_seq;
