package com.google.docsy.feature.document.dto.response;

import com.google.docsy.enums.DocumentStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class DocumentDetailsResponse {
    private UUID id;
    private String title;
    private String content;
    private String templateSnapshotHtml;
    private DocumentStatus status;
    private UUID authorId;
    private String authorName;
    private UUID reviewerId;
    private String reviewerName;
    private LocalDateTime updatedAt;
}