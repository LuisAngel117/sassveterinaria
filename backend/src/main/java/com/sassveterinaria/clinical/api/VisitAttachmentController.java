package com.sassveterinaria.clinical.api;

import com.sassveterinaria.clinical.dto.VisitAttachmentResponse;
import com.sassveterinaria.clinical.service.VisitAttachmentService;
import com.sassveterinaria.security.AuthPrincipal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
public class VisitAttachmentController {

    private final VisitAttachmentService visitAttachmentService;

    public VisitAttachmentController(VisitAttachmentService visitAttachmentService) {
        this.visitAttachmentService = visitAttachmentService;
    }

    @PostMapping(value = "/visits/{id}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('VISIT_ATTACHMENT_UPLOAD')")
    public ResponseEntity<VisitAttachmentResponse> upload(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID visitId,
        @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(visitAttachmentService.upload(principal, visitId, file));
    }

    @GetMapping("/visits/{id}/attachments")
    @PreAuthorize("hasAuthority('VISIT_READ')")
    public ResponseEntity<List<VisitAttachmentResponse>> listByVisit(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID visitId
    ) {
        return ResponseEntity.ok(visitAttachmentService.listByVisit(principal, visitId));
    }

    @GetMapping("/attachments/{id}/download")
    @PreAuthorize("hasAuthority('VISIT_READ')")
    public ResponseEntity<Resource> download(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID attachmentId
    ) {
        VisitAttachmentService.DownloadResult result = visitAttachmentService.download(principal, attachmentId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
            .filename(result.originalFilename(), StandardCharsets.UTF_8)
            .build());

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (result.contentType() != null) {
            mediaType = MediaType.parseMediaType(result.contentType());
        }

        return ResponseEntity.ok()
            .headers(headers)
            .contentType(mediaType)
            .body(result.resource());
    }

    @DeleteMapping("/attachments/{id}")
    @PreAuthorize("hasAuthority('VISIT_UPDATE')")
    public ResponseEntity<Void> delete(
        @AuthenticationPrincipal AuthPrincipal principal,
        @PathVariable("id") UUID attachmentId
    ) {
        visitAttachmentService.delete(principal, attachmentId);
        return ResponseEntity.noContent().build();
    }
}
