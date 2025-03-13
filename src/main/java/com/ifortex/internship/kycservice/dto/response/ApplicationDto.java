package com.ifortex.internship.kycservice.dto.response;

import com.ifortex.internship.kycservice.model.constant.KycStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class ApplicationDto {
    String applicationId;
    String accountId;
    KycStatus status;
    Instant submittedAt;
    Instant reviewedAt;
    UUID reviewerId;
    String rejectionReason;
    List<DocumentDto> documents;
}
