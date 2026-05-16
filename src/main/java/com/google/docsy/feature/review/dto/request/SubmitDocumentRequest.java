package com.google.docsy.feature.review.dto.request;

import lombok.Data;
import java.util.UUID;

@Data
public class SubmitDocumentRequest {
    private UUID reviewerId; 
    private String comment;  
}