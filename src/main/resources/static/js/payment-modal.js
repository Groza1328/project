(function () {
    const modal = document.getElementById('paymentModal');
    if (!modal) {
        return;
    }

    const backdrop = document.getElementById('paymentModalBackdrop');
    const confirmBtn = document.getElementById('paymentConfirmBtn');
    const cancelBtn = document.getElementById('paymentModalCancelBtn') || document.getElementById('paymentCancelBtn');
    let pendingAction = null;

    function closePaymentModal() {
        modal.classList.remove('is-open');
        modal.setAttribute('aria-hidden', 'true');
        document.body.classList.remove('modal-open');
        pendingAction = null;
    }

    window.openPaymentModal = function (onConfirm) {
        pendingAction = onConfirm;
        modal.classList.add('is-open');
        modal.setAttribute('aria-hidden', 'false');
        document.body.classList.add('modal-open');
    };

    confirmBtn.addEventListener('click', function () {
        const action = pendingAction;
        closePaymentModal();
        if (typeof action === 'function') {
            action();
        }
    });

    if (cancelBtn) {
        cancelBtn.addEventListener('click', closePaymentModal);
    }
    if (backdrop) {
        backdrop.addEventListener('click', closePaymentModal);
    }

    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && modal.classList.contains('is-open')) {
            closePaymentModal();
        }
    });
})();
