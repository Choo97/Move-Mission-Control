package com.moving.reservation.reservation;

import com.moving.reservation.config.PrivacyRetentionProperties;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationConflictAttemptService {

    private final ReservationConflictAttemptRepository repository;
    private final PrivacyRetentionProperties retentionProperties;

    public ReservationConflictAttemptService(ReservationConflictAttemptRepository repository,
                                             PrivacyRetentionProperties retentionProperties) {
        this.repository = repository;
        this.retentionProperties = retentionProperties;
    }

    @Transactional
    public void record(ReservationCreateRequest request) {
        repository.save(new ReservationConflictAttempt(
                request.getCustomerName(),
                request.getPhone(),
                request.getMoveDate(),
                request.getMoveTime()
        ));
    }

    @Transactional(readOnly = true)
    public List<ReservationConflictAttempt> findRecent() {
        return repository.findTop10ByOrderByAttemptedAtDesc();
    }

    @Scheduled(cron = "${privacy.retention.cleanup-cron:0 0 3 * * *}", zone = "Asia/Seoul")
    @Transactional
    public long deleteExpired() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionProperties.conflictAttemptDays());
        return repository.deleteByAttemptedAtBefore(cutoff);
    }
}
