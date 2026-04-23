

function format(command, value = null) {
    // 이 한 줄이 드래그된 영역에 명령을 내리는 핵심이야!
    document.execCommand(command, false, value);

    // 에디터에 다시 포커스를 줘서 바로 계속 글을 쓸 수 있게 해줘
    document.getElementById("editor").focus();
}





document.querySelector("form").addEventListener("submit", (e) => {
    const editor = document.getElementById("editor");
    const contentInput = document.getElementById("content");

    // 에디터 안의 모든 스타일 제거
    editor.querySelectorAll("*").forEach(el => {
        el.removeAttribute("style");
    });

    // 정제된 HTML을 hidden input에 저장
    contentInput.value = editor.innerHTML;

    // 값이 비었는지 체크 (가령이를 위한 안전장치!)
    if (contentInput.value.trim() === "") {
        alert("내용을 입력해주세요!");
        e.preventDefault();
    }
});



function toggleReplyForm(commentId) {
    const target = document.getElementById('reply-form-' + commentId);
    if (target.style.display === 'none') {
        target.style.display = 'block';
    } else {
        target.style.display = 'none';
    }
    //alert(commentId);
}

