(function () {
    const form = document.querySelector('[data-estimate-preview-form]');
    const panel = document.querySelector('[data-estimate-preview-panel]');

    if (!form || !panel) {
        return;
    }

    const status = panel.querySelector('[data-estimate-preview-status]');
    const lines = panel.querySelector('[data-estimate-preview-lines]');
    const estimatedPrice = panel.querySelector('[data-estimate-preview-estimated]');
    const discountAmount = panel.querySelector('[data-estimate-preview-discount]');
    const finalPrice = panel.querySelector('[data-estimate-preview-final]');
    const couponName = panel.querySelector('[data-estimate-preview-coupon]');
    const formatter = new Intl.NumberFormat('ko-KR');
    let timer;

    function value(name) {
        const field = form.querySelector(`[name="${name}"]`);
        return field ? field.value.trim() : '';
    }

    function checked(name) {
        const field = form.querySelector(`[name="${name}"]`);
        return field ? field.checked : false;
    }

    function previewDistanceKm() {
        const field = form.querySelector('#previewDistanceKm');
        return field ? field.value.trim() : '';
    }

    function money(amount) {
        return `${formatter.format(amount)}원`;
    }

    function renderEmpty(message) {
        status.textContent = message;
        lines.innerHTML = '';
        estimatedPrice.textContent = '-';
        discountAmount.textContent = '-';
        finalPrice.textContent = '-';
        couponName.textContent = '';
    }

    function renderPreview(data) {
        status.textContent = '현재 입력값 기준 예상 견적입니다.';
        lines.innerHTML = '';

        data.lines.forEach((line) => {
            const item = document.createElement('li');
            const label = document.createElement('span');
            const amount = document.createElement('strong');
            label.textContent = line.label;
            amount.textContent = money(line.amount);
            item.append(label, amount);
            lines.appendChild(item);
        });

        estimatedPrice.textContent = money(data.estimatedPrice);
        discountAmount.textContent = money(data.discountAmount);
        finalPrice.textContent = money(data.finalEstimatedPrice);
        couponName.textContent = data.couponName ? `적용 쿠폰: ${data.couponName}` : '';
    }

    async function loadPreview() {
        const moveType = value('moveType');
        if (!moveType) {
            renderEmpty('이사 유형을 선택하면 예상 견적을 확인할 수 있습니다.');
            return;
        }

        const params = new URLSearchParams({
            moveType,
            fromElevator: checked('fromElevator'),
            toElevator: checked('toElevator'),
            fromFloor: value('fromFloor') || '1',
            toFloor: value('toFloor') || '1',
            fromLadderTruck: checked('fromLadderTruck'),
            toLadderTruck: checked('toLadderTruck'),
        });

        const distanceKm = previewDistanceKm();
        if (distanceKm) {
            params.set('distanceKm', distanceKm);
        }

        const couponCode = value('couponCode');
        if (couponCode) {
            params.set('couponCode', couponCode);
        }

        status.textContent = '예상 견적을 계산하는 중입니다.';

        try {
            const response = await fetch(`/reservations/estimate-preview?${params.toString()}`);
            if (!response.ok) {
                renderEmpty(await response.text());
                return;
            }

            renderPreview(await response.json());
        } catch (error) {
            renderEmpty('예상 견적을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.');
        }
    }

    function schedulePreview() {
        window.clearTimeout(timer);
        timer = window.setTimeout(loadPreview, 250);
    }

    form.addEventListener('input', schedulePreview);
    form.addEventListener('change', schedulePreview);
    loadPreview();
})();
