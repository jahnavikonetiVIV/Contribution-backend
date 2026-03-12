package com.jahnavi.contribution.contribution_management.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jahnavi.contribution.contribution_management.dto.S3EventNotificationDto;
import com.jahnavi.contribution.exception.CoreException;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.annotation.SqsListenerAcknowledgementMode;
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        name = "ecollect.sqs.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class EcollectSqsWorkerServiceImpl {

    private final ObjectMapper objectMapper;
    private final EcollectMisReportServiceImpl misReportService;

    @SqsListener(
            value = "${ecollect.sqs.queue.name}",
            acknowledgementMode = SqsListenerAcknowledgementMode.MANUAL
    )
    public void consume(String body, Acknowledgement acknowledgement) throws IOException {

        try {
            log.info("RAW S3 EVENT RECEIVED");
            log.info("received s3 body: {}", body);

            S3EventNotificationDto event =
                    objectMapper.readValue(body, S3EventNotificationDto.class);

            List<S3EventNotificationDto.Record> records = event.getRecords() != null ? event.getRecords() : List.of();
            for (S3EventNotificationDto.Record rec: records) {

                String bucket =
                        rec.getS3().getBucket().getName();

                String key =
                        URLDecoder.decode(
                                rec.getS3().getObject().getKey(),
                                StandardCharsets.UTF_8
                        );

                log.info("Processing S3 object | bucket={} | key={}", bucket, key);

                misReportService.processEmailFromS3(bucket, key);
            }

            acknowledgement.acknowledge();
            log.info("Acknowledgement sent. Message acknowledged (deleted from queue).");

        } catch (CoreException e) {
            log.warn("MIS parse error (batch marked Parse Error, alert sent). Acknowledging message to avoid retry: {}", e.getMessage());
            acknowledgement.acknowledge();
        } catch (Exception e) {
            Throwable root = e;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            log.error("S3 event processing failed. Root cause: {} - {}", root.getClass().getSimpleName(), root.getMessage(), e);
            throw e; // do not acknowledge -> message will reappear after visibility timeout / DLQ
        }
    }
}
