package com.moving.reservation.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class CorsPropertiesTest {

    @Test
    void setAllowedOriginsRemovesTrailingSlashesAndBlankValues() {
        CorsProperties properties = new CorsProperties();

        properties.setAllowedOrigins(List.of(
                "https://24nalpo.vercel.app/",
                " https://move-mission-control.vercel.app// ",
                ""
        ));

        assertThat(properties.getAllowedOrigins())
                .containsExactly(
                        "https://24nalpo.vercel.app",
                        "https://move-mission-control.vercel.app"
                );
    }
}
