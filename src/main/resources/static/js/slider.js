document.addEventListener("DOMContentLoaded", () => {
  const track = document.getElementById("fruitSlider");
  if (!track) return;

  let speed = 0.5; // 작을수록 느림
  let isDown = false;
  let startX;
  let scrollLeft;

  // 자동 흐름
  function autoScroll() {
    if (!isDown) {
      track.scrollLeft += speed;

      // 끝까지 가면 처음으로
      if (track.scrollLeft + track.clientWidth >= track.scrollWidth) {
        track.scrollLeft = 0;
      }
    }
    requestAnimationFrame(autoScroll);
  }

  autoScroll();

  // 마우스 드래그
  track.addEventListener("mousedown", (e) => {
    isDown = true;
    startX = e.pageX - track.offsetLeft;
    scrollLeft = track.scrollLeft;
    track.style.cursor = "grabbing";
  });

  track.addEventListener("mouseleave", () => {
    isDown = false;
    track.style.cursor = "grab";
  });

  track.addEventListener("mouseup", () => {
    isDown = false;
    track.style.cursor = "grab";
  });

  track.addEventListener("mousemove", (e) => {
    if (!isDown) return;
    e.preventDefault();
    const x = e.pageX - track.offsetLeft;
    const walk = (x - startX) * 1.5;
    track.scrollLeft = scrollLeft - walk;
  });
});