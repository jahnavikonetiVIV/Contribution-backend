package com.vivriti.investron.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    private DmsConfig dms = new DmsConfig();
    private NotificationConfig notification = new NotificationConfig();

    @Getter
    @Setter
    public static class DmsConfig {
        private String investorFolderName = "investor";
        private String bucketName = "default-bucket";
    }

    @Getter
    @Setter
    public static class NotificationConfig {
        private String platform = "INVESTRON";
    }
}
