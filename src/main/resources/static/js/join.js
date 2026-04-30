// 회원가입 전용 로직 (join.js)

document.addEventListener('DOMContentLoaded', function() {
    const idInput = document.getElementById('user_login_id');
    const idMsg = document.getElementById('id-check-msg');
    const pwInput = document.getElementById('password');
    const pwCheckInput = document.getElementById('password_check');
    const nicknameInput = document.getElementById('nickname');
    const errorWrapper = document.getElementById('error-message-wrapper');
    const errorText = document.getElementById('error-text');
    const joinForm = document.getElementById('joinForm');

    let isIdAvailable = false; // 아이디 중복 통과 여부

    // 1. 아이디 실시간 중복/형식 체크 (포커스 아웃 시)
    idInput.addEventListener('blur', function() {
        const loginId = this.value;
        const idRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{4,12}$/;

        if (loginId === "") {
            idMsg.textContent = "영어와 숫자를 조합하여 4~12자로 입력해주세요.";
            idMsg.style.color = "gray";
            isIdAvailable = false;
            return;
        }

        if (!idRegex.test(loginId)) {
            idMsg.textContent = "형식이 맞지 않습니다. (영어+숫자 조합 4~12자)";
            idMsg.style.color = "red";
            isIdAvailable = false;
            return;
        }

        fetch('/check_id?user_login_id=' + loginId)
            .then(res => res.json())
            .then(data => {
                idMsg.textContent = data.message;
                if (data.available) {
                    idMsg.style.color = "green";
                    isIdAvailable = true;
                } else {
                    idMsg.style.color = "red";
                    isIdAvailable = false;
                }
            })
            .catch(() => {
                idMsg.textContent = "서버 통신 오류";
                isIdAvailable = false;
            });
    });

    // 2. 가입 버튼 클릭 시 통합 검증[cite: 1]
    joinForm.addEventListener('submit', function(e) {
        e.preventDefault();

        errorWrapper.style.display = 'none';
        errorText.textContent = "";

        // 아이디 검사
        const idRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{4,12}$/;
        if (!idRegex.test(idInput.value)) {
            showError("아이디 형식이 올바르지 않습니다. (영어+숫자 4~12자)", idInput);
            return;
        }
        if (!isIdAvailable) {
            showError("아이디 중복 확인이 필요하거나 이미 사용 중인 아이디입니다.", idInput);
            return;
        }

        // 비밀번호 검사
        if (pwInput.value === "") {
            showError("비밀번호를 입력해주세요.", pwInput);
            return;
        }
        if (pwInput.value !== pwCheckInput.value) {
            showError("비밀번호가 서로 일치하지 않습니다.", pwCheckInput);
            return;
        }

        // 닉네임 검사
        if (nicknameInput.value.trim() === "") {
            showError("닉네임을 입력해주세요.", nicknameInput);
            return;
        }

        // 서버 전송 로직[cite: 1]
        const formData = new FormData(this);
        fetch('/join_process', {
            method: 'POST',
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert(data.message);
                window.location.href = '/';
            } else {
                showError(data.message);
            }
        })
        .catch(() => showError("서버 통신 중 오류가 발생했습니다."));
    });

    // 공통 에러 표시 함수[cite: 1]
    function showError(msg, targetInput) {
        errorText.textContent = msg;
        errorWrapper.style.display = 'block';
        if (targetInput) {
            targetInput.focus();
            targetInput.style.border = "2px solid red";
            setTimeout(() => targetInput.style.border = "", 2000);
        }
    }
});