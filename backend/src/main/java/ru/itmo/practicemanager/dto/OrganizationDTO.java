package ru.itmo.practicemanager.dto;

import lombok.Data;

@Data
public class OrganizationDTO {
    Long inn;
    String name;
    String direction;
    String location;
}
