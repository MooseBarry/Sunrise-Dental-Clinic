document.addEventListener("click", function (event) {
    const printButton = event.target.closest("[data-print]");
    if (printButton) {
        window.print();
    }
});

const appointmentForm = document.getElementById("appointment-form");
const availabilityButton = document.getElementById("check-availability");
const availabilityResult = document.getElementById("availability-result");

if (appointmentForm && availabilityButton && availabilityResult) {
    availabilityButton.addEventListener("click", async function () {
        const dentistId = appointmentForm.elements.dentistId.value;
        const date = appointmentForm.elements.appointmentDate.value;
        const startTime = appointmentForm.elements.startTime.value;
        const durationMinutes = appointmentForm.elements.durationMinutes.value;

        if (!dentistId || !date || !startTime || !durationMinutes) {
            availabilityResult.textContent =
                "Select a dentist, date, time and duration first.";
            availabilityResult.className = "availability-result unavailable";
            return;
        }

        const parameters = new URLSearchParams({
            dentistId,
            date,
            startTime,
            durationMinutes
        });

        availabilityButton.disabled = true;
        availabilityResult.textContent = "Checking availability…";
        availabilityResult.className = "availability-result";

        try {
            const response = await fetch(
                appointmentForm.dataset.availabilityUrl + "?" + parameters,
                {headers: {"Accept": "application/json"}}
            );
            const result = await response.json();
            availabilityResult.textContent = result.available
                ? "Available — this full time period is currently free."
                : "Unavailable — this period overlaps another appointment.";
            availabilityResult.className = result.available
                ? "availability-result available"
                : "availability-result unavailable";
        } catch (error) {
            availabilityResult.textContent =
                "Availability could not be checked. You can still submit for the final server check.";
            availabilityResult.className = "availability-result unavailable";
        } finally {
            availabilityButton.disabled = false;
        }
    });
}
