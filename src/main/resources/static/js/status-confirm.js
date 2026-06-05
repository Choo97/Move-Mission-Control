document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("[data-status-confirm]").forEach((form) => {
        form.addEventListener("submit", (event) => {
            const statusSelect = form.querySelector("select[name='status']");
            const selectedStatus = statusSelect?.selectedOptions?.[0]?.textContent?.trim() || "선택한 상태";
            const reservationId = form.dataset.reservationId;
            const prefix = reservationId ? `예약 ${reservationId}번을 ` : "";
            const message = `${prefix}'${selectedStatus}' 상태로 변경할까요?\n상태 변경은 고객 안내와 운영 이력에 영향을 줄 수 있습니다.`;

            if (!window.confirm(message)) {
                event.preventDefault();
            }
        });
    });
});
