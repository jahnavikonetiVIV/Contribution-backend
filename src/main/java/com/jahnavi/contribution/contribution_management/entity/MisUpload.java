package com.jahnavi.contribution.contribution_management.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mis_upload")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MisUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_received", nullable = false)
    private LocalDate dateReceived;

    @Column(name = "sender_email", nullable = false)
    private String senderEmail;

    @Column(name = "fund_name", nullable = false)
    private String fundName;

    @Column(name = "attachment_name", nullable = false)
    private String attachmentName;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "error_reason", length = 500)
    private String errorReason;

    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name="mis_file_id")
    private Long misFileId;

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }
}
