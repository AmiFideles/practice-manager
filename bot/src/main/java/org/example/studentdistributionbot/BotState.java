package org.example.studentdistributionbot;

public enum BotState {
    WAITING_FOR_ISU_NUMBER,
    WAITING_FOR_FULL_NAME,

    WAITING_FOR_PRACTICE_APPLICATION,
    WAITING_FOR_APPROVE_FILE_LOADING,

    WAITING_FOR_APPROVE_EXCEL,
    WAITING_FOR_GROUP_NAME_AND_STATUS,
    WAITING_FOR_ISU_NUMBER_AND_STATUS,
    WAITING_FOR_ISU_NUMBER_FOR_STUDENT_STATUS,
    WAITING_FOR_ISU_NUMBER_FOR_GET_STUDENT_STATUS,
    IDLE;
}
