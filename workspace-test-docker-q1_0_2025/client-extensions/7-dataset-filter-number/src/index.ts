import type {
	FDSFilter,
	FDSFilterHTMLElementBuilderArgs,
} from '@liferay/js-api/data-set';

type FilterData = string;

const OPERATORS = [
	{label: '=', value: 'eq'},
	{label: '>', value: 'gt'},
	{label: '>=', value: 'ge'},
	{label: '<', value: 'lt'},
	{label: '<=', value: 'le'},
];

function descriptionBuilder(selectedData: FilterData): string {
	if (!selectedData) {
		return '';
	}

	const match = selectedData.match(/(eq|gt|ge|lt|le)\s+([0-9]+(?:\.[0-9]+)?)$/);

	if (!match) {
		return selectedData;
	}

	const operator = match[1];
	const value = match[2];

	const labelByOperator: Record<string, string> = {
		eq: '=',
		gt: '>',
		ge: '>=',
		lt: '<',
		le: '<=',
	};

	return `${labelByOperator[operator] || operator} ${value}`;
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

	const row = document.createElement('div');
	row.style.display = 'flex';
	row.style.gap = '8px';

	const operatorSelect = document.createElement('select');
	operatorSelect.className = 'form-control';
	operatorSelect.style.width = '90px';

	const input = document.createElement('input');
	input.type = 'number';
	input.className = 'form-control';
	input.placeholder = 'Valore';
	input.style.flex = '1';

	let currentOperator = 'eq';
	let currentValue = '';

	if (filter.selectedData) {
		const match = filter.selectedData.match(
			/(eq|gt|ge|lt|le)\s+([0-9]+(?:\.[0-9]+)?)$/
		);

		if (match) {
			currentOperator = match[1];
			currentValue = match[2];
		}
	}

	for (const operator of OPERATORS) {
		const option = document.createElement('option');
		option.value = operator.value;
		option.textContent = operator.label;

		if (operator.value === currentOperator) {
			option.selected = true;
		}

		operatorSelect.appendChild(option);
	}

	input.value = currentValue;

	const button = document.createElement('button');
	button.className = 'btn btn-block btn-secondary btn-sm mt-2';
	button.innerText = 'Applica';

	button.onclick = () => {
		const operator = operatorSelect.value;
		const value = input.value.trim();

		console.log('NUMBER FILTER button click');
		console.log('fieldName:', fieldName);
		console.log('operator:', operator);
		console.log('value:', value);

		if (!value) {
			setFilter({
				selectedData: '',
			});

			return;
		}

		if (!fieldName) {
			console.error('NUMBER FILTER: fieldName assente');

			return;
		}

		const query = `${fieldName} ${operator} ${value}`;

		console.log('setFilter ->', {
			selectedData: query,
		});

		setFilter({
			selectedData: query,
		});
	};

	row.appendChild(operatorSelect);
	row.appendChild(input);

	div.appendChild(row);
	div.appendChild(button);

	return div;
}

function oDataQueryBuilder(selectedData: FilterData): string {
	console.log('NUMBER FILTER oDataQueryBuilder selectedData:', selectedData);

	return selectedData || '';
}

const fdsFilter: FDSFilter<FilterData> = {
	descriptionBuilder,
	htmlElementBuilder,
	oDataQueryBuilder,
};

export default fdsFilter;