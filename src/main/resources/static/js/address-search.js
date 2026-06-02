document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll("[data-address-target]").forEach(function (button) {
        button.addEventListener("click", function () {
            var targetId = button.getAttribute("data-address-target");
            var target = document.getElementById(targetId);

            if (!target) {
                return;
            }

            if (!window.kakao || !window.kakao.Postcode) {
                alert("주소 검색 서비스를 불러오지 못했습니다. 주소를 직접 입력해 주세요.");
                target.focus();
                return;
            }

            new window.kakao.Postcode({
                oncomplete: function (data) {
                    var address = data.userSelectedType === "R" ? data.roadAddress : data.jibunAddress;
                    var extraAddress = "";

                    if (data.userSelectedType === "R") {
                        if (data.bname && /[동로가]$/.test(data.bname)) {
                            extraAddress += data.bname;
                        }

                        if (data.buildingName && data.apartment === "Y") {
                            extraAddress += extraAddress ? ", " + data.buildingName : data.buildingName;
                        }

                        if (extraAddress) {
                            address += " (" + extraAddress + ")";
                        }
                    }

                    target.value = address;
                    target.focus();
                }
            }).open();
        });
    });
});
