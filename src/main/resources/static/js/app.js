document.addEventListener('DOMContentLoaded', () => {
    // Modal Open & Close Triggers
    document.querySelectorAll('[data-modal-target]').forEach(trigger => {
        trigger.addEventListener('click', (e) => {
            e.preventDefault();
            const modalId = trigger.getAttribute('data-modal-target');
            const modal = document.getElementById(modalId);
            if (modal) {
                modal.classList.add('active');
            }
        });
    });

    document.querySelectorAll('[data-modal-close]').forEach(trigger => {
        trigger.addEventListener('click', (e) => {
            e.preventDefault();
            const modal = trigger.closest('.modal-overlay');
            if (modal) {
                modal.classList.remove('active');
            }
        });
    });

    // Close modal on click outside
    document.querySelectorAll('.modal-overlay').forEach(overlay => {
        overlay.addEventListener('click', (e) => {
            if (e.target === overlay) {
                overlay.classList.remove('active');
            }
        });
    });

    // Close modal on Escape
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            document.querySelectorAll('.modal-overlay.active').forEach(modal => {
                modal.classList.remove('active');
            });
        }
    });

    // Setup auto-fill for Renewal Modal
    document.querySelectorAll('[data-action-seguimiento]').forEach(button => {
        button.addEventListener('click', (e) => {
            const renId = button.getAttribute('data-renovacion-id');
            const renCliente = button.getAttribute('data-renovacion-cliente');
            const hiddenInput = document.getElementById('modalSeguimientoRenovacionId');
            if (hiddenInput) {
                hiddenInput.value = renId;
            }
            const modal = document.getElementById('modalSeguimiento');
            if (modal) {
                modal.classList.add('active');
            }
        });
    });
});
