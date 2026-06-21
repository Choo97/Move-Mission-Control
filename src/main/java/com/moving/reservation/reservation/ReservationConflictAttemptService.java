package com.moving.reservation.reservation;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationConflictAttemptService {

    private final ReservationConflictAttemptRepository repository;

    public ReservationConflictAttemptService(ReservationConflictAttemptRepository repository) {
        this.repository = repository;
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
}
