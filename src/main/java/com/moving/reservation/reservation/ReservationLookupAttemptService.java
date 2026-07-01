package com.moving.reservation.reservation;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ReservationLookupAttemptService {

    private static final String UNKNOWN_CLIENT = "unknown";

    private final Map<String, AttemptBucket> attempts = new ConcurrentHashMap<>();
    private final int maxFailureCount;
    private final int failureWindowMinutes;
    private final int lockMinutes;

    public ReservationLookupAttemptService(
            @Value("${reservation.lookup.max-failure-count:5}") int maxFailureCount,
            @Value("${reservation.lookup.failure-window-minutes:10}") int failureWindowMinutes,
            @Value("${reservation.lookup.lock-minutes:10}") int lockMinutes) {
        this.maxFailureCount = Math.max(1, maxFailureCount);
        this.failureWindowMinutes = Math.max(1, failureWindowMinutes);
        this.lockMinutes = Math.max(1, lockMinutes);
    }

    public ReservationLookupAttemptResult checkAllowed(String clientKey) {
        String normalizedKey = normalizeKey(clientKey);
        AttemptBucket current = attempts.get(normalizedKey);

        if (current == null) {
            return ReservationLookupAttemptResult.allow();
        }

        LocalDateTime now = LocalDateTime.now();

        if (current.isLocked(now)) {
            return blockedResult(current, now);
        }

        if (current.isExpired(now, failureWindowMinutes)) {
            attempts.remove(normalizedKey, current);
        }

        return ReservationLookupAttemptResult.allow();
    }

    public ReservationLookupAttemptResult recordFailure(String clientKey) {
        String normalizedKey = normalizeKey(clientKey);
        LocalDateTime now = LocalDateTime.now();
        AttemptBucket updated = attempts.compute(normalizedKey, (key, current) -> {
            if (current == null || current.isExpired(now, failureWindowMinutes)) {
                return new AttemptBucket(1, now, null);
            }

            int nextFailureCount = current.failureCount() + 1;
            LocalDateTime lockedUntil = nextFailureCount >= maxFailureCount
                    ? now.plusMinutes(lockMinutes)
                    : null;
            return new AttemptBucket(nextFailureCount, current.firstFailedAt(), lockedUntil);
        });

        if (updated.isLocked(now)) {
            return blockedResult(updated, now);
        }

        return ReservationLookupAttemptResult.allow();
    }

    public void recordSuccess(String clientKey) {
        attempts.remove(normalizeKey(clientKey));
    }

    @Scheduled(fixedDelayString = "${reservation.lookup.cleanup-delay-minutes:30}", timeUnit = TimeUnit.MINUTES)
    public void cleanupExpiredAttempts() {
        LocalDateTime now = LocalDateTime.now();
        attempts.entrySet().removeIf(entry -> entry.getValue().isExpired(now, failureWindowMinutes));
    }

    private ReservationLookupAttemptResult blockedResult(AttemptBucket bucket, LocalDateTime now) {
        long retryAfterSeconds = Math.max(1, ChronoUnit.SECONDS.between(now, bucket.lockedUntil()));
        long retryAfterMinutes = Math.max(1, (long) Math.ceil(retryAfterSeconds / 60.0));
        return ReservationLookupAttemptResult.block(
                "예약 조회 시도가 많아 잠시 제한되었습니다. " + retryAfterMinutes + "분 후 다시 시도해 주세요.",
                retryAfterSeconds
        );
    }

    private String normalizeKey(String clientKey) {
        if (clientKey == null || clientKey.isBlank()) {
            return UNKNOWN_CLIENT;
        }

        return clientKey.trim();
    }

    private record AttemptBucket(
            int failureCount,
            LocalDateTime firstFailedAt,
            LocalDateTime lockedUntil
    ) {

        boolean isLocked(LocalDateTime now) {
            return lockedUntil != null && lockedUntil.isAfter(now);
        }

        boolean isExpired(LocalDateTime now, int failureWindowMinutes) {
            if (isLocked(now)) {
                return false;
            }

            if (lockedUntil != null && !lockedUntil.isAfter(now)) {
                return true;
            }

            return firstFailedAt.plusMinutes(failureWindowMinutes).isBefore(now);
        }
    }
}
