package com.google.docsy.feature.archive;

import com.google.docsy.common.security.UserPrincipal;
import com.google.docsy.feature.document.dto.response.DocumentDetailsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/archive")
@RequiredArgsConstructor
public class ArchiveController {

    private final ArchiveService archiveService;

    @GetMapping
    public ResponseEntity<List<DocumentDetailsResponse>> getArchivedDocuments(
            @PathVariable UUID workspaceId,
            @AuthenticationPrincipal UserPrincipal principal) {
        
        return ResponseEntity.ok(archiveService.getArchivedDocuments(principal.getUser().getId(), workspaceId));
    }
}