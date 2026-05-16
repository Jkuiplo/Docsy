package com.google.docsy.feature.review.dto.response;

import com.google.docsy.enums.DocumentStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReviewQueueItemResponse {
    private UUID documentId;
    private String title;
    private UUID authorId;
    private String authorName;
    private DocumentStatus status;
    private LocalDateTime updatedAt;
}