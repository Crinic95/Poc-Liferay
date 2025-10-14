(function () {
  const root = typeof fragmentElement !== 'undefined' ? fragmentElement : document;
  const cfg = (typeof configuration === 'object' && configuration) ? configuration : {};

  const PRESETS = {
    status: {
      label: 'Stato fattura',
      param: 'invoiceStatus',
      options: [
        { label: (cfg.allLabel || 'Tutte'), value: '' },
        { label: 'Aperte', value: 'Aperta' },
        { label: 'Chiuse', value: 'Chiusa' }
      ]
    },
    type: {
      label: 'Tipo fattura',
      param: 'invoiceType',
      options: [
        { label: (cfg.allLabel || 'Tutte'), value: '' },
        { label: 'Pagate', value: 'Pagata' },
        { label: 'Scadute', value: 'Scaduta' }
      ]
    }
  };

  const RESET_PARAMS = ['p', 'delta', 'cur', 'start'];

  const presetKey = String(cfg.facetPreset || 'status').toLowerCase();
  const showClear = cfg.clearButton !== false;

  const labelEl = root.querySelector('.facet-filter__label');
  const selectEl = root.querySelector('.facet-filter__select');
  const clearBtn = root.querySelector('.facet-filter__clear');
  if (!labelEl || !selectEl) return;

  function parseOptionsString(s) {
    const raw = (s || '').trim();
    if (!raw) return [];
    const pairs = raw.split(/[\n;,]+/).map(x => x.trim()).filter(Boolean);
    return pairs.map(pair => {
      const [lbl, val] = pair.split('|');
      const label = (lbl || '').trim();
      const value = ((val || lbl || '') + '').trim();
      return { label: label || value, value: value || label };
    }).filter(o => o.label);
  }

  let labelText, paramName, options;

  if (presetKey === 'status' || presetKey === 'type') {
    const p = PRESETS[presetKey];
    labelText = p.label;
    paramName = p.param;
    options = p.options;
  } else {
    labelText = (cfg.label || 'Filtro').trim();
    paramName = (cfg.paramName || 'facet').trim();
    const all = (cfg.allLabel || 'Tutte').trim();

    const parsed = parseOptionsString(cfg.optionsString);
    options = [{ label: all, value: '' }, ...parsed];

    const seen = new Set();
    options = options.filter(o => {
      const k = `v:${o.value}`;
      if (seen.has(k)) return false;
      seen.add(k);
      return true;
    });
  }

  const uid = Math.random().toString(36).slice(2, 8);
  selectEl.id = `facet-select-${uid}`;
  labelEl.setAttribute('for', selectEl.id);
  labelEl.textContent = labelText;

  selectEl.innerHTML = '';
  options.forEach(({ label, value }) => {
    const opt = document.createElement('option');
    opt.value = value;
    opt.textContent = label;
    selectEl.appendChild(opt);
  });

  const url = new URL(window.location.href);
  selectEl.value = url.searchParams.get(paramName) || '';

  function applyFilter(value) {
    const next = new URL(window.location.href);
    if (value) next.searchParams.set(paramName, value);
    else next.searchParams.delete(paramName);

    RESET_PARAMS.forEach(p => next.searchParams.delete(p));

    window.location.assign(next.toString());
  }

  selectEl.addEventListener('change', () => applyFilter(selectEl.value));
  if (clearBtn) {
    clearBtn.style.display = showClear ? '' : 'none';
    clearBtn.addEventListener('click', () => applyFilter(''));
  }
})();