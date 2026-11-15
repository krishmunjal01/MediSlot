/**
 * MediSlot UI & Interactive Scheduling Logic
 */

document.addEventListener("DOMContentLoaded", () => {
    // Quick-fill demo credentials on login page
    const demoButtons = document.querySelectorAll(".demo-login-fill");
    demoButtons.forEach(btn => {
        btn.addEventListener("click", () => {
            const user = btn.getAttribute("data-user");
            const pass = btn.getAttribute("data-pass");
            const userInput = document.getElementById("username");
            const passInput = document.getElementById("password");
            if (userInput && passInput) {
                userInput.value = user;
                passInput.value = pass;
                userInput.classList.add("highlight");
                passInput.classList.add("highlight");
                setTimeout(() => {
                    userInput.classList.remove("highlight");
                    passInput.classList.remove("highlight");
                }, 500);
            }
        });
    });

    // Dynamic slot loading for appointment booking
    const doctorSelect = document.getElementById("bookingDoctorSelect");
    const dateInput = document.getElementById("bookingDateInput");
    const slotContainer = document.getElementById("slotSelectionContainer");
    const selectedSlotInput = document.getElementById("selectedSlotIdInput");

    function fetchAvailableSlots() {
        if (!doctorSelect || !dateInput || !slotContainer) return;

        const doctorId = doctorSelect.value;
        const date = dateInput.value;

        if (!doctorId || !date) {
            slotContainer.innerHTML = '<p class="form-text">Select both a doctor and date to view available time slots.</p>';
            return;
        }

        slotContainer.innerHTML = '<p class="form-text">Checking live schedule availability...</p>';

        fetch(`/api/public/slots?doctorId=${doctorId}&date=${date}`)
            .then(res => {
                if (!res.ok) throw new Error("Could not retrieve doctor slots.");
                return res.json();
            })
            .then(slots => {
                if (!slots || slots.length === 0) {
                    slotContainer.innerHTML = `
                        <div class="alert-banner alert-warning" style="margin-top: 10px;">
                            <span>No open consultation slots on this date. You may select another date or join the waiting list.</span>
                        </div>`;
                    if (selectedSlotInput) selectedSlotInput.value = "";
                    return;
                }

                let html = '<div class="slot-grid">';
                slots.forEach(s => {
                    const timeLabel = s.startTime.substring(0, 5);
                    const isAvailable = s.status === 'AVAILABLE';
                    html += `
                        <button type="button" 
                                class="slot-btn" 
                                data-slot-id="${s.slotId}" 
                                ${!isAvailable ? 'disabled' : ''}>
                            ${timeLabel}
                        </button>`;
                });
                html += '</div>';
                slotContainer.innerHTML = html;

                // Bind click events on slot buttons
                const slotBtns = slotContainer.querySelectorAll(".slot-btn");
                slotBtns.forEach(btn => {
                    btn.addEventListener("click", () => {
                        slotBtns.forEach(b => b.classList.remove("selected"));
                        btn.classList.add("selected");
                        if (selectedSlotInput) {
                            selectedSlotInput.value = btn.getAttribute("data-slot-id");
                        }
                    });
                });
            })
            .catch(err => {
                slotContainer.innerHTML = `<p class="form-error">Error loading availability: ${err.message}</p>`;
            });
    }

    if (doctorSelect) {
        doctorSelect.addEventListener("change", fetchAvailableSlots);
    }
    if (dateInput) {
        dateInput.addEventListener("change", fetchAvailableSlots);
    }
});
