package com.vivriti.investron.common.feign;

import com.vivriti.investron.common.dto.MailRequestDto;
import com.vivriti.investron.common.response.MailerApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Stub implementation for notification/mail sending.
 * Replace with actual Feign client when notification service is configured.
 */
@Slf4j
@Component
public class NotificationFeignClientHelper {

    public MailerApiResponse sendMail(MailRequestDto mailRequest) {
        log.warn("NotificationFeignClientHelper: Mail sending not configured. Subject: {}", 
                mailRequest != null ? mailRequest.getSubject() : "null");
        return MailerApiResponse.builder()
                .success(false)
                .message("Mail service not configured")
                .build();
    }
}
