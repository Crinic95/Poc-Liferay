(function () {
  const root = typeof fragmentElement !== 'undefined' ? fragmentElement : document;
  const cfg = (typeof configuration === 'object' && configuration) ? configuration : {};

  const keywordParamId = String(cfg.keywordParamId || '').trim();
  const keywordParam = keywordParamId ? `filter_keywords_${keywordParamId}` : null;

  const PRESETS = {
    supplystatus: {
      label: 'Stato fornitura',
      param: keywordParam,
      options: [
        { label: cfg.allLabel || 'Tutte', value: '' },
        { label: 'Attive', value: 'ATTIVO' },
        { label: 'Cessate', value: 'CESSATO' }
      ]
    },
    billstatus: {
      label: 'Stato bolletta',
      param: 'billStatus',
      options: [
        { label: cfg.allLabel || 'Stato', value: '' },
        { label: 'Pagata', value: 'PAGATA' },
        { label: 'Da pagare', value: 'DA PAGARE' },
        { label: 'Da contabilizzare', value: 'DA CONTABILIZZARE' },
        { label: 'Da addebitare', value: 'DA ADDEBITARE' }
      ]
    }
  };

  const RESET_PARAMS = ['p', 'delta', 'cur', 'start', 'page', 'pageSize'];
  const presetKey = String(cfg.facetPreset || 'supplystatus');

  const preset = PRESETS[presetKey];
  if (!preset) return;

  if (!preset.param) {
    console.warn('[dd-facet] Missing keywordParamId configuration for supplystatus');
    return;
  }

  const selectEl = root.querySelector('.facet-filter__select');
  if (!selectEl) return;

  selectEl.setAttribute('aria-label', preset.label);

  selectEl.innerHTML = '';
  preset.options.forEach(({ label, value }) => {
    const opt = document.createElement('option');
    opt.value = value;
    opt.textContent = label;
    selectEl.appendChild(opt);
  });

  const url = new URL(window.location.href);
  const current = url.searchParams.get(preset.param) || '';
  selectEl.value = preset.options.some(o => o.value === current) ? current : '';

  selectEl.addEventListener('change', () => {
    const next = new URL(window.location.href);

    if (selectEl.value) next.searchParams.set(preset.param, selectEl.value);
    else next.searchParams.delete(preset.param);

    RESET_PARAMS.forEach(p => next.searchParams.delete(p));

    if (window.Liferay?.Util?.navigate) {
      Liferay.Util.navigate(next.toString());
    } else {
      window.location.assign(next.toString());
    }
  });
})();