package com.moving.reservation.reservation;

import com.moving.reservation.api.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "고객 예약 API", description = "React 같은 별도 프론트엔드에서 사용할 고객 예약 신청, 조회, 수정, 취소 API")
public class ReservationApiController {

    private final ReservationService reservationService;
    private final ReservationLookupAttemptService lookupAttemptService;

    public ReservationApiController(ReservationService reservationService,
                                    ReservationLookupAttemptService lookupAttemptService) {
        this.reservationService = reservationService;
        this.lookupAttemptService = lookupAttemptService;
    }

    @Operation(
            summary = "예약 신청",
            description = "고객이 이사 예약을 신청합니다. 신청이 성공하면 접수 상태의 예약 상세 정보를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "예약 신청 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류 또는 예약 생성 불가")
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@Valid @RequestBody ReservationApiCreateRequest request,
                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(ApiErrorResponse.badRequest(firstErrorMessage(bindingResult)));
        }

        try {
            Reservation reservation = reservationService.create(request.toServiceRequest());
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(reservation));
        } catch (IllegalArgumentException | IllegalStateException | ReservationScheduleConflictException exception) {
            return ResponseEntity.badRequest().body(ApiErrorResponse.badRequest(exception.getMessage()));
        }
    }

    @Operation(
            summary = "예약 조회",
            description = "예약 번호와 예약 당시 연락처가 일치하는 고객 예약을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "예약 조회 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류"),
            @ApiResponse(responseCode = "404", description = "예약 번호와 연락처가 일치하는 예약 없음"),
            @ApiResponse(responseCode = "429", description = "예약 조회 시도 제한")
    })
    @PostMapping("/search")
    public ResponseEntity<?> search(@Valid @RequestBody ReservationSearchRequest request,
                                    BindingResult bindingResult,
                                    HttpServletRequest servletRequest) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(ApiErrorResponse.badRequest(firstErrorMessage(bindingResult)));
        }

        String clientIp = ReservationLookupClientInfo.from(servletRequest).ipAddress();
        ReservationLookupAttemptResult allowed = lookupAttemptService.checkAllowed(clientIp);

        if (!allowed.allowed()) {
            return tooManyRequests(allowed);
        }

        try {
            Reservation reservation = reservationService.search(request);
            lookupAttemptService.recordSuccess(clientIp);
            return ResponseEntity.ok(toResponse(reservation));
        } catch (IllegalArgumentException exception) {
            ReservationLookupAttemptResult failed = lookupAttemptService.recordFailure(clientIp);

            if (!failed.allowed()) {
                return tooManyRequests(failed);
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiErrorResponse.notFound(exception.getMessage()));
        }
    }

    @Operation(
            summary = "예약 수정",
            description = "예약 번호와 연락처를 확인한 뒤 고객의 예약 수정 요청을 접수합니다. 실제 예약 정보는 관리자가 승인한 뒤 반영됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "예약 수정 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류, 연락처 불일치 또는 수정 불가 상태")
    })
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> update(@Parameter(description = "수정할 예약 번호", example = "1") @PathVariable Long id,
                                    @Valid @RequestBody ReservationUpdateRequest request,
                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(ApiErrorResponse.badRequest(firstErrorMessage(bindingResult)));
        }

        try {
            reservationService.requestUpdateDetails(id, request);
            Reservation reservation = reservationService.get(id);
            return ResponseEntity.ok(toResponse(reservation));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(ApiErrorResponse.badRequest(exception.getMessage()));
        }
    }

    @Operation(
            summary = "예약 취소",
            description = "예약 번호와 연락처를 확인한 뒤 고객의 예약 취소 요청을 접수합니다. 실제 취소 상태는 관리자가 승인한 뒤 반영됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "예약 취소 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류, 연락처 불일치 또는 취소 불가 상태")
    })
    @PostMapping(value = "/{id}/cancel", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> cancel(@Parameter(description = "취소할 예약 번호", example = "1") @PathVariable Long id,
                                    @Valid @RequestBody ReservationApiCancelRequest request,
                                    BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(ApiErrorResponse.badRequest(firstErrorMessage(bindingResult)));
        }

        try {
            reservationService.requestCancel(id, request.getPhone());
            Reservation reservation = reservationService.get(id);
            return ResponseEntity.ok(toResponse(reservation));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(ApiErrorResponse.badRequest(exception.getMessage()));
        }
    }

    @Operation(
            summary = "견적 동의",
            description = "고객이 받은 최종 견적에 동의하고 예약을 확정 상태로 변경합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "견적 동의 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류, 연락처 불일치 또는 견적 동의 불가 상태")
    })
    @PostMapping(value = "/{id}/estimate/accept", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> acceptEstimate(@Parameter(description = "견적에 동의할 예약 번호", example = "1") @PathVariable Long id,
                                            @Valid @RequestBody ReservationApiEstimateAcceptRequest request,
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(ApiErrorResponse.badRequest(firstErrorMessage(bindingResult)));
        }

        try {
            reservationService.acceptEstimate(id, request.getPhone());
            Reservation reservation = reservationService.get(id);
            return ResponseEntity.ok(toResponse(reservation));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(ApiErrorResponse.badRequest(exception.getMessage()));
        }
    }

    @Operation(
            summary = "짐 사진 업로드",
            description = "예약 번호와 연락처를 확인한 뒤 고객이 이삿짐 사진을 업로드합니다. jpg, jpeg, png, webp 파일을 사용할 수 있으며 파일 형식, 용량, 업로드 개수를 검증합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "짐 사진 업로드 성공"),
            @ApiResponse(responseCode = "400", description = "연락처 불일치, 파일 형식 오류 또는 업로드 불가 상태")
    })
    @PostMapping(value = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPhotos(@Parameter(description = "사진을 업로드할 예약 번호", example = "1") @PathVariable Long id,
                                          @Parameter(description = "예약 당시 입력한 연락처", example = "010-1234-5678") @RequestParam String phone,
                                          @Parameter(description = "업로드할 짐 사진 파일 목록") @RequestParam("photos") List<MultipartFile> photos) {
        try {
            List<ReservationApiPhotoResponse> responses = reservationService.addPhotos(id, phone, photos).stream()
                    .map(ReservationApiPhotoResponse::from)
                    .toList();
            return ResponseEntity.status(HttpStatus.CREATED).body(responses);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return ResponseEntity.badRequest().body(ApiErrorResponse.badRequest(exception.getMessage()));
        }
    }

    private ResponseEntity<ApiErrorResponse> tooManyRequests(ReservationLookupAttemptResult result) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(result.retryAfterSeconds()))
                .body(ApiErrorResponse.tooManyRequests(result.message()));
    }

    private String firstErrorMessage(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("요청 정보를 확인해 주세요.");
    }

    private ReservationApiResponse toResponse(Reservation reservation) {
        return ReservationApiResponse.from(
                reservation,
                reservationService.estimateLines(reservation),
                reservationService.findPhotos(reservation.getId()),
                reservationService.findCustomerRequests(reservation.getId())
        );
    }
}
