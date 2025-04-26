--liquibase formatted sql
--changeset Shakhzodbek:1.9_rollback

-- Удаляем добавленные записи в organization (по имени)
DELETE FROM organization WHERE name = 'ИТМО';

-- Удаляем добавленные записи в direction (по номеру)
DELETE FROM direction WHERE number IN ('09.03.01', '09.03.04');