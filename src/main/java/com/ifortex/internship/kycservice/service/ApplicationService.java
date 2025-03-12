package com.ifortex.internship.kycservice.service;

import com.ifortex.internship.kycservice.dto.response.ApplicationDto;
import com.ifortex.internship.kycservice.dto.response.ApplicationListDto;
import com.ifortex.internship.kycservice.dto.response.UploadedFileInfoDto;
import com.ifortex.internship.kycservice.model.Application;
import com.ifortex.internship.kycservice.model.Document;
import com.ifortex.internship.kycservice.model.constant.DocumentType;
import com.ifortex.internship.kycservice.model.constant.KycStatus;
import com.ifortex.internship.kycservice.repository.ApplicationRepository;
import com.ifortex.internship.kycservice.util.ApplicationMapper;
import com.ifortex.internship.medstarter.exception.custom.EntityNotFoundException;
import com.ifortex.internship.medstarter.exception.custom.ForbiddenActionException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    ApplicationRepository applicationRepository;
    FileStorageService fileStorageService;
    ApplicationMapper applicationMapper;

    @Transactional
    public void submitApplication(UUID accountId,
                                  MultipartFile identityDocument,
                                  MultipartFile selfie,
                                  MultipartFile medicalCertification) {

        log.info("Submitting application from user with ID: {}", accountId);

        List<Application> applications = applicationRepository.findByAccountIdAndStatusPendingOrApproved(accountId);
        if (!applications.isEmpty()) {
            log.error("User with account ID: {} trying to submit one more application while has pending or approved", accountId);
            throw new ForbiddenActionException("You can't upload another application. Wait until review");
        }

        UploadedFileInfoDto identityFile = fileStorageService.uploadFile(identityDocument);
        UploadedFileInfoDto selfieFile = fileStorageService.uploadFile(selfie);
        UploadedFileInfoDto medCertificateDoc = fileStorageService.uploadFile(medicalCertification);
        log.debug("Documents for user: {} successfully uploaded in the blob storage", accountId);

        Application application = new Application();
        application.setAccountId(accountId);
        application.setStatus(KycStatus.PENDING);

        List<Document> documents = new ArrayList<>();

        Document idDoc = new Document(DocumentType.ID_CARD, identityFile);
        documents.add(idDoc);

        Document selfieDoc = new Document(DocumentType.SELFIE, selfieFile);
        documents.add(selfieDoc);

        Document medicalDoc = new Document(DocumentType.MEDICAL_CERTIFICATE, medCertificateDoc);
        documents.add(medicalDoc);

        application.setDocuments(documents);
        applicationRepository.save(application);

        log.info("Application with ID: {} from user: {} successfully submitted", application.getApplicationId(), accountId);
    }

    public List<ApplicationListDto> getAllApplications(int page, int size) {
        log.info("Getting all applications");

        Pageable pageable = PageRequest.of(page, size);
        Page<Application> applications = applicationRepository.findAll(pageable);
        return applicationMapper.applicationsToApplicationListDtos(applications.stream().toList());
    }

    public ApplicationDto getApplicationDetails(UUID applicationId) {
        log.info("Getting application with ID: {}", applicationId);

        var application =
            applicationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Application with id: %s not found", applicationId)));

        return applicationMapper.applicationToApplicationDto(application);
    }

}
