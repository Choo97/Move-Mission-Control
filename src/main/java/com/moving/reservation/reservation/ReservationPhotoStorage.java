package com.moving.reservation.reservation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ReservationPhotoStorage {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp");
    private static final Map<String, Set<String>> ALLOWED_CONTENT_TYPES = Map.of(
            ".jpg", Set.of("image/jpeg"),
            ".jpeg", Set.of("image/jpeg"),
            ".png", Set.of("image/png"),
            ".webp", Set.of("image/webp")
    );

    private final Path storageDirectory;
    private final long maxFileSizeBytes;

    public ReservationPhotoStorage(
            @Value("${upload.reservation-photo.directory:uploads/reservation-photos}") String storageDirectory,
            @Value("${upload.reservation-photo.max-file-size-bytes:10485760}") long maxFileSizeBytes) {
        this.storageDirectory = Path.of(storageDirectory).toAbsolutePath().normalize();
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public StoredPhoto store(MultipartFile multipartFile) {
        ValidatedPhoto validatedPhoto = validate(multipartFile);
        String originalFilename = validatedPhoto.originalFilename();
        String extension = validatedPhoto.extension();
        String storedFilename = UUID.randomUUID() + extension;
        Path targetPath = storageDirectory.resolve(storedFilename).normalize();

        if (!targetPath.startsWith(storageDirectory)) {
            throw new IllegalStateException("짐 사진 저장 경로가 올바르지 않습니다.");
        }

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Files.createDirectories(storageDirectory);
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalStateException("짐 사진을 저장하는 중 문제가 발생했습니다.", exception);
        }

        return new StoredPhoto(originalFilename, storedFilename, "/uploads/reservation-photos/" + storedFilename);
    }

    public ValidatedPhoto validate(MultipartFile multipartFile) {
        String originalFilename = sanitizeOriginalFilename(multipartFile.getOriginalFilename());
        String extension = extractExtension(originalFilename);

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("짐 사진은 jpg, jpeg, png, webp 파일만 업로드할 수 있습니다.");
        }

        if (multipartFile.getSize() > maxFileSizeBytes) {
            throw new IllegalArgumentException("짐 사진은 파일당 최대 " + maxFileSizeMegabytes() + "MB까지 업로드할 수 있습니다.");
        }

        validateContentType(extension, multipartFile.getContentType());
        validateFileSignature(extension, multipartFile);

        return new ValidatedPhoto(originalFilename, extension);
    }

    private String sanitizeOriginalFilename(String submittedFilename) {
        String filename = submittedFilename == null ? "photo" : submittedFilename.trim();

        if (filename.isBlank()) {
            throw new IllegalArgumentException("파일 이름을 확인해 주세요.");
        }

        filename = filename.replace('\\', '/');
        int lastSeparatorIndex = filename.lastIndexOf("/");

        if (lastSeparatorIndex >= 0) {
            filename = filename.substring(lastSeparatorIndex + 1);
        }

        filename = filename.replaceAll("[\\p{Cntrl}<>:\"|?*]", "_").trim();

        if (filename.isBlank() || ".".equals(filename) || "..".equals(filename)) {
            throw new IllegalArgumentException("파일 이름을 확인해 주세요.");
        }

        return filename;
    }

    private String extractExtension(String filename) {
        int extensionStartIndex = filename.lastIndexOf(".");

        if (extensionStartIndex < 0) {
            return "";
        }

        return filename.substring(extensionStartIndex).toLowerCase(Locale.ROOT);
    }

    private void validateContentType(String extension, String contentType) {
        String normalizedContentType = contentType == null ? "" : contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);

        if (!ALLOWED_CONTENT_TYPES.get(extension).contains(normalizedContentType)) {
            throw new IllegalArgumentException("파일 확장자와 이미지 형식이 일치하지 않습니다.");
        }
    }

    private void validateFileSignature(String extension, MultipartFile multipartFile) {
        byte[] header;

        try (InputStream inputStream = multipartFile.getInputStream()) {
            header = inputStream.readNBytes(12);
        } catch (IOException exception) {
            throw new IllegalStateException("짐 사진을 읽는 중 문제가 발생했습니다.", exception);
        }

        boolean matched = switch (extension) {
            case ".jpg", ".jpeg" -> isJpeg(header);
            case ".png" -> isPng(header);
            case ".webp" -> isWebp(header);
            default -> false;
        };

        if (!matched) {
            throw new IllegalArgumentException("파일 내용이 이미지 형식과 일치하지 않습니다.");
        }
    }

    private boolean isJpeg(byte[] header) {
        return header.length >= 3
                && unsigned(header[0]) == 0xFF
                && unsigned(header[1]) == 0xD8
                && unsigned(header[2]) == 0xFF;
    }

    private boolean isPng(byte[] header) {
        return header.length >= 8
                && unsigned(header[0]) == 0x89
                && header[1] == 'P'
                && header[2] == 'N'
                && header[3] == 'G'
                && header[4] == 0x0D
                && header[5] == 0x0A
                && header[6] == 0x1A
                && header[7] == 0x0A;
    }

    private boolean isWebp(byte[] header) {
        return header.length >= 12
                && header[0] == 'R'
                && header[1] == 'I'
                && header[2] == 'F'
                && header[3] == 'F'
                && header[8] == 'W'
                && header[9] == 'E'
                && header[10] == 'B'
                && header[11] == 'P';
    }

    private int unsigned(byte value) {
        return value & 0xFF;
    }

    private long maxFileSizeMegabytes() {
        return Math.max(1, maxFileSizeBytes / 1024 / 1024);
    }

    public record ValidatedPhoto(String originalFilename, String extension) {
    }

    public record StoredPhoto(String originalFilename, String storedFilename, String fileUrl) {
    }
}
