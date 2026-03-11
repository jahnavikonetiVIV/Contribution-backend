package com.jahnavi.contribution.contribution_management.service.impl;

import com.vivriti.investron.common.exception.CoreException;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class EcollectMisReportServiceImpl {
    private final S3Client s3Client;

    private final MisProcessingServiceImpl misProcessingServiceImpl;


    public void processEmailFromS3(String bucket, String key) throws IOException {

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> s3Stream =
                     s3Client.getObject(request)) {

            MimeMessage message = parseEmlFromStream(s3Stream);
            String senderEmail = getSenderFromMessage(message);
            List<MultipartFile> attachments = extractAttachmentsFromMessage(message);

            if (attachments.isEmpty()) {
                log.warn("No attachments found for {}", key);
                return;
            }

            for (MultipartFile file : attachments) {
                if (!isSupportedMisFile(file.getOriginalFilename())) {
                    log.debug("Skipping unsupported attachment (not xlsx/xls/csv): {}", file.getOriginalFilename());
                    continue;
                }
                misProcessingServiceImpl.processMisReportFile(file, null, key, senderEmail);
            }

        } catch (Exception e) {
            log.error("Failed processing email from S3", e);
            throw e;
        }
    }
    private MimeMessage parseEmlFromStream(InputStream stream) {
        try {
            Session session = Session.getDefaultInstance(new Properties());
            return new MimeMessage(session, stream);
        } catch (Exception e) {
            throw new CoreException(HttpStatus.BAD_REQUEST.value(), "Failed to parse email", e);
        }
    }

    private String getSenderFromMessage(MimeMessage message) {
        try {
            Address[] from = message.getFrom();
            if (from != null && from.length > 0 && from[0] instanceof InternetAddress ia) {
                String address = ia.getAddress();
                return (address != null && !address.isBlank()) ? address : null;
            }
            if (from != null && from.length > 0) {
                return from[0].toString();
            }
        } catch (MessagingException e) {
            log.warn("Could not read From header from .eml: {}", e.getMessage());
        }
        return null;
    }

    private List<MultipartFile> extractAttachmentsFromMessage(MimeMessage message) {
        List<MultipartFile> attachments = new ArrayList<>();
        try {
            Object content = message.getContent();
            if (content instanceof MimeMultipart multipart) {
                extractFromMultipart(multipart, attachments);
            }
        } catch (Exception e) {
            throw new CoreException(HttpStatus.BAD_REQUEST.value(), "Failed to extract attachments from email", e);
        }
        return attachments;
    }

    /** Only .xlsx, .xls, .csv are parsed as MIS reports; other attachments are skipped. */
    private static boolean isSupportedMisFile(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }
        String lower = fileName.toLowerCase();
        return lower.endsWith(".xlsx") || lower.endsWith(".xls") || lower.endsWith(".csv");
    }

    private void extractFromMultipart(
            MimeMultipart multipart,
            List<MultipartFile> attachments
    ) throws MessagingException, IOException {

        for (int i = 0; i < multipart.getCount(); i++) {

            BodyPart part = multipart.getBodyPart(i);

            if (part.getContent() instanceof MimeMultipart nested) {
                extractFromMultipart(nested, attachments);
                continue;
            }

            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())
                    || part.getFileName() != null) {

                byte[] bytes =
                        part.getInputStream().readAllBytes();

                attachments.add(
                        new InMemoryMultipartFile(
                                part.getFileName(),
                                part.getContentType(),
                                bytes
                        )
                );

                log.info("Attachment extracted: {} ({} bytes)",
                        part.getFileName(), bytes.length);
            }
        }
    }

    // =========================
    // IN-MEMORY MULTIPART FILE
    // =========================
    private static class InMemoryMultipartFile implements MultipartFile {

        private final String name;
        private final String contentType;
        private final byte[] content;

        InMemoryMultipartFile(
                String name,
                String contentType,
                byte[] content
        ) {
            this.name = name;
            this.contentType = contentType;
            this.content = content;
        }

        @NotNull
        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return name;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @NotNull
        @Override
        public byte[] getBytes() {
            return content;
        }

        @NotNull
        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(@NotNull java.io.File dest) {
            throw new UnsupportedOperationException();
        }
    }
}
