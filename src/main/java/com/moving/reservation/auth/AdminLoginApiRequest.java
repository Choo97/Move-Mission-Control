package com.moving.reservation.auth;

public record AdminLoginApiRequest(
        String username,
        String password
) {
}
