package com.moving.reservation.reservation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ReservationPhotoStorage {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");

    private final Path storageDirectory;

    public ReservationPhotoStorage(@Value("${upload.reservation-photo.directory:uploads/reservation-photos}") String storageDirectory) {
        this.storageDirectory = Path.of(storageDirectory);
    }

    public StoredPhoto store(MultipartFile multipartFile) {
        String submittedFilename = multipartFile.getOriginalFilename() == null ? "photo" : multipartFile.getOriginalFilename();
        String originalFilename = Path.of(submittedFilename).getFileName().toString();
        String extension = extractExtension(originalFilename);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("짐 사진은 jpg, jpeg, png, webp 파일만 업로드할 수 있습니다.");
        }

        String storedFilename = UUID.randomUUID() + extension;

        try {
            Files.createDirectories(storageDirectory);
            multipartFile.transferTo(storageDirectory.resolve(storedFilename).toAbsolutePath());
        } catch (IOException exception) {
            throw new IllegalStateException("짐 사진을 저장하는 중 문제가 발생했습니다.", exception);
        }

        return new StoredPhoto(originalFilename, storedFilename, "/uploads/reservation-photos/" + storedFilename);
    }

    private String extractExtension(String filename) {
        int extensionStartIndex = filename.lastIndexOf(".");

        if (extensionStartIndex < 0) {
            return "";
        }

        return filename.substring(extensionStartIndex).toLowerCase(Locale.ROOT);
    }

    public record StoredPhoto(String originalFilename, String storedFilename, String fileUrl) {
    }
}
