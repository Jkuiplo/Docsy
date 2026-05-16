package com.google.docsy.feature.documentVersion;

import com.google.docsy.feature.document.Document;
import com.google.docsy.feature.documentVersion.dto.DocumentVersionResponse;
import com.google.docsy.feature.documentVersion.mapper.DocumentVersionMapper;
import com.google.docsy.feature.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentVersionService {

    private final DocumentVersionRepository versionRepository;
    private final DocumentVersionMapper versionMapper;

    @Transactional
    public void createSnapshot(Document document, User author) {
        int nextVersion = versionRepository.findTopByDocumentIdOrderByVersionNumberDesc(document.getId())
                .map(v -> v.getVersionNumber() + 1)
                .orElse(1);

        DocumentVersion version = new DocumentVersion();
        version.setDocument(document);
        version.setVersionNumber(nextVersion);
        version.setTitleSnapshot(document.getTitle());
        version.setContentSnapshot(document.getContent());
        // For now, HTML snapshot is template snapshot. Will evolve when rendering logic is added.
        version.setRenderedHtmlSnapshot(document.getTemplateSnapshotHtml()); 
        version.setCreatedBy(author);

        versionRepository.save(version);
    }

    public List<DocumentVersionResponse> getDocumentVersions(UUID documentId) {
        return versionRepository.findByDocumentIdOrderByVersionNumberDesc(documentId).stream()
                .map(versionMapper::toResponse)
                .collect(Collectors.toList());
    }
}