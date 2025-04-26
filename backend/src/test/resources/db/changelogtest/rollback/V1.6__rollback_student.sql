--liquibase formatted sql
--changeset Shakhzodbek:1.6_rollback


DROP INDEX IF EXISTS idx_student_isu;
DROP INDEX IF EXISTS idx_student_group;
DROP TABLE IF EXISTS student;
DROP SEQUENCE IF EXISTS student_seq;