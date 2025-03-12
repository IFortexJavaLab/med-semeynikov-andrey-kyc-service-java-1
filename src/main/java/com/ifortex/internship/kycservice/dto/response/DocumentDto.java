package com.ifortex.internship.kycservice.dto.response;

import com.ifortex.internship.kycservice.model.constant.DocumentType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class DocumentDto {
    UUID documentId;
    DocumentType documentType;
}
