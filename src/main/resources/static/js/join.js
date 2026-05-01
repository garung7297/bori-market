/**
 * 아이디/비밀번호 문자열 패턴 체크 (3자 이상 반복/연속)[cite: 2]
 */
function isWeakPattern(str) {
    for (let i = 0; i < str.length - 2; i++) {
        const char1 = str.charCodeAt(i);
        const char2 = str.charCodeAt(i + 1);
        const char3 = str.charCodeAt(i + 2);

        if (char1 === char2 && char2 === char3) {
            return { invalid: true, msg: "동일한 문자를 3번 이상 연속해서 적을 수 없습니다." };
        }
        if ((char2 === char1 + 1 && char3 === char2 + 1) ||
            (char2 === char1 - 1 && char3 === char2 - 1)) {
            return { invalid: true, msg: "연속된 숫자나 문자(3자 이상)를 사용할 수 없습니다." };
        }
    }
    return { invalid: false };
}

/**
 * 비밀번호 보이기/숨기기[cite: 3]
 */
function togglePassword(inputId, iconElement) {
    const targetInput = document.getElementById(inputId);
    if (targetInput.type === 'password') {
        targetInput.type = 'text';
        iconElement.textContent = '🔒';
    } else {
        targetInput.type = 'password';
        iconElement.textContent = '👁️';
    }
}

document.addEventListener('DOMContentLoaded', function() {
    const idInput = document.getElementById('user_login_id');
    const idMsg = document.getElementById('id-check-msg');
    const pwInput = document.getElementById('password');
    const pwMsg = document.getElementById('pw-msg');
    const pwCheckInput = document.getElementById('password_check');
    const pwCheckMsg = document.getElementById('pw-check-msg');
    const nicknameInput = document.getElementById('nickname');
    const emailInput = document.getElementById('email');
    const errorWrapper = document.getElementById('error-message-wrapper');
    const errorText = document.getElementById('error-text');
    const joinForm = document.getElementById('joinForm');

    let isIdAvailable = false;

    // 1. 아이디 실시간 검증[cite: 2]
    idInput.addEventListener('blur', function() {
        const loginId = this.value;
        const idRegex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{5,12}$/;

        idMsg.style.color = "red"; // 기본 오류 색상

        if (loginId === "") {
            idMsg.textContent = "영어와 숫자를 조합하여 5~12자로 입력해주세요.";
            idMsg.style.color = "gray";
            isIdAvailable = false;
            return;
        }

        // [형식 체크] 정규식 먼저 확인![cite: 2]
        if (!idRegex.test(loginId)) {
            idMsg.textContent = "형식이 맞지 않습니다. (영어+숫자 조합 5~12자)";
            isIdAvailable = false;
            return;
        }

        // [취약 패턴 체크][cite: 2]
        const patternCheck = isWeakPattern(loginId);
        if (patternCheck.invalid) {
            idMsg.textContent = "아이디에 " + patternCheck.msg;
            isIdAvailable = false;
            return;
        }

        // [중복 체크] 형식이 완벽할 때만 호출[cite: 2]
        fetch('/check_id?user_login_id=' + loginId)
            .then(res => res.json())
            .then(data => {
                if (data.available) {
                    idMsg.textContent = "사용 가능한 멋진 아이디네요! ✨";
                    idMsg.style.color = "green";
                    isIdAvailable = true;
                } else {
                    idMsg.textContent = "이미 사용 중인 아이디입니다. 😭";
                    isIdAvailable = false;
                }
            })
            .catch(() => {
                idMsg.textContent = "서버 통신 오류";
                isIdAvailable = false;
            });
    });

    // 2. 비밀번호 실시간 검증[cite: 3]
    pwInput.addEventListener('blur', function() {
        const pw = this.value;
        pwMsg.style.color = "red";
         if ( pwCheckInput.value ==="") {
               pwCheckMsg.textContent = "비밀번호 확인을 입력하세요.";
         }
         if (pw==pwCheckInput.value&& this.value.length > 4 ) {
            pwCheckMsg.textContent = "비밀번호가 일치합니다. ✨";
            pwCheckMsg.style.color = "green";
        }else{

            pwCheckMsg.textContent = "비밀번호가 일치하지 않습니다. 😭";
            pwCheckMsg.style.color = "red";
        }

        if (pw === "") {
            pwMsg.textContent = "비밀번호를 입력해주세요.";
            return;
        }
        if (pw.length < 5) {
            pwMsg.textContent = "최소 5글자 이상 입력해주세요.";
            return;
        }
        const pwPattern = isWeakPattern(pw);
        if (pwPattern.invalid) {
            pwMsg.textContent = pwPattern.msg;
            return;
        }
        pwMsg.textContent = "사용 가능한 비밀번호입니다. ✅";
        pwMsg.style.color = "green";
    });

    // 3. 비밀번호 일치 실시간 체크[cite: 3]
    pwCheckInput.addEventListener('input', function() {
        if (pwInput.value === this.value) {
            pwCheckMsg.textContent = "비밀번호가 일치합니다. ✨";
            pwCheckMsg.style.color = "green";
        }else {
            pwCheckMsg.textContent = "비밀번호가 일치하지 않습니다. 😭";
            pwCheckMsg.style.color = "red";
        }
        if (this.value === "" || pwInput.value ==="") {
            pwCheckMsg.textContent = "";
            return;
        }
    });

    // 4. 폼 전송 시 최종 검증[cite: 2]
    joinForm.addEventListener('submit', function(e) {
        e.preventDefault();
        errorWrapper.style.display = 'none';

        if (!isIdAvailable) {
            showError("아이디를 올바르게 입력하고 중복 확인을 완료해주세요.", idInput);
            return;
        }
        if (pwInput.value.length < 5 || isWeakPattern(pwInput.value).invalid) {
            showError("비밀번호 형식을 다시 확인해주세요.", pwInput);
            return;
        }
        if (pwInput.value !== pwCheckInput.value) {
            showError("비밀번호가 일치하지 않습니다.", pwCheckInput);
            return;
        }
        if (nicknameInput.value.trim() === "") {
            showError("닉네임을 입력해주세요.", nicknameInput);
            return;
        }
        const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
        if (!emailRegex.test(emailInput.value.trim())) {
            showError("올바른 이메일 형식을 입력해주세요.", emailInput);
            return;
        }

        const formData = new FormData(this);
        fetch('/join_process', { method: 'POST', body: formData })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert(data.message);
                window.location.href = '/';
            } else {
                showError(data.message);
            }
        });
    });

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