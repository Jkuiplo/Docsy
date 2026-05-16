package com.google.docsy.feature.documentVersion.mapper;

import com.google.docsy.feature.documentVersion.DocumentVersion;
import com.google.docsy.feature.documentVersion.dto.DocumentVersionResponse;
import org.springframework.stereotype.Component;

@Component
public class DocumentVersionMapper {
    public DocumentVersionResponse toResponse(DocumentVersion version) {
        return DocumentVersionResponse.builder()
                .id(version.getId())
                .versionNumber(version.getVersionNumber())
                .titleSnapshot(version.getTitleSnapshot())
                .contentSnapshot(version.getContentSnapshot())
                .createdById(version.getCreatedBy().getId())
                .createdByName(version.getCreatedBy().getFullName())
                .createdAt(version.getCreatedAt())
                .build();
    }
}