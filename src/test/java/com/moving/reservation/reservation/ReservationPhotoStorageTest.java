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
        ReservationPhotoStorage storage = new ReservationPhotoStorage(tempDirectory.toString());
        MockMultipartFile multipartFile = new MockMultipartFile(
                "itemPhotos",
                "living-room.JPG",
                "image/jpeg",
                "photo".getBytes()
        );

        ReservationPhotoStorage.StoredPhoto storedPhoto = storage.store(multipartFile);

        assertThat(storedPhoto.originalFilename()).isEqualTo("living-room.JPG");
        assertThat(storedPhoto.storedFilename()).endsWith(".jpg");
        assertThat(storedPhoto.fileUrl()).startsWith("/uploads/reservation-photos/");
        assertThat(Files.exists(tempDirectory.resolve(storedPhoto.storedFilename()))).isTrue();
    }

    @Test
    void 허용되지_않은_확장자의_파일은_저장하지_않는다() {
        ReservationPhotoStorage storage = new ReservationPhotoStorage(tempDirectory.toString());
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
}
