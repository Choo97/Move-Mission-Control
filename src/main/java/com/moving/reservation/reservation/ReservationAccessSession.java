package com.moving.reservation.reservation;

import jakarta.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Set;

public final class ReservationAccessSession {

    private static final String AUTHENTICATED_RESERVATION_IDS = "AUTHENTICATED_RESERVATION_IDS";

    private ReservationAccessSession() {
    }

    public static void authorize(HttpSession session, Long reservationId) {
        authenticatedReservationIds(session).add(reservationId);
    }

    public static boolean isAuthorized(HttpSession session, Long reservationId) {
        return authenticatedReservationIds(session).contains(reservationId);
    }

    @SuppressWarnings("unchecked")
    private static Set<Long> authenticatedReservationIds(HttpSession session) {
        Object value = session.getAttribute(AUTHENTICATED_RESERVATION_IDS);

        if (value instanceof Set<?> authenticatedIds) {
            return (Set<Long>) authenticatedIds;
        }

        Set<Long> authenticatedIds = new HashSet<>();
        session.setAttribute(AUTHENTICATED_RESERVATION_IDS, authenticatedIds);
        return authenticatedIds;
    }
}
