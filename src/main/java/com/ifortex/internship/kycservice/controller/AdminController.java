package com.ifortex.internship.kycservice.controller;

import com.ifortex.internship.kycservice.dto.request.RejectionReason;
import com.ifortex.internship.kycservice.dto.response.ApplicationDto;
import com.ifortex.internship.kycservice.dto.response.ApplicationListDto;
import com.ifortex.internship.kycservice.dto.response.FileDownloadResponse;
import com.ifortex.internship.kycservice.service.ApplicationService;
import com.ifortex.internship.kycservice.service.DocumentService;
import com.ifortex.internship.medstarter.security.service.AuthenticationFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Admin Functions API")
@SecurityRequirement(name = "BearerAuth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/career")
@RequiredArgsConstructor
public class AdminController {

    ApplicationService applicationService;
    AuthenticationFacade authenticationFacade;
    DocumentService documentService;

    @Operation(
        summary = "Get all applications",
        description = "Retrieves a paginated list of all KYC applications for admin"
    )
    @GetMapping("/applications")
    public ResponseEntity<List<ApplicationListDto>> getAllApplications(
        @RequestParam(defaultValue = "0")
        @Min(value = 0, message = "Page can't be a negative number")
        int page,
        @RequestParam(defaultValue = "20")
        @Min(value = 1, message = "Size of the page can't be less than 1")
        @Max(value = 100, message = "Size of the page can't be more than 100")
        int size) {
        log.info("Request to get all applications from admin with account ID: {}", authenticationFacade.getAccountIdFromAuthentication());
        List<ApplicationListDto> result = applicationService.getAllApplications(page, size);
        log.info("Successfully fetched {} applications", result.size());
        return ResponseEntity.ok(result);
    }

    @Operation(
        summary = "Get application details",
        description = "Retrieves detailed information about a specific KYC application"
    )
    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<ApplicationDto> getApplicationDetails(@PathVariable @NotNull UUID applicationId) {
        log.info("Request to get application with ID: {}", applicationId);
        var applicationDetails = applicationService.getApplicationDetails(applicationId);
        log.info("Application with ID: {} successfully fetched", applicationId);
        return ResponseEntity.ok(applicationDetails);
    }

    @GetMapping("/image")
    public ResponseEntity<Resource> getImage(@RequestParam @NotNull UUID documentId) {
        log.info("Request to get image with ID: {}", documentId);

        FileDownloadResponse fileDownloadResponse = documentService.downloadDocument(documentId);
        ByteArrayResource resource = new ByteArrayResource(fileDownloadResponse.getFileData());
        return ResponseEntity.ok()
            .contentLength(fileDownloadResponse.getFileData().length)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDownloadResponse.getFileName() + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
    }

    @Operation(summary = "Approve a paramedic application",
        description = "Approves the paramedic application by the provided application ID.")
    @PostMapping("/applications/{applicationId}/approve")
    public ResponseEntity<Void> approveApplication(@PathVariable UUID applicationId) {
        log.info("Request to approve application: {}", applicationId);
        applicationService.approveApplication(applicationId);
        log.info("Application: {} was approved", applicationId);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Reject a paramedic application",
        description = "Rejects a paramedic application with a specified reason.")
    @PostMapping("/applications/{applicationId}/reject")
    public ResponseEntity<Void> rejectApplication(@PathVariable UUID applicationId, @RequestBody RejectionReason reason) {
        log.info("Request to reject application: {}", applicationId);
        applicationService.rejectApplication(applicationId, reason);
        log.info("Application: {} was rejected", applicationId);

        return ResponseEntity.noContent().build();
    }
}
