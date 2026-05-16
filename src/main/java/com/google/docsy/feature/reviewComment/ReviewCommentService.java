package com.google.docsy.feature.reviewComment;

import com.google.docsy.enums.ReviewCommentType;
import com.google.docsy.feature.document.Document;
import com.google.docsy.feature.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewCommentService {

    private final ReviewCommentRepository commentRepository;

    @Transactional
    public void addSystemComment(Document document, User author, ReviewCommentType type, String text) {
        ReviewComment comment = new ReviewComment();
        comment.setDocument(document);
        comment.setAuthor(author);
        comment.setType(type);
        comment.setCommentText(text != null ? text : getSystemMessage(type));
        
        commentRepository.save(comment);
    }

    private String getSystemMessage(ReviewCommentType type) {
        return switch (type) {
            case SUBMIT -> "Document submitted for review.";
            case APPROVE -> "Document approved.";
            case REJECT -> "Document rejected.";
            default -> "Comment added.";
        };
    }
}