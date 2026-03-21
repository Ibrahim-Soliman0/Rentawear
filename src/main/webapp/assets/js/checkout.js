/**
 * Toggle the 'selected' class on address options.
 * Updates the hidden input so the servlet receives the chosen address id.
 */
function selectAddress(el, id) {
    document.querySelectorAll('.address-option').forEach(o => o.classList.remove('selected'));
    el.classList.add('selected');
    document.getElementById('selectedAddressId').value = id;
}

/**
 * Toggle the 'selected' class on payment options.
 * Updates the hidden input so the servlet receives the chosen payment id.
 */
function selectPayment(el, id) {
    document.querySelectorAll('.payment-option').forEach(o => o.classList.remove('selected'));
    el.classList.add('selected');
    document.getElementById('selectedPaymentId').value = id;
}