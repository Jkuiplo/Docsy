package com.google.docsy.feature.document.dto.request;

import lombok.Data;
import java.util.UUID;

@Data
public class CreateDocumentRequest {
    private String title;
    private String content;
    private UUID templateId; 
}