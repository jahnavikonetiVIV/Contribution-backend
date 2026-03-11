package com.jahnavi.contribution.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;

@Slf4j
@Configuration
public class AwsS3Config {

    @Bean
    public S3Client s3Client() {
        log.info("AWS S3 (SDK v2) configured");
        return S3Client.builder()
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .region(Region.US_EAST_1)
                .build();
    }
    @Bean
    public SqsClient sqsClient() {
        log.info("AWS SQS (SDK v2) configured");
        return SqsClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .region(Region.US_EAST_1)
                .build();
    }


}
