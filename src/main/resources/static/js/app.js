document.addEventListener("DOMContentLoaded", () => {
    // Modal Open & Close Triggers
    document.querySelectorAll("[data-modal-target]").forEach((trigger) => {
        trigger.addEventListener("click", (e) => {
            e.preventDefault();
            const modalId = trigger.getAttribute("data-modal-target");
            const modal = document.getElementById(modalId);
            if (modal) {
                modal.classList.add("active");
            }
        });
    });

    document.querySelectorAll("[data-modal-close]").forEach((trigger) => {
        trigger.addEventListener("click", (e) => {
            e.preventDefault();
            const modal = trigger.closest(".modal-overlay");
            if (modal) {
                modal.classList.remove("active");
            }
        });
    });

    // Close modal on click outside
    document.querySelectorAll(".modal-overlay").forEach((overlay) => {
        overlay.addEventListener("click", (e) => {
            if (e.target === overlay) {
                overlay.classList.remove("active");
            }
        });
    });

    // Close modal on Escape
    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape") {
            document.querySelectorAll(".modal-overlay.active").forEach((modal) => {
                modal.classList.remove("active");
            });
        }
    });

    // Setup auto-fill for Renewal Modal
    document.querySelectorAll("[data-action-seguimiento]").forEach((button) => {
        button.addEventListener("click", (e) => {
            const renId = button.getAttribute("data-renovacion-id");
            const renCliente = button.getAttribute("data-renovacion-cliente");
            const hiddenInput = document.getElementById("modalSeguimientoRenovacionId");
            if (hiddenInput) {
                hiddenInput.value = renId;
            }
            const modal = document.getElementById("modalSeguimiento");
            if (modal) {
                modal.classList.add("active");
            }
        });
    });

    // Modal de propuestas de renovación:
    // funciona en Gestión de Renovaciones y Cliente 360°.
    const modalPropuesta = document.getElementById("modalPropuesta");

    if (modalPropuesta) {
        document.querySelectorAll("[data-action-propuesta]").forEach((boton) => {
            boton.addEventListener("click", () => {
                const datos = boton.dataset;

                document.getElementById("propuestaNombreCliente").textContent = datos.cliente || "";

                document.getElementById("propuestaNuevaFecha").value = datos.fecha || "";

                document.getElementById("propuestaMensaje").value = datos.mensaje || "";

                document.getElementById("formPropuesta").action = datos.urlPropuesta;

                document.getElementById("formAceptarContraoferta").action = datos.urlAceptar;

                const esContraoferta = datos.pendiente === "ADMIN" && Boolean(datos.fecha);

                const descripcion =
                    datos.pendiente === "CLIENTE"
                        ? "Esperando respuesta del cliente. Puedes modificar la propuesta."
                        : esContraoferta
                          ? "El cliente envió una contraoferta."
                          : datos.pendiente === "ADMIN"
                            ? "El cliente solicita una nueva propuesta."
                            : "Prepara una propuesta para el cliente.";

                document.getElementById("propuestaEstadoActual").textContent = descripcion;

                document.getElementById("formAceptarContraoferta").style.display = esContraoferta ? "block" : "none";

                modalPropuesta.classList.add("active");
            });
        });
    }
});
