package com.ifortex.internship.kycservice.controller;

import com.ifortex.internship.kycservice.service.ApplicationService;
import com.ifortex.internship.medstarter.security.service.AuthenticationFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(name = "Client Functions API")
@SecurityRequirement(name = "BearerAuth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/career")
@RequiredArgsConstructor
public class ClientController {

    ApplicationService applicationService;
    AuthenticationFacade authenticationFacade;

    @Operation(
        summary = "Submit KYC Application",
        description = "Allows a client to submit a KYC application with the required documents: identity document, selfie with identity document, and medical certification."
    )
    @PostMapping("/kyc")
    public ResponseEntity<Void> submitKycApplication(
        @RequestParam("identityDocument") @NotNull MultipartFile identityDocument,
        @RequestParam("selfie") @NotNull MultipartFile selfie,
        @RequestParam("medicalCertification") @NotNull MultipartFile medicalCertification) {

        UUID accountId = authenticationFacade.getAccountIdFromAuthentication();
        log.info("Attempt to submit kyc application for account: {}", accountId);
        applicationService.submitApplication(
            accountId, identityDocument, selfie, medicalCertification);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
