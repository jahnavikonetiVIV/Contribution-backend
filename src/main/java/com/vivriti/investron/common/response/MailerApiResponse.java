package com.vivriti.investron.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailerApiResponse {
    private boolean success;
    private String message;

    public boolean isSuccess() {
        return success;
    }
}
