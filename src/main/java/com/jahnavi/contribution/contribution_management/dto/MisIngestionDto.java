package com.jahnavi.contribution.contribution_management.dto;


import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class MisIngestionDto {

    private String senderEmail;
    private String fundName;
    private String attachment;
    private String status;
    private String errorReason;
    private LocalDate dateRecieved;
    private Long fileId;

}
