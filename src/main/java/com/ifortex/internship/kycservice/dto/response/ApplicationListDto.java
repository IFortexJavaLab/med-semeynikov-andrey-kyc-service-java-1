package com.ifortex.internship.kycservice.dto.response;

import com.ifortex.internship.kycservice.model.constant.KycStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class ApplicationListDto {
    String applicationId;
    String accountId;
    KycStatus status;
    Instant submittedAt;
}
