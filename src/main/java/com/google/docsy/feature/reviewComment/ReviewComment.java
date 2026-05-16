package com.google.docsy.feature.reviewComment;

import com.google.docsy.common.entity.BaseEntity;
import com.google.docsy.enums.ReviewCommentType;
import com.google.docsy.feature.document.Document;
import com.google.docsy.feature.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "review_comments")
public class ReviewComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReviewCommentType type;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String commentText;
}