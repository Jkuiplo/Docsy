package com.google.docsy.feature.document.mapper;

import com.google.docsy.feature.document.Document;
import com.google.docsy.feature.document.dto.response.DocumentDetailsResponse;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {

    public DocumentDetailsResponse toDetailsResponse(Document doc) {
        return DocumentDetailsResponse.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .content(doc.getContent())
                .templateSnapshotHtml(doc.getTemplateSnapshotHtml())
                .status(doc.getStatus())
                .authorId(doc.getAuthor().getId())
                .authorName(doc.getAuthor().getFullName())
                .reviewerId(doc.getAssignedReviewer() != null ? doc.getAssignedReviewer().getId() : null)
                .reviewerName(doc.getAssignedReviewer() != null ? doc.getAssignedReviewer().getFullName() : null)
                .updatedAt(doc.getUpdatedAt())
                .build();
    }
}