package ru.itmo.practicemanager.dto;

import lombok.Data;

@Data
public class SupervisorDTO {
    String surname;
    String name;
    String patronymic;
    String mail;
    String phone;
    Long organizationId;
}
