package com.ifortex.internship.kycservice.util;

import com.ifortex.internship.kycservice.dto.response.ApplicationDto;
import com.ifortex.internship.kycservice.dto.response.ApplicationListDto;
import com.ifortex.internship.kycservice.dto.response.DocumentDto;
import com.ifortex.internship.kycservice.model.Application;
import com.ifortex.internship.kycservice.model.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    @Mapping(target = "documents", source = "documents")
    ApplicationListDto applicationToApplicationListDto(Application application);

    List<ApplicationListDto> applicationsToApplicationListDtos(List<Application> applications);

    @Mapping(target = "documents", source = "documents")
    ApplicationDto applicationToApplicationDto(Application application);

    DocumentDto documentToDocumentDto(Document document);

    List<DocumentDto> documentsToDocumentDtos(List<Document> documents);
}
