package com.ifortex.internship.kycservice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.ifortex.internship.kycservice.dto.response.UploadedFileInfoDto;
import com.ifortex.internship.kycservice.model.constant.DocumentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID documentId = UUID.randomUUID();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private String filename;

    @CreationTimestamp
    @Column(nullable = false)
    private Instant uploadedAt;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private Application application;

    public Document(DocumentType documentType, UploadedFileInfoDto uploadedFileInfo) {
        this.documentType = documentType;
        this.fileUrl = uploadedFileInfo.getFileUrl();
        this.filename = uploadedFileInfo.getFileName();
    }
}