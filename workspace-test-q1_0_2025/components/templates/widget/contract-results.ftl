<#if !searchResultsPortletDisplayContext.isRenderNothing()>
	<#assign documents = searchResultsPortletDisplayContext.getDocuments() />

	<div class="contracts-search-results">
		<#if !documents?has_content>
			<div class="contracts-search-results__empty">
				Nessun risultato trovato.
			</div>
		<#else>
			<div class="contracts-search-results__grid">
				<#list documents as document>
					<#assign entry = searchResultsPortletDisplayContext.getSearchResultSummaryDisplayContext(document) />

					<#assign title = entry.getTitle()!"" />
					<#assign highlightedTitle = entry.getHighlightedTitle()!title />
					<#assign viewURL = entry.getViewURL()!"" />
					<#assign summary = entry.getContent()!"" />

					<#assign contractNumber = document.get("numeroContratto")!"" />
					<#assign contractYear = document.get("annoContratto")!"" />
					<#assign status = document.get("statoContratto")!"" />
					<#assign address = document.get("indirizzo")!"" />

					<#assign normalizedStatus = status?trim?upper_case />

					<div class="contract-card">
						<div class="contract-card__body">
							<div class="contract-card__icon">
								<i class="fa-solid fa-droplet"></i>
							</div>

							<div class="contract-card__top">
								<h2 class="contract-card__title">
									<a href="${viewURL}">
										${highlightedTitle}
									</a>
								</h2>

								<#if address?has_content>
									<p class="contract-card__address">${address}</p>
								<#elseif summary?has_content>
									<p class="contract-card__address">${summary}</p>
								</#if>
							</div>

							<div class="contract-card__bottom">
								<div class="contract-card__row contract-card__row--border">
									<span class="contract-card__label">Contratto</span>
									<span class="contract-card__value contract-card__value--contract">
										<#if contractYear?has_content && contractNumber?has_content>
											${contractYear}C${contractNumber}
										<#elseif contractNumber?has_content>
											${contractNumber}
										<#else>
											-
										</#if>
									</span>
								</div>

								<div class="contract-card__row">
									<span class="contract-card__label">Stato</span>

									<#if normalizedStatus == "ATTIVO">
										<span class="contract-card__status contract-card__status--active">${status}</span>
									<#elseif normalizedStatus == "CESSATO">
										<span class="contract-card__status contract-card__status--closed">${status}</span>
									<#else>
										<span class="contract-card__status">${status!'-'}</span>
									</#if>
								</div>

								<a class="contract-card__cta" href="${viewURL}">
									Monitora e gestisci
								</a>
							</div>
						</div>
					</div>
				</#list>
			</div>

			<#if searchResultsPortletDisplayContext.isShowPagination()>
				<div class="contracts-search-results__pagination">
					<@liferay_ui["search-paginator"] searchContainer=searchContainer />
				</div>
			</#if>
		</#if>
	</div>
</#if>