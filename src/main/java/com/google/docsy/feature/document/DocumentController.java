package com.google.docsy.feature.document;

import com.google.docsy.common.security.UserPrincipal;
import com.google.docsy.feature.document.dto.request.CreateDocumentRequest;
import com.google.docsy.feature.document.dto.request.UpdateDocumentRequest;
import com.google.docsy.feature.document.dto.response.DocumentDetailsResponse;
import com.google.docsy.feature.documentVersion.dto.DocumentVersionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<DocumentDetailsResponse> createDocument(
            @PathVariable UUID workspaceId,
            @RequestBody CreateDocumentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(documentService.createDocument(principal.getUser(), workspaceId, request));
    }

    @GetMapping
    public ResponseEntity<List<DocumentDetailsResponse>> getDocuments(
            @PathVariable UUID workspaceId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(documentService.getDocuments(principal.getUser().getId(), workspaceId));
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentDetailsResponse> getDocument(
            @PathVariable UUID workspaceId,
            @PathVariable UUID documentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(documentService.getDocumentById(principal.getUser().getId(), workspaceId, documentId));
    }

    @PatchMapping("/{documentId}")
    public ResponseEntity<DocumentDetailsResponse> updateDocument(
            @PathVariable UUID workspaceId,
            @PathVariable UUID documentId,
            @RequestBody UpdateDocumentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(documentService.updateDocument(principal.getUser(), workspaceId, documentId, request));
    }

    @GetMapping("/{documentId}/versions")
    public ResponseEntity<List<DocumentVersionResponse>> getDocumentVersions(
            @PathVariable UUID workspaceId,
            @PathVariable UUID documentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(documentService.getDocumentVersions(principal.getUser().getId(), workspaceId, documentId));
    }
}