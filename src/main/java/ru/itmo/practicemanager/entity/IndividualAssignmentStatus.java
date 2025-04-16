package ru.itmo.practicemanager.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IndividualAssignmentStatus {
    CREATED("Формирование ИЗ"),
    PENDING_SUPERVISOR("На согласовании у внешнего руководителя"),
    APPROVED("ИЗ утверждено");

    private final String description;
}