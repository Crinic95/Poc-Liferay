(function () {
  const select = document.getElementById('invoiceTypeSelect');
  const clearBtn = document.getElementById('invoiceTypeClear');

  const url = new URL(window.location.href);
  const current = url.searchParams.get('invoiceType') || '';
  select.value = current;

  function applyFilter(value) {
    const next = new URL(window.location.href);

    if (value) {
      next.searchParams.set('invoiceType', value);
    } else {
      next.searchParams.delete('invoiceType');
    }
    next.searchParams.delete('p');
    next.searchParams.delete('delta');

    window.location.assign(next.toString());
  }

  select.addEventListener('change', () => applyFilter(select.value));
  clearBtn.addEventListener('click', () => applyFilter(''));
})();