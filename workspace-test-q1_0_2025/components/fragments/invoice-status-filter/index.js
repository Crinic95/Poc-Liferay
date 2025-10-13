(function () {
  const select = document.getElementById('invoiceStatusSelect');
  const clearBtn = document.getElementById('invoiceStatusClear');

  const url = new URL(window.location.href);
  const current = url.searchParams.get('invoiceStatus') || '';
  select.value = current;

  function applyFilter(value) {
    const next = new URL(window.location.href);

    if (value) {
      next.searchParams.set('invoiceStatus', value);
    } else {
      next.searchParams.delete('invoiceStatus');
    }
    next.searchParams.delete('p');
    next.searchParams.delete('delta');

    window.location.assign(next.toString());
  }

  select.addEventListener('change', () => applyFilter(select.value));
  clearBtn.addEventListener('click', () => applyFilter(''));
})();