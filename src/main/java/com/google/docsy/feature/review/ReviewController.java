package com.google.docsy.feature.review;

import com.google.docsy.common.security.UserPrincipal;
import com.google.docsy.feature.review.dto.request.ApproveDocumentRequest;
import com.google.docsy.feature.review.dto.request.RejectDocumentRequest;
import com.google.docsy.feature.review.dto.request.SubmitDocumentRequest;
import com.google.docsy.feature.review.dto.response.ReviewQueueItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/documents/{documentId}/submit")
    public ResponseEntity<Void> submitDocument(
            @PathVariable UUID workspaceId,
            @PathVariable UUID documentId,
            @RequestBody SubmitDocumentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        reviewService.submitForReview(principal.getUser(), workspaceId, documentId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/documents/{documentId}/approve")
    public ResponseEntity<Void> approveDocument(
            @PathVariable UUID workspaceId,
            @PathVariable UUID documentId,
            @RequestBody(required = false) ApproveDocumentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        // If request is null because no body was sent, initialize an empty one
        ApproveDocumentRequest actualRequest = request != null ? request : new ApproveDocumentRequest();
        reviewService.approveDocument(principal.getUser(), workspaceId, documentId, actualRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/documents/{documentId}/reject")
    public ResponseEntity<Void> rejectDocument(
            @PathVariable UUID workspaceId,
            @PathVariable UUID documentId,
            @RequestBody RejectDocumentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        reviewService.rejectDocument(principal.getUser(), workspaceId, documentId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/review-queue")
    public ResponseEntity<List<ReviewQueueItemResponse>> getReviewQueue(
            @PathVariable UUID workspaceId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(reviewService.getReviewQueue(principal.getUser().getId(), workspaceId));
    }
}