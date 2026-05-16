package com.google.docsy.feature.template;

import com.google.docsy.common.exception.NotFoundException;
import com.google.docsy.common.security.CurrentUserProvider;
import com.google.docsy.feature.audit.AuditLogService;
import com.google.docsy.feature.permission.PermissionChecker;
import com.google.docsy.feature.template.dto.request.CreateTemplateRequest;
import com.google.docsy.feature.template.dto.request.UpdateTemplateRequest;
import com.google.docsy.feature.template.dto.response.TemplateResponse;
import com.google.docsy.feature.template.mapper.TemplateMapper;
import com.google.docsy.feature.user.User;
import com.google.docsy.feature.workspace.Workspace;
import com.google.docsy.feature.workspace.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final WorkspaceRepository workspaceRepository;
    private final PermissionChecker permissionChecker;
    private final TemplateMapper templateMapper;
    private final CurrentUserProvider currentUserProvider;
    private final AuditLogService auditLogService;

    @Transactional
    public TemplateResponse createTemplate(UUID userId, UUID workspaceId, CreateTemplateRequest request) {
        // Gatekeeper: Only users with MANAGE_TEMPLATES permission can do this
        permissionChecker.checkCanManageTemplates(userId, workspaceId);

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new NotFoundException("Workspace not found"));

        Template template = new Template();
        template.setWorkspace(workspace);
        template.setTitle(request.getTitle());
        template.setHtmlContent(request.getHtmlContent());

        Template savedTemplate = templateRepository.save(template);

        User requester = currentUserProvider.getCurrentUser();
        auditLogService.logAction(workspace, requester, "TEMPLATE_CREATED", "Template created: " + request.getTitle());

        return templateMapper.toResponse(savedTemplate);
    }

    public List<TemplateResponse> getTemplates(UUID userId, UUID workspaceId) {
        // Gatekeeper: Any member of the workspace can view templates
        permissionChecker.verifyWorkspaceAccess(userId, workspaceId);

        return templateRepository.findByWorkspaceIdAndDeletedAtIsNull(workspaceId).stream()
                .map(templateMapper::toResponse)
                .collect(Collectors.toList());
    }

    public TemplateResponse getTemplateById(UUID userId, UUID workspaceId, UUID templateId) {
        // Gatekeeper: Any member of the workspace can view a specific template
        permissionChecker.verifyWorkspaceAccess(userId, workspaceId);

        Template template = getActiveTemplate(workspaceId, templateId);
        return templateMapper.toResponse(template);
    }

    @Transactional
    public TemplateResponse updateTemplate(UUID userId, UUID workspaceId, UUID templateId, UpdateTemplateRequest request) {
        // Gatekeeper: Only users with MANAGE_TEMPLATES permission can do this
        permissionChecker.checkCanManageTemplates(userId, workspaceId);

        Template template = getActiveTemplate(workspaceId, templateId);

        if (request.getTitle() != null) {
            template.setTitle(request.getTitle());
        }
        if (request.getHtmlContent() != null) {
            template.setHtmlContent(request.getHtmlContent());
        }

        Template updatedTemplate = templateRepository.save(template);
        return templateMapper.toResponse(updatedTemplate);
    }

    @Transactional
    public void deleteTemplate(UUID userId, UUID workspaceId, UUID templateId) {
        // Gatekeeper: Only users with MANAGE_TEMPLATES permission can do this
        permissionChecker.checkCanManageTemplates(userId, workspaceId);

        Template template = getActiveTemplate(workspaceId, templateId);
        
        // Soft Delete implementation
        template.setDeletedAt(LocalDateTime.now());

        User requester = currentUserProvider.getCurrentUser();
        auditLogService.logAction(template.getWorkspace(), requester, "TEMPLATE_DELETED", "Template_deleted");

        templateRepository.save(template);
    }

    // Helper method to ensure we don't fetch templates from other workspaces or deleted ones
    private Template getActiveTemplate(UUID workspaceId, UUID templateId) {
        return templateRepository.findByIdAndWorkspaceIdAndDeletedAtIsNull(templateId, workspaceId)
                .orElseThrow(() -> new NotFoundException("Template not found or has been deleted"));
    }
}