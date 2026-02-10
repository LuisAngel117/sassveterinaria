package com.sassveterinaria.clinical.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VisitAttachmentProperties {

    @Value("${app.visit.storage-dir:${STORAGE_DIR:storage}}")
    private String storageDir;

    @Value("${app.visit.attachments.max-size-bytes:10485760}")
    private long maxSizeBytes;

    @Value("${app.visit.attachments.max-per-visit:5}")
    private int maxPerVisit;

    public String getStorageDir() {
        return storageDir;
    }

    public long getMaxSizeBytes() {
        return maxSizeBytes;
    }

    public int getMaxPerVisit() {
        return maxPerVisit;
    }
}
