<#-- Se voglio disegnarlo diretto nel template -->
<#--<section class="cards-slider" aria-label="Forniture">
  <div class="cards-slider__scroller" role="list">
    <#list items as it>
      <#assign _id     = it.invoiceId?string />
      <#assign _user   = it.userId?string />
      <#assign _value  = (it.invoiceValue?has_content)?then(it.invoiceValue?string, "") />
      <#assign _type   = (it.type)!"" />
      <#assign _status = (it.status)!"" />

      <article class="supply-card cards-slider__slide" role="listitem">
        <header class="supply-card__header">
          <div class="supply-card__icon" aria-hidden="true">
            <svg viewBox="0 0 64 64" class="drop">
              <path d="M32 6c8.5 12.2 18 22.6 18 32.1C50 47.9 41 56 32 56S14 47.9 14 38.1C14 28.6 23.5 18.2 32 6z"/>
            </svg>
          </div>
        </header>

        <h3 class="supply-card__title">${_id?html}</h3>
        <p class="supply-card__address">${_user?html}</p>

        <hr class="supply-card__divider" />

        <dl class="supply-card__meta">
          <div class="meta__row">
            <dt class="meta__label">Stato</dt>
            <dd class="meta__value"><span class="pill">${_status?html}</span></dd>
          </div>
          <#if _value?has_content>
            <div class="meta__row">
              <dt class="meta__label">Valore</dt>
              <dd class="meta__value">${_value?html}</dd>
            </div>
          </#if>
          <#if _type?has_content>
            <div class="meta__row">
              <dt class="meta__label">Tipo</dt>
              <dd class="meta__value">${_type?html}</dd>
            </div>
          </#if>
        </dl>

        <a class="supply-card__cta" href="#">MONITORA E GESTISCI</a>
      </article>
    </#list>
  </div>
</section>

<style>
/* Wrapper slider */
.cards-slider{--gap:24px;--slides:1;--pad:12px;max-width:1200px;margin-inline:auto;overflow:hidden}
.cards-slider__scroller{
  display:grid;grid-auto-flow:column;gap:var(--gap);
  grid-auto-columns:calc((100%-(var(--gap)*(var(--slides)-1)))/var(--slides));
  padding-inline:var(--pad);
  overflow-x:auto;scroll-snap-type:x mandatory;scroll-padding-inline:var(--pad);
  -webkit-overflow-scrolling:touch;scrollbar-width:none}
.cards-slider__scroller::-webkit-scrollbar{display:none}
.cards-slider__slide{scroll-snap-align:start;min-width:0;max-width:560px}
@media(min-width:900px){.cards-slider{--slides:2}}
@media(min-width:1400px){.cards-slider{--slides:3}}

/* Card compact */
.supply-card{display:grid;gap:.4rem;border:1px solid #e6eef6;border-radius:12px;background:#fff;padding:14px 16px}
.supply-card__header{display:flex;align-items:center;justify-content:space-between}
.supply-card__icon{width:52px;height:52px;border-radius:12px;background:#eef6ff;display:grid;place-items:center}
.drop{width:24px;height:24px;fill:#0a77d5}
.supply-card__title{margin:.1rem 0 0;font-size:1.05rem;line-height:1.2;color:#0a2b55;font-weight:800}
.supply-card__address{margin:0;color:#5c6b7a;font-size:.9rem;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.supply-card__divider{border:0;border-top:1px solid #e9edf2;margin:.35rem 0}
.supply-card__meta{display:grid;gap:.35rem;margin:0}
.meta__row{display:flex;align-items:center;justify-content:space-between;gap:.75rem}
.meta__label{color:#5c6b7a;font-size:.9rem}
.meta__value{font-weight:700;color:#0a2b55}
.pill{display:inline-flex;align-items:center;gap:.4rem;font-size:.8rem;font-weight:800;padding:.25rem .55rem;border-radius:999px;background:#e7f6ec;color:#1d7a4b}
.supply-card__cta{margin-top:.35rem;display:inline-block;width:100%;text-align:center;background:#0a5aa6;color:#fff;font-weight:800;padding:.6rem .8rem;border-radius:10px;text-decoration:none}

</style>-->

<div class="invoices-context" data-all-url="https://www.test.com" hidden></div>