package com.moving.reservation.privacy;

import com.moving.reservation.reservation.Reservation;
import com.moving.reservation.reservation.ReservationRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ReservationPrivacyMigration implements ApplicationRunner {

    private final ReservationRepository reservationRepository;
    private final PrivacyHashService privacyHashService;

    public ReservationPrivacyMigration(ReservationRepository reservationRepository,
                                       PrivacyHashService privacyHashService) {
        this.reservationRepository = reservationRepository;
        this.privacyHashService = privacyHashService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        reservationRepository.findAll().stream()
                .filter(reservation -> reservation.getPhoneHash() == null || reservation.getPhoneHash().isBlank())
                .forEach(this::fillPhoneHash);
    }

    private void fillPhoneHash(Reservation reservation) {
        reservation.updatePhoneHash(privacyHashService.phoneHash(reservation.getPhone()));
    }
}
