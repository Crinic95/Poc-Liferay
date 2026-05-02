type CellRendererProps = {
	itemData: Record<string, unknown>;
	value: unknown;
};

type RawAction = {
	href?: string;
	method?: string;
};

type RawActions = Record<string, RawAction>;

export default function renderCell({itemData}: CellRendererProps) {
	const checkbox = document.createElement('input');

	checkbox.type = 'checkbox';
	checkbox.className = 'bulk-checkbox form-check-input';

	const recordId = itemData.id;
	const actions = itemData.actions as RawActions | undefined;

	console.log('BULK RENDERER itemData:', itemData);
	console.log('BULK RENDERER itemData.actions:', actions);

	if (recordId == null) {
		checkbox.disabled = true;
		return checkbox;
	}

	checkbox.id = `BULK_${String(recordId)}`;
	checkbox.dataset.recordId = String(recordId);

	if (!actions || typeof actions !== 'object') {
		checkbox.disabled = true;
		return checkbox;
	}

	const availableActions: Record<string, {label: string; href: string}> = {};

	for (const [actionKey, actionConfig] of Object.entries(actions)) {
		if (!actionConfig?.href) {
			continue;
		}

		availableActions[actionKey] = {
			label: actionKey,
			href: actionConfig.href,
		};
	}

	if (!Object.keys(availableActions).length) {
		checkbox.disabled = true;
		return checkbox;
	}

	checkbox.dataset.actionsJson = JSON.stringify(availableActions);

	return checkbox;
}