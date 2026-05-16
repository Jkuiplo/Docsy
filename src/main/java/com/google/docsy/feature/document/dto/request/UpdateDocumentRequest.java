package com.google.docsy.feature.document.dto.request;

import lombok.Data;

@Data
public class UpdateDocumentRequest {
    private String title;
    private String content;
}