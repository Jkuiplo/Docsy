package com.google.docsy.feature.template;

import com.google.docsy.common.security.UserPrincipal;
import com.google.docsy.feature.template.dto.request.CreateTemplateRequest;
import com.google.docsy.feature.template.dto.request.UpdateTemplateRequest;
import com.google.docsy.feature.template.dto.response.TemplateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @PostMapping
    public ResponseEntity<TemplateResponse> createTemplate(
            @PathVariable UUID workspaceId,
            @RequestBody CreateTemplateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(templateService.createTemplate(principal.getUser().getId(), workspaceId, request));
    }

    @GetMapping
    public ResponseEntity<List<TemplateResponse>> getTemplates(
            @PathVariable UUID workspaceId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(templateService.getTemplates(principal.getUser().getId(), workspaceId));
    }

    @GetMapping("/{templateId}")
    public ResponseEntity<TemplateResponse> getTemplate(
            @PathVariable UUID workspaceId,
            @PathVariable UUID templateId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(templateService.getTemplateById(principal.getUser().getId(), workspaceId, templateId));
    }

    @PatchMapping("/{templateId}")
    public ResponseEntity<TemplateResponse> updateTemplate(
            @PathVariable UUID workspaceId,
            @PathVariable UUID templateId,
            @RequestBody UpdateTemplateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(templateService.updateTemplate(principal.getUser().getId(), workspaceId, templateId, request));
    }

    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteTemplate(
            @PathVariable UUID workspaceId,
            @PathVariable UUID templateId,
            @AuthenticationPrincipal UserPrincipal principal) {
        templateService.deleteTemplate(principal.getUser().getId(), workspaceId, templateId);
        return ResponseEntity.noContent().build();
    }
}