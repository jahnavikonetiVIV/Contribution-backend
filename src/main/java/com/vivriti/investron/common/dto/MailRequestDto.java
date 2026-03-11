package com.vivriti.investron.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailRequestDto {
    private String from;
    private String subject;
    private String body;
    private List<String> to;
    private String templateName;
    private String platform;
    private String logoName;
    private boolean isHtml;
}
