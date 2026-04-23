/* =========================
   GARUNG Slider.js
   - Drag Scroll
   - Momentum / Inertia
   - Mouse + Touch Support
========================= */

document.addEventListener("DOMContentLoaded", () => {

  const sliders = document.querySelectorAll(".slider-zone");

  sliders.forEach(slider => {
    let isDown = false;
    let startX;
    let scrollLeft;

    let velocity = 0;
    let momentumID;
    let lastX = 0;
    let lastTime = 0;

    /* =========================
       Mouse Events
    ========================= */
    slider.addEventListener("mousedown", (e) => {
      isDown = true;
      slider.classList.add("dragging");
      startX = e.pageX - slider.offsetLeft;
      scrollLeft = slider.scrollLeft;

      lastX = e.pageX;
      lastTime = Date.now();

      cancelMomentum();
    });

    slider.addEventListener("mouseleave", () => {
      if (isDown) stopDrag();
    });

    slider.addEventListener("mouseup", () => {
      if (isDown) stopDrag();
    });

    slider.addEventListener("mousemove", (e) => {
      if (!isDown) return;
      e.preventDefault();

      const x = e.pageX - slider.offsetLeft;
      const walk = (x - startX) * 1.2;
      slider.scrollLeft = scrollLeft - walk;

      trackVelocity(e.pageX);
    });

    /* =========================
       Touch Events
    ========================= */
    slider.addEventListener("touchstart", (e) => {
      isDown = true;
      startX = e.touches[0].pageX - slider.offsetLeft;
      scrollLeft = slider.scrollLeft;

      lastX = e.touches[0].pageX;
      lastTime = Date.now();

      cancelMomentum();
    }, { passive: true });

    slider.addEventListener("touchend", () => {
      if (isDown) stopDrag();
    });

    slider.addEventListener("touchmove", (e) => {
      if (!isDown) return;

      const x = e.touches[0].pageX - slider.offsetLeft;
      const walk = (x - startX) * 1.2;
      slider.scrollLeft = scrollLeft - walk;

      trackVelocity(e.touches[0].pageX);
    }, { passive: true });

    /* =========================
       Functions
    ========================= */

    function stopDrag() {
      isDown = false;
      slider.classList.remove("dragging");
      startMomentumScroll();
    }

    function trackVelocity(currentX) {
      const now = Date.now();
      const dx = currentX - lastX;
      const dt = now - lastTime;

      velocity = dx / dt;

      lastX = currentX;
      lastTime = now;
    }

    function startMomentumScroll() {
      cancelMomentum();

      momentumID = requestAnimationFrame(function momentum() {
        slider.scrollLeft -= velocity * 22;
        velocity *= 0.95; // 감속 (값 낮을수록 빨리 멈춤)

        if (Math.abs(velocity) > 0.02) {
          momentumID = requestAnimationFrame(momentum);
        }
      });
    }

    function cancelMomentum() {
      if (momentumID) cancelAnimationFrame(momentumID);
    }
  });

});