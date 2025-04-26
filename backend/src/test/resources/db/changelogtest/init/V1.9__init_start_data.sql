--liquibase formatted sql
--changeset Shakhzodbek:1.9


INSERT INTO direction(transcript, number, faculty_name)
VALUES ('Информатика и вычислительная техника', '09.03.01', 'ФПИиКТ'),
       ('Программная инженерия', '09.03.04', 'ФПИиКТ');

INSERT INTO organization(name)
VALUES ('ИТМО');