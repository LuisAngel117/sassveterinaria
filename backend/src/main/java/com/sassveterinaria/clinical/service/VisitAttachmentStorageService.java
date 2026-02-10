package com.sassveterinaria.clinical.service;

import com.sassveterinaria.common.ApiProblemException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VisitAttachmentStorageService {

    private final VisitAttachmentProperties properties;

    public VisitAttachmentStorageService(VisitAttachmentProperties properties) {
        this.properties = properties;
    }

    public String store(UUID visitId, UUID attachmentId, MultipartFile file) {
        Path root = resolveRoot();
        Path visitDir = root.resolve("visits").resolve(visitId.toString());
        String extension = detectExtension(file);
        String fileName = attachmentId + extension;
        Path filePath = visitDir.resolve(fileName);

        try {
            Files.createDirectories(visitDir);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new ApiProblemException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "https://sassveterinaria.local/errors/attachment-storage-failed",
                "Attachment storage failed",
                "No se pudo guardar el adjunto en almacenamiento local.",
                "ATTACHMENT_STORAGE_FAILED"
            );
        }

        return "visits/" + visitId + "/" + fileName;
    }

    public Resource load(String relativePath) {
        Path root = resolveRoot();
        Path file = root.resolve(relativePath).normalize();
        if (!file.startsWith(root) || !Files.exists(file)) {
            throw new ApiProblemException(
                HttpStatus.NOT_FOUND,
                "https://sassveterinaria.local/errors/attachment-not-found",
                "Attachment not found",
                "No se encontro el archivo del adjunto.",
                "ATTACHMENT_NOT_FOUND"
            );
        }
        return new FileSystemResource(file);
    }

    public void delete(String relativePath) {
        Path root = resolveRoot();
        Path file = root.resolve(relativePath).normalize();
        if (!file.startsWith(root)) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException ex) {
            throw new ApiProblemException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "https://sassveterinaria.local/errors/attachment-delete-failed",
                "Attachment delete failed",
                "No se pudo eliminar el archivo del adjunto.",
                "ATTACHMENT_DELETE_FAILED"
            );
        }
    }

    private Path resolveRoot() {
        Path root = Path.of(properties.getStorageDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException ex) {
            throw new ApiProblemException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "https://sassveterinaria.local/errors/storage-dir-failed",
                "Storage directory failed",
                "No se pudo inicializar STORAGE_DIR.",
                "STORAGE_DIR_FAILED"
            );
        }
        return root;
    }

    private String detectExtension(MultipartFile file) {
        String original = file.getOriginalFilename();
        if (original != null) {
            int idx = original.lastIndexOf('.');
            if (idx >= 0 && idx < original.length() - 1) {
                String ext = original.substring(idx).toLowerCase();
                if (ext.matches("^\\.[a-z0-9]{1,5}$")) {
                    return ext;
                }
            }
        }

        String contentType = file.getContentType();
        if ("application/pdf".equalsIgnoreCase(contentType)) {
            return ".pdf";
        }
        if ("image/jpeg".equalsIgnoreCase(contentType)) {
            return ".jpg";
        }
        if ("image/png".equalsIgnoreCase(contentType)) {
            return ".png";
        }
        return "";
    }
}
