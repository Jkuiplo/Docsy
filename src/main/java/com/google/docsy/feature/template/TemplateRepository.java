package com.google.docsy.feature.template;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TemplateRepository extends JpaRepository<Template, UUID> {
    List<Template> findByWorkspaceIdAndDeletedAtIsNull(UUID workspaceId);
    
    Optional<Template> findByIdAndWorkspaceIdAndDeletedAtIsNull(UUID id, UUID workspaceId);
}