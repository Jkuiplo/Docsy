package com.google.docsy.feature.archive;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArchiveScheduler {

    private final ArchiveService archiveService;

    // Runs every day at midnight (00:00:00) server time
    @Scheduled(cron = "0 0 0 * * *")
    public void archiveApprovedDocuments() {
        archiveService.archiveExpiredApprovedDocuments();
    }
}