package com.moving.reservation.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class ReservationPhotoStorageTest {

    @TempDir
    private Path tempDirectory;

    @Test
    void 허용된_확장자의_사진파일을_저장한다() {
        ReservationPhotoStorage storage = storage();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "itemPhotos",
                "living-room.JPG",
                "image/jpeg",
                jpegBytes()
        );

        ReservationPhotoStorage.StoredPhoto storedPhoto = storage.store(multipartFile);

        assertThat(storedPhoto.originalFilename()).isEqualTo("living-room.JPG");
        assertThat(storedPhoto.storedFilename()).endsWith(".jpg");
        assertThat(storedPhoto.fileUrl()).startsWith("/uploads/reservation-photos/");
        assertThat(Files.exists(tempDirectory.resolve(storedPhoto.storedFilename()))).isTrue();
    }

    @Test
    void 허용되지_않은_확장자의_파일은_저장하지_않는다() {
        ReservationPhotoStorage storage = storage();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "itemPhotos",
                "memo.txt",
                "text/plain",
                "not-image".getBytes()
        );

        assertThatThrownBy(() -> storage.store(multipartFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("짐 사진은 jpg, jpeg, png, webp 파일만 업로드할 수 있습니다.");
    }

    @Test
    void 원본파일명에_경로가_포함되면_파일명만_저장한다() {
        ReservationPhotoStorage storage = storage();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "itemPhotos",
                "C:\\temp\\boxes.png",
                "image/png",
                pngBytes()
        );

        ReservationPhotoStorage.StoredPhoto storedPhoto = storage.store(multipartFile);

        assertThat(storedPhoto.originalFilename()).isEqualTo("boxes.png");
        assertThat(storedPhoto.storedFilename()).endsWith(".png");
    }

    @Test
    void 확장자와_이미지형식이_다르면_저장하지_않는다() {
        ReservationPhotoStorage storage = storage();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "itemPhotos",
                "boxes.jpg",
                "image/png",
                pngBytes()
        );

        assertThatThrownBy(() -> storage.store(multipartFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("파일 확장자와 이미지 형식이 일치하지 않습니다.");
    }

    @Test
    void 이미지_시그니처가_다르면_저장하지_않는다() {
        ReservationPhotoStorage storage = storage();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "itemPhotos",
                "boxes.jpg",
                "image/jpeg",
                "<html></html>".getBytes()
        );

        assertThatThrownBy(() -> storage.store(multipartFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("파일 내용이 이미지 형식과 일치하지 않습니다.");
    }

    @Test
    void 최대용량을_넘으면_저장하지_않는다() {
        ReservationPhotoStorage storage = new ReservationPhotoStorage(tempDirectory.toString(), 3);
        MockMultipartFile multipartFile = new MockMultipartFile(
                "itemPhotos",
                "boxes.jpg",
                "image/jpeg",
                jpegBytes()
        );

        assertThatThrownBy(() -> storage.store(multipartFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("짐 사진은 파일당 최대");
    }

    private ReservationPhotoStorage storage() {
        return new ReservationPhotoStorage(tempDirectory.toString(), 10 * 1024 * 1024);
    }

    private byte[] jpegBytes() {
        return new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
    }

    private byte[] pngBytes() {
        return new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
    }
}
