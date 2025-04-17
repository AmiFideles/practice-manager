package ru.itmo.practicemanager.service.pdf;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApplicationDocxData {

    private String fullName;
    private String faculty;
    private String programCode;
    private String programName;
    private String group;
    private String practiceDates;
    private String format;
    private String organization;
    private String organizationAddress;
    private String representativePosition;
    private String representativeFullName;

}
