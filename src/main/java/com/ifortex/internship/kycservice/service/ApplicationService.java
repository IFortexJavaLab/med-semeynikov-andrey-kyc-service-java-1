package com.ifortex.internship.kycservice.service;

import com.ifortex.internship.authserviceapi.AccountingServiceApi;
import com.ifortex.internship.authserviceapi.dto.response.AccountDto;
import com.ifortex.internship.authserviceapi.exception.CustomFeignException;
import com.ifortex.internship.kycservice.dto.request.RejectionReason;
import com.ifortex.internship.kycservice.dto.response.ApplicationDto;
import com.ifortex.internship.kycservice.dto.response.ApplicationListDto;
import com.ifortex.internship.kycservice.dto.response.UploadedFileInfoDto;
import com.ifortex.internship.kycservice.model.Application;
import com.ifortex.internship.kycservice.model.Document;
import com.ifortex.internship.kycservice.model.constant.DocumentType;
import com.ifortex.internship.kycservice.model.constant.KycStatus;
import com.ifortex.internship.kycservice.repository.ApplicationRepository;
import com.ifortex.internship.kycservice.util.AccountDtoMapper;
import com.ifortex.internship.kycservice.util.ApplicationMapper;
import com.ifortex.internship.medstarter.exception.custom.EmailSendException;
import com.ifortex.internship.medstarter.exception.custom.EntityNotFoundException;
import com.ifortex.internship.medstarter.exception.custom.ForbiddenActionException;
import com.ifortex.internship.medstarter.exception.custom.InternalServiceException;
import com.ifortex.internship.medstarter.model.KycVerificationEvent;
import com.ifortex.internship.medstarter.model.constant.KycEventType;
import com.ifortex.internship.medstarter.model.constant.LinkConstants;
import com.ifortex.internship.medstarter.security.service.AuthenticationFacade;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    static final String APPROVED_APPLICATION = "Approved application";
    static final String REJECTED_APPLICATION = "Rejected application";
    static final String LOG_SENDING_EMAIL_ERROR = "Error during sending verification email for: {}. There details: {}";

    ApplicationRepository applicationRepository;
    FileStorageService fileStorageService;
    ApplicationMapper applicationMapper;
    AuthenticationFacade authenticationFacade;
    KafkaTemplate<String, KycVerificationEvent> kafkaTemplate;
    UserNotificationService userNotificationService;
    AccountingServiceApi accountingServiceApi;
    AccountDtoMapper accountDtoMapper;

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

        var application = findApplicationByApplicationId(applicationId);
        return applicationMapper.applicationToApplicationDto(application);
    }

    @Transactional
    public void approveApplication(UUID applicationId) {
        var adminDetails = authenticationFacade.getAdminDetailsFromAuthentication();
        log.info("Approving application: {} by admin: {}", applicationId, adminDetails.getAccountId());

        var targetApplication = findApplicationByApplicationId(applicationId);
        validateApplicationModification(targetApplication);

        targetApplication.setStatus(KycStatus.APPROVED);
        targetApplication.setReviewedAt(Instant.now());
        targetApplication.setReviewerId(adminDetails.getAccountId());
        applicationRepository.save(targetApplication);
        log.debug("Application with ID: {} has been approved and saved to db", applicationId);

        var kycVerificationEvent = new KycVerificationEvent(
            KycEventType.KYC_VERIFICATION_APPROVED,
            targetApplication.getAccountId(),
            Instant.now(),
            null);

        AccountDto clientAccount = getAccountDtoFromAuthService(targetApplication);

        try {
            userNotificationService.sendApprovedApplicationEmail(clientAccount.getEmail(), APPROVED_APPLICATION, LinkConstants.LOGIN);
        } catch (MessagingException e) {
            log.error(LOG_SENDING_EMAIL_ERROR, clientAccount.getEmail(), e.getMessage());
            throw new EmailSendException(String.format("Failed to send approved application email to the email: %s", clientAccount.getEmail()));
        }

        log.info("Start- Sending kycVerificationEvent with status: {}  to Kafka Topic", kycVerificationEvent.eventType());
        kafkaTemplate.send("kyc-verification-events", kycVerificationEvent);
        log.info("End- Sending kycVerificationEvent with status: {} to Kafka Topic ", kycVerificationEvent.eventType());
    }

    @Transactional
    public void rejectApplication(UUID applicationId, RejectionReason rejectionReason) {
        var adminDetails = authenticationFacade.getAdminDetailsFromAuthentication();
        log.info("Rejecting application: {} by admin: {}", applicationId, adminDetails.getAccountId());

        var targetApplication = findApplicationByApplicationId(applicationId);
        validateApplicationModification(targetApplication);

        targetApplication.setStatus(KycStatus.REJECTED);
        targetApplication.setReviewedAt(Instant.now());
        targetApplication.setReviewerId(adminDetails.getAccountId());
        targetApplication.setRejectionReason(rejectionReason.reason());
        applicationRepository.save(targetApplication);
        log.debug("Application with ID: {} has been rejected and saved to db", applicationId);

        AccountDto clientAccount = getAccountDtoFromAuthService(targetApplication);

        try {
            userNotificationService.sendRejectedApplicationEmail(clientAccount.getEmail(), REJECTED_APPLICATION, rejectionReason.reason());
        } catch (MessagingException e) {
            log.error(LOG_SENDING_EMAIL_ERROR, clientAccount.getEmail(), e.getMessage());
            throw new EmailSendException(String.format("Failed to send approved application email to the email: %s", clientAccount.getEmail()));
        }
    }

    private AccountDto getAccountDtoFromAuthService(Application targetApplication) {
        AccountDto clientAccount;
        try {
            // I know that it is a bad code, but a have problems with decode body of ResponseEntity to AccountDto for no apparent reason to me
            var accountDtoResponseEntity = accountingServiceApi.getUserProfileById(targetApplication.getAccountId());
            clientAccount = accountDtoMapper.map((Map<String, Object>) accountDtoResponseEntity.getBody());
        } catch (CustomFeignException ex) {
            log.debug("Error occurred during call to the auth service");
            throw new InternalServiceException("Something went wrong, try later");
        }
        assert clientAccount != null;
        return clientAccount;
    }

    private void validateApplicationModification(Application application) {
        boolean isApplicationPending = KycStatus.PENDING.equals(application.getStatus());
        if (!isApplicationPending) {
            log.error("Attempt to change status of the not PENDING application with ID: {}", application.getApplicationId());
            throw new ForbiddenActionException("You can edit applications only with status PENDING");
        }
    }

    private Application findApplicationByApplicationId(UUID applicationId) {
        return applicationRepository.findByApplicationId(applicationId)
            .orElseThrow(() -> new EntityNotFoundException(
                String.format("Application with ID: %s not found", applicationId)));
    }
}
