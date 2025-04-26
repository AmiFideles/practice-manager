--liquibase formatted sql
--changeset Shakhzodbek:1.1


CREATE SEQUENCE user_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO CYCLE;

CREATE TABLE users (
    id BIGINT PRIMARY KEY DEFAULT nextval('user_seq'),
    telegram_id BIGINT UNIQUE,
    telegram_username VARCHAR(255),
    role VARCHAR(50) NOT NULL
);

CREATE INDEX idx_user_telegram ON users(telegram_id);