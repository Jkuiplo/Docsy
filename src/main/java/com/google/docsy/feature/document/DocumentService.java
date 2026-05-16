package com.google.docsy.feature.document;

import com.google.docsy.common.exception.BadRequestException;
import com.google.docsy.common.exception.NotFoundException;
import com.google.docsy.enums.DocumentStatus;
import com.google.docsy.feature.audit.AuditLogService;
import com.google.docsy.feature.document.dto.request.CreateDocumentRequest;
import com.google.docsy.feature.document.dto.request.UpdateDocumentRequest;
import com.google.docsy.feature.document.dto.response.DocumentDetailsResponse;
import com.google.docsy.feature.document.mapper.DocumentMapper;
import com.google.docsy.feature.documentVersion.DocumentVersionService;
import com.google.docsy.feature.documentVersion.dto.DocumentVersionResponse;
import com.google.docsy.feature.permission.PermissionChecker;
import com.google.docsy.feature.template.Template;
import com.google.docsy.feature.template.TemplateRepository;
import com.google.docsy.feature.user.User;
import com.google.docsy.feature.workspace.Workspace;
import com.google.docsy.feature.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final WorkspaceRepository workspaceRepository;
    private final TemplateRepository templateRepository;
    private final DocumentVersionService versionService;
    private final PermissionChecker permissionChecker;
    private final DocumentMapper documentMapper;
    private final AuditLogService auditLogService;

    @Transactional
    public DocumentDetailsResponse createDocument(User author, UUID workspaceId, CreateDocumentRequest request) {
        
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new NotFoundException("Workspace not found"));

        Document document = new Document();
        document.setWorkspace(workspace);
        document.setAuthor(author);
        document.setTitle(request.getTitle());
        document.setContent(request.getContent());
        document.setStatus(DocumentStatus.DRAFT);

        if (request.getTemplateId() != null) {
            permissionChecker.checkCanCreateFromTemplate(author.getId(), workspaceId);
            Template template = templateRepository.findByIdAndWorkspaceIdAndDeletedAtIsNull(request.getTemplateId(), workspaceId)
                    .orElseThrow(() -> new BadRequestException("Template not found or deleted"));
            document.setTemplate(template);
            // CRITICAL: Copy template HTML so old docs don't break if template changes
            document.setTemplateSnapshotHtml(template.getHtmlContent()); 
        } else {
            permissionChecker.checkCanCreateBlankDocument(author.getId(), workspaceId);
        }

        Document savedDoc = documentRepository.save(document);
        
        // Auto-create initial version snapshot
        versionService.createSnapshot(savedDoc, author);

        auditLogService.logAction(workspace, author, "DOCUMENT_CREATED", "Created document: " + request.getTitle());

        return documentMapper.toDetailsResponse(savedDoc);
    }

    public List<DocumentDetailsResponse> getDocuments(UUID userId, UUID workspaceId) {
        permissionChecker.verifyWorkspaceAccess(userId, workspaceId);

        return documentRepository.findByWorkspaceIdOrderByUpdatedAtDesc(workspaceId).stream()
                .map(documentMapper::toDetailsResponse)
                .collect(Collectors.toList());
    }

    public DocumentDetailsResponse getDocumentById(UUID userId, UUID workspaceId, UUID documentId) {
        permissionChecker.verifyWorkspaceAccess(userId, workspaceId);

        Document doc = documentRepository.findByIdAndWorkspaceId(documentId, workspaceId)
                .orElseThrow(() -> new NotFoundException("Document not found"));

        return documentMapper.toDetailsResponse(doc);
    }

    @Transactional
    public DocumentDetailsResponse updateDocument(User author, UUID workspaceId, UUID documentId, UpdateDocumentRequest request) {
        permissionChecker.verifyWorkspaceAccess(author.getId(), workspaceId);

        Document document = documentRepository.findByIdAndWorkspaceId(documentId, workspaceId)
                .orElseThrow(() -> new NotFoundException("Document not found"));

        // STATE MACHINE RULE ENFORCEMENT
        if (document.getStatus() != DocumentStatus.DRAFT && document.getStatus() != DocumentStatus.REJECTED) {
            throw new BadRequestException("Document cannot be edited because it is currently " + document.getStatus());
        }

        // Only authors or admins can edit (You can refine this rule later in PermissionChecker)
        if (!document.getAuthor().getId().equals(author.getId())) {
            permissionChecker.checkCanEditAnyDocument(author.getId(), workspaceId); // Fallback to admin power
        }

        if (request.getTitle() != null) document.setTitle(request.getTitle());
        if (request.getContent() != null) document.setContent(request.getContent());

        Document updatedDoc = documentRepository.save(document);

        // Auto-create version snapshot for the history timeline
        versionService.createSnapshot(updatedDoc, author);

        return documentMapper.toDetailsResponse(updatedDoc);
    }

    public List<DocumentVersionResponse> getDocumentVersions(UUID userId, UUID workspaceId, UUID documentId) {
        permissionChecker.verifyWorkspaceAccess(userId, workspaceId);
        
        // Ensure doc exists in this workspace to prevent IDOR
        documentRepository.findByIdAndWorkspaceId(documentId, workspaceId)
                .orElseThrow(() -> new NotFoundException("Document not found"));

        return versionService.getDocumentVersions(documentId);
    }

    @Transactional
    public void unassignRemovedReviewer(UUID workspaceId, UUID reviewerId) {
        List<Document> stuckDocuments = documentRepository.findByWorkspaceIdAndAssignedReviewerIdAndStatus(
                workspaceId, reviewerId, DocumentStatus.ON_REVIEW);

        if (stuckDocuments.isEmpty()) return;

        for (Document doc : stuckDocuments) {
            doc.setAssignedReviewer(null);
            doc.setStatus(DocumentStatus.DRAFT);
        }
        
        documentRepository.saveAll(stuckDocuments);
        System.out.println("Reverted " + stuckDocuments.size() + " document(s) to DRAFT because reviewer was removed.");
    }
}