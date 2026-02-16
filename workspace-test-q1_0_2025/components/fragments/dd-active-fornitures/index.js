(function () {
  const root = typeof fragmentElement !== 'undefined' ? fragmentElement : document;

  const link = root.querySelector('[data-lfr-editable-id="linkTutteForn"]');
  const title = root.querySelector('[data-lfr-editable-id="titleCardHome"]');

  function findBridge() {
    return (
      root.querySelector('.cf-source .invoices-context') ||
      document.querySelector('.invoices-context')
    );
  }

  function applyFromBridge(ctx) {
    if (!ctx) return false;

    const allUrl = ctx.getAttribute('data-all-url');
    if (link && allUrl) {
      const current = link.getAttribute('href');
      if (!current || current === '' || current === '#') {
        link.setAttribute('href', allUrl);
        if (link.getAttribute('target') === '_blank') {
          link.setAttribute('rel', 'noopener');
        }
      }
    }

    const countRaw = ctx.getAttribute('data-active-count');
    const count = Number.parseInt(countRaw, 10);
    if (title && Number.isFinite(count)) {
      const currentText = title.textContent.trim();

      if (/\d+/.test(currentText)) {
        title.textContent = currentText.replace(/\d+/, count.toString());
      } else {
        title.textContent = `${currentText} ${count}`;
      }
    }

    return true;
  }

  if (applyFromBridge(findBridge())) return;

  const source = root.querySelector('.cf-source') || document.body;
  const obs = new MutationObserver(() => {
    if (applyFromBridge(findBridge())) obs.disconnect();
  });
  obs.observe(source, { childList: true, subtree: true });

  setTimeout(() => obs.disconnect(), 10000);
})();
