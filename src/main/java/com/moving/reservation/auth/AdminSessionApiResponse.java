package com.moving.reservation.auth;

import java.util.List;

public record AdminSessionApiResponse(
        String username,
        List<String> roles
) {
}
