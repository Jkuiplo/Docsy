package com.google.docsy.feature.archive;

import com.google.docsy.enums.DocumentStatus;
import com.google.docsy.feature.audit.AuditLogService;
import com.google.docsy.feature.document.Document;
import com.google.docsy.feature.document.DocumentRepository;
import com.google.docsy.feature.document.dto.response.DocumentDetailsResponse;
import com.google.docsy.feature.document.mapper.DocumentMapper;
import com.google.docsy.feature.permission.Permission;
import com.google.docsy.feature.permission.PermissionChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArchiveService {

    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;
    private final PermissionChecker permissionChecker;
    private final AuditLogService auditLogService;

    public List<DocumentDetailsResponse> getArchivedDocuments(UUID userId, UUID workspaceId) {
        // We added VIEW_ARCHIVE to the Permission enum in Stage 6!
        permissionChecker.checkPermission(userId, workspaceId, Permission.VIEW_ARCHIVE);

        return documentRepository.findByWorkspaceIdAndStatusOrderByUpdatedAtDesc(workspaceId, DocumentStatus.ARCHIVED)
                .stream()
                .map(documentMapper::toDetailsResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void archiveExpiredApprovedDocuments() {
        System.out.println("--- CRON JOB: Running Archive check for Expired Approved Documents ---");

        List<Document> documentsToArchive = documentRepository
                .findByStatusAndArchiveScheduledAtLessThanEqual(DocumentStatus.APPROVED, LocalDateTime.now());

        if (documentsToArchive.isEmpty()) {
            System.out.println("No documents require archiving today.");
            return;
        }

        for (Document doc : documentsToArchive) {
            doc.setStatus(DocumentStatus.ARCHIVED);
            doc.setArchivedAt(LocalDateTime.now());
            // Clear the schedule timer since it's now archived
            doc.setArchiveScheduledAt(null); 
            auditLogService.logAction(doc.getWorkspace(), null, "DOCUMENT_ARCHIVED", "Auto-archived document ID: " + doc.getId());
        }


        documentRepository.saveAll(documentsToArchive);
        System.out.println("Successfully archived " + documentsToArchive.size() + " document(s).");
    }
}