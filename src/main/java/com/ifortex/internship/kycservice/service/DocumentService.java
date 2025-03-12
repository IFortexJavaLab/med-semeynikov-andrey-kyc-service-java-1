package com.ifortex.internship.kycservice.service;

import com.ifortex.internship.kycservice.dto.response.FileDownloadResponse;
import com.ifortex.internship.kycservice.model.Document;
import com.ifortex.internship.kycservice.repository.DocumentRepository;
import com.ifortex.internship.medstarter.exception.custom.EntityNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    DocumentRepository documentRepository;
    FileStorageService fileStorageService;

    public FileDownloadResponse downloadDocument(UUID documentId) {
        Document document = documentRepository.findByDocumentId(documentId)
            .orElseThrow(() -> new EntityNotFoundException("Document not found with id: " + documentId));

        byte[] fileData = fileStorageService.downloadFile(document.getFilename());

        return new FileDownloadResponse(fileData, document.getFilename());

    }
}
