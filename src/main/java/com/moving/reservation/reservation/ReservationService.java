package com.moving.reservation.reservation;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public Reservation create(ReservationCreateRequest request) {
        return reservationRepository.save(request.toEntity());
    }

    public Reservation get(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("예약을 찾을 수 없습니다."));
    }

    public Reservation search(ReservationSearchRequest request) {
        return reservationRepository.findByIdAndPhone(request.getReservationId(), request.getPhone())
                .orElseThrow(() -> new IllegalArgumentException("예약 번호와 연락처가 일치하는 예약을 찾을 수 없습니다."));
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAllByOrderByMoveDateAscMoveTimeAsc();
    }

    @Transactional
    public void updateStatus(Long id, ReservationStatus status) {
        get(id).updateStatus(status);
    }

    @Transactional
    public void updateEstimate(Long id, Integer estimatedPrice) {
        get(id).updateEstimate(estimatedPrice);
    }

    public ReservationSummary summary() {
        return new ReservationSummary(
                reservationRepository.count(),
                reservationRepository.countByStatus(ReservationStatus.RECEIVED),
                reservationRepository.countByStatus(ReservationStatus.CONSULTING),
                reservationRepository.countByStatus(ReservationStatus.CONFIRMED)
        );
    }
}
