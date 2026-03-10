<#assign facetParamName = customFacetDisplayContext.getParameterName()!"">
<#if !facetParamName?has_content>
	<#assign facetParamName = "stato">
</#if>

<@liferay_ui["panel-container"]
	extended=true
	id="${namespace + 'facetCustomPanelContainer'}"
	markupView="lexicon"
	persistState=true
>
	<@liferay_ui.panel
		collapsible=true
		cssClass="search-facet search-facet-dropdown"
		id="${namespace + 'facetCustomPanel'}"
		markupView="lexicon"
		persistState=true
		title="${customFacetDisplayContext.getDisplayCaption()}"
	>
		<div class="facet-dropdown">
			<label class="facet-dropdown__label" for="${namespace}facetSelect">
				${customFacetDisplayContext.getDisplayCaption()}
			</label>

			<select
				class="facet-dropdown__select"
				data-facet-param="${htmlUtil.escapeAttribute(facetParamName)}"
				id="${namespace}facetSelect"
			>
				<option
					value=""
					${customFacetDisplayContext.isNothingSelected()?then('selected', '')}
				>
					Tutte
				</option>

				<#if entries?has_content>
					<#list entries as entry>
						<option
							value="${htmlUtil.escapeAttribute(entry.getBucketText())}"
							${entry.isSelected()?then('selected', '')}
						>
							${htmlUtil.escape(entry.getBucketText())}
							<#if entry.isFrequencyVisible()>
								(${entry.getFrequency()})
							</#if>
						</option>
					</#list>
				</#if>
			</select>

			<i class="fa-light fa-chevron-down chevron-icon" aria-hidden="true"></i>
		</div>

		<@liferay_aui.script>
			(function() {
				var selectEl = document.getElementById('${namespace}facetSelect');

				if (!selectEl) {
					return;
				}

				selectEl.addEventListener('change', function() {
					var facetParam = selectEl.getAttribute('data-facet-param');
					var selectedValue = selectEl.value;
					var nextUrl = new URL(window.location.href);

					if (!facetParam) {
						return;
					}

					if (selectedValue) {
						nextUrl.searchParams.set(facetParam, selectedValue);
					}
					else {
						nextUrl.searchParams.delete(facetParam);
					}

					nextUrl.searchParams.delete('p');
					nextUrl.searchParams.delete('cur');
					nextUrl.searchParams.delete('delta');
					nextUrl.searchParams.delete('start');

					if (window.Liferay && Liferay.Util && Liferay.Util.navigate) {
						Liferay.Util.navigate(nextUrl.toString());
					}
					else {
						window.location.assign(nextUrl.toString());
					}
				});
			})();
		</@liferay_aui.script>
	</@>
</@>

<style>
.search-facet-dropdown .panel-body {
	padding-top: 8px;
}

.facet-dropdown {
	position: relative;
	margin-bottom: 8px;
}

.facet-dropdown__label {
	display: none;
}

.facet-dropdown__select {
	width: 100%;
	height: 48px;
	padding: 0 40px 0 0;
	border: 0;
	border-bottom: 2px solid #0b5fff;
	border-radius: 0;
	background: transparent;
	box-shadow: none;
	font-size: 16px;
	appearance: none;
}

.facet-dropdown__select:focus {
	outline: none;
	box-shadow: none;
}

.facet-dropdown .chevron-icon {
	position: absolute;
	right: 8px;
	top: 50%;
	transform: translateY(-50%);
	pointer-events: none;
}
</style>