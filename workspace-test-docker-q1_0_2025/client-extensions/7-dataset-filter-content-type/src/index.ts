import type {
	FDSFilter,
	FDSFilterHTMLElementBuilderArgs,
} from '@liferay/js-api/data-set';

type FilterData = string;

const OPTIONS = [
	{label: 'Notizia', value: 'notizia'},
	{label: 'Comunicato', value: 'comunicato'},
];

function descriptionBuilder(selectedData: FilterData): string {
	if (!selectedData) {
		return '';
	}

	const match = selectedData.match(/eq\s+'([^']+)'$/);

	return match?.[1] || selectedData;
}

function htmlElementBuilder({
	fieldName,
	filter,
	setFilter,
}: FDSFilterHTMLElementBuilderArgs<FilterData>): HTMLElement {
	const div = document.createElement('div');
	div.className = 'dropdown-item';
	div.style.display = 'flex';
	div.style.flexDirection = 'column';
	div.style.gap = '8px';
	div.style.padding = '8px';

	const select = document.createElement('select');
	select.className = 'form-control';

	const empty = document.createElement('option');
	empty.value = '';
	empty.textContent = 'Seleziona un valore';
	select.appendChild(empty);

	let currentValue = '';

	if (filter.selectedData) {
		const match = filter.selectedData.match(/eq\s+'([^']+)'$/);

		if (match?.[1]) {
			currentValue = match[1];
		}
	}

	for (const option of OPTIONS) {
		const el = document.createElement('option');
		el.value = option.value;
		el.textContent = option.label;

		if (currentValue === option.value) {
			el.selected = true;
		}

		select.appendChild(el);
	}

	const button = document.createElement('button');
	button.className = 'btn btn-block btn-secondary btn-sm mt-2';
	button.innerText = 'Applica';

	button.onclick = () => {
		const value = select.value;

		console.log('SELECT FILTER button click');
		console.log('fieldName:', fieldName);
		console.log('selected value:', value);

		if (!value) {
			setFilter({
				selectedData: '',
			});

			return;
		}

		if (!fieldName) {
			console.error('SELECT FILTER: fieldName assente');

			return;
		}

		const query = `${fieldName} eq '${value}'`;

		console.log('setFilter ->', {
			selectedData: query,
		});

		setFilter({
			selectedData: query,
		});
	};

	div.appendChild(select);
	div.appendChild(button);

	return div;
}

function oDataQueryBuilder(selectedData: FilterData): string {
	console.log('SELECT FILTER oDataQueryBuilder selectedData:', selectedData);

	return selectedData || '';
}

const fdsFilter: FDSFilter<FilterData> = {
	descriptionBuilder,
	htmlElementBuilder,
	oDataQueryBuilder,
};

export default fdsFilter;