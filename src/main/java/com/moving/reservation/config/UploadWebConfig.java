package com.moving.reservation.config;

import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UploadWebConfig implements WebMvcConfigurer {

    private final Path reservationPhotoDirectory;

    public UploadWebConfig(@Value("${upload.reservation-photo.directory:uploads/reservation-photos}") String reservationPhotoDirectory) {
        this.reservationPhotoDirectory = Path.of(reservationPhotoDirectory);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/reservation-photos/**")
                .addResourceLocations(reservationPhotoLocation());
    }

    private String reservationPhotoLocation() {
        String location = reservationPhotoDirectory.toAbsolutePath().normalize().toUri().toString();
        return location.endsWith("/") ? location : location + "/";
    }
}
