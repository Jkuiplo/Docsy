package com.google.docsy.feature.review;

import com.google.docsy.common.exception.BadRequestException;
import com.google.docsy.common.exception.ForbiddenException;
import com.google.docsy.common.exception.NotFoundException;
import com.google.docsy.enums.DocumentStatus;
import com.google.docsy.enums.ReviewCommentType;
import com.google.docsy.feature.audit.AuditLogService;
import com.google.docsy.feature.document.Document;
import com.google.docsy.feature.document.DocumentRepository;
import com.google.docsy.feature.notification.NotificationService;
import com.google.docsy.feature.permission.PermissionChecker;
import com.google.docsy.feature.review.dto.request.ApproveDocumentRequest;
import com.google.docsy.feature.review.dto.request.RejectDocumentRequest;
import com.google.docsy.feature.review.dto.request.SubmitDocumentRequest;
import com.google.docsy.feature.review.dto.response.ReviewQueueItemResponse;
import com.google.docsy.feature.reviewComment.ReviewCommentService;
import com.google.docsy.feature.user.User;
import com.google.docsy.feature.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final ReviewCommentService commentService;
    private final PermissionChecker permissionChecker;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Transactional
    public void submitForReview(User submitter, UUID workspaceId, UUID documentId, SubmitDocumentRequest request) {
        permissionChecker.verifyWorkspaceAccess(submitter.getId(), workspaceId);

        Document document = documentRepository.findByIdAndWorkspaceId(documentId, workspaceId)
                .orElseThrow(() -> new NotFoundException("Document not found"));

        if (document.getStatus() != DocumentStatus.DRAFT && document.getStatus() != DocumentStatus.REJECTED) {
            throw new BadRequestException("Only DRAFT or REJECTED documents can be submitted.");
        }

        User reviewer = userRepository.findById(request.getReviewerId())
                .orElseThrow(() -> new NotFoundException("Assigned reviewer not found"));
        
        // Ensure reviewer is actually in the workspace
        permissionChecker.verifyWorkspaceAccess(reviewer.getId(), workspaceId);

        document.setStatus(DocumentStatus.ON_REVIEW);
        document.setAssignedReviewer(reviewer);
        
        auditLogService.logAction(document.getWorkspace(), submitter, "DOCUMENT_SUBMITTED", "Submitted document ID: " + document.getId());

        documentRepository.save(document);

        commentService.addSystemComment(document, submitter, ReviewCommentType.SUBMIT, request.getComment());
        
        notificationService.sendDocumentStatusEmail(
            reviewer.getEmail(), document.getTitle(), "ON_REVIEW", request.getComment()
        );
    }

    @Transactional
    public void approveDocument(User reviewer, UUID workspaceId, UUID documentId, ApproveDocumentRequest request) {
        permissionChecker.verifyWorkspaceAccess(reviewer.getId(), workspaceId);

        Document document = getDocumentForReview(documentId, workspaceId, reviewer.getId());

        document.setStatus(DocumentStatus.APPROVED);
        document.setApprovedAt(LocalDateTime.now());
        // Scheduled for archiving in 7 days, as per your SRS rules
        document.setArchiveScheduledAt(LocalDateTime.now().plusDays(7)); 
        
        auditLogService.logAction(document.getWorkspace(), reviewer, "DOCUMENT_APPROVED", "Approved document ID: " + document.getId());

        documentRepository.save(document);

        commentService.addSystemComment(document, reviewer, ReviewCommentType.APPROVE, request.getComment());
        
        notificationService.sendDocumentStatusEmail(
            document.getAuthor().getEmail(), document.getTitle(), "APPROVED", request.getComment()
        );
    }

    @Transactional
    public void rejectDocument(User reviewer, UUID workspaceId, UUID documentId, RejectDocumentRequest request) {
        permissionChecker.verifyWorkspaceAccess(reviewer.getId(), workspaceId);

        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            throw new BadRequestException("A rejection reason is strictly required.");
        }

        Document document = getDocumentForReview(documentId, workspaceId, reviewer.getId());

        document.setStatus(DocumentStatus.REJECTED);

        auditLogService.logAction(document.getWorkspace(), reviewer, "DOCUMENT_REJECTED", "Rejected document ID: " + document.getId());

        documentRepository.save(document);

        commentService.addSystemComment(document, reviewer, ReviewCommentType.REJECT, request.getReason());
        
        notificationService.sendDocumentStatusEmail(
            document.getAuthor().getEmail(), document.getTitle(), "REJECTED", request.getReason()
        );
    }

    public List<ReviewQueueItemResponse> getReviewQueue(UUID reviewerId, UUID workspaceId) {
        permissionChecker.verifyWorkspaceAccess(reviewerId, workspaceId);

        // Ideally, create a specific repository method for this in DocumentRepository, 
        // but for vibe coding we can stream/filter or add the method.
        // Let's assume you'll add this to DocumentRepository: 
        // List<Document> findByWorkspaceIdAndStatusAndAssignedReviewerId(UUID workspaceId, DocumentStatus status, UUID reviewerId);
        
        return documentRepository.findByWorkspaceIdOrderByUpdatedAtDesc(workspaceId).stream()
                .filter(doc -> doc.getStatus() == DocumentStatus.ON_REVIEW)
                .filter(doc -> doc.getAssignedReviewer() != null && doc.getAssignedReviewer().getId().equals(reviewerId))
                .map(this::mapToQueueItem)
                .collect(Collectors.toList());
    }

    private Document getDocumentForReview(UUID documentId, UUID workspaceId, UUID reviewerId) {
        Document document = documentRepository.findByIdAndWorkspaceId(documentId, workspaceId)
                .orElseThrow(() -> new NotFoundException("Document not found"));

        if (document.getStatus() != DocumentStatus.ON_REVIEW) {
            throw new BadRequestException("Document is not currently under review.");
        }

        // Security check: Only the assigned reviewer can approve/reject
        if (document.getAssignedReviewer() == null || !document.getAssignedReviewer().getId().equals(reviewerId)) {
            throw new ForbiddenException("You are not the assigned reviewer for this document.");
        }

        return document;
    }

    private ReviewQueueItemResponse mapToQueueItem(Document doc) {
        return ReviewQueueItemResponse.builder()
                .documentId(doc.getId())
                .title(doc.getTitle())
                .authorId(doc.getAuthor().getId())
                .authorName(doc.getAuthor().getFullName())
                .status(doc.getStatus())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }
}