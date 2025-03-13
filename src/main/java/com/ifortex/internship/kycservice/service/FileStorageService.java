package com.ifortex.internship.kycservice.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.ifortex.internship.kycservice.dto.response.UploadedFileInfoDto;
import com.ifortex.internship.medstarter.exception.custom.EntityNotFoundException;
import com.ifortex.internship.medstarter.exception.custom.InternalServiceException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    BlobServiceClient blobServiceClient;

    @Value("${spring.cloud.azure.storage.blob.container-name}")
    String containerName;

    public UploadedFileInfoDto uploadFile(MultipartFile file) {

        log.debug(containerName);

        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        if (!containerClient.exists()) {
            containerClient.create();
        }
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        BlobClient blobClient = containerClient.getBlobClient(fileName);
        try {
            blobClient.upload(file.getInputStream(), file.getSize(), true);
        } catch (IOException e) {
            log.error("Error uploading file {}: {}", file.getOriginalFilename(), e.getMessage());
            throw new InternalServiceException(
                "Failed to upload file: unable to read the file. Please ensure you have uploaded a valid image (e.g., JPG or PNG) and try again later.");
        }
        return new UploadedFileInfoDto(blobClient.getBlobUrl(), fileName);

    }

    private String generateUniqueFileName(String originalFileName) {

        String safeFileName = (originalFileName != null) ?
            Paths.get(originalFileName).getFileName().toString() : "file";

        String fileExtension = "";
        int dotIndex = safeFileName.lastIndexOf('.');
        if (dotIndex != -1) {
            fileExtension = safeFileName.substring(dotIndex);
        }

        return UUID.randomUUID() + fileExtension;
    }

    public byte[] downloadFile(String filename) {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = containerClient.getBlobClient(filename);
        if (!blobClient.exists()) {
            throw new EntityNotFoundException("File not found: " + filename);
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blobClient.downloadStream(outputStream);
        return outputStream.toByteArray();
    }

}