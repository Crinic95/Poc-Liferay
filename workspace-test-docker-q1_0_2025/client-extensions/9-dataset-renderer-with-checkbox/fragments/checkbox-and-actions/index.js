const ACTION_LABELS = {
    markAsCompleted: 'Segna come Completato',
    resetToNew: 'Segna come Nuovo',
};

function getBulkCheckboxes() {
	return Array.from(
		document.querySelectorAll('input.bulk-checkbox[id^="BULK_"]')
	);
}

function getSelectedCheckboxes() {
	return getBulkCheckboxes().filter((cb) => cb.checked);
}

function getSelectedCount() {
	return getSelectedCheckboxes().length;
}

function parseActionsJson(checkbox) {
	try {
		return JSON.parse(checkbox.dataset.actionsJson || '{}');
	}
	catch (error) {
		console.error('Invalid actionsJson on checkbox', checkbox, error);
		return {};
	}
}

function getCommonActions(selectedCheckboxes) {
	if (!selectedCheckboxes.length) {
		return {};
	}

	const actionMaps = selectedCheckboxes.map(parseActionsJson);
	const firstMap = actionMaps[0] || {};

	const commonKeys = Object.keys(firstMap).filter((key) =>
		actionMaps.every((map) => map[key] && map[key].href)
	);

	const commonActions = {};

	for (const key of commonKeys) {
		commonActions[key] = firstMap[key];
	}

	return commonActions;
}

function populateBulkActionSelect() {
	const select = document.getElementById('bulkActionSelect');
	const selectedCheckboxes = getSelectedCheckboxes();
	const commonActions = getCommonActions(selectedCheckboxes);
	const actionEntries = Object.entries(commonActions);
	const previousValue = select.value;

	select.innerHTML = '';

	if (!selectedCheckboxes.length) {
		select.innerHTML =
			'<option value="">Seleziona almeno un record</option>';
		return;
	}

	if (!actionEntries.length) {
		select.innerHTML =
			'<option value="">Nessuna azione comune disponibile</option>';
		return;
	}

	const placeholder = document.createElement('option');
	placeholder.value = '';
	placeholder.textContent = 'Seleziona un’azione';
	select.appendChild(placeholder);

	for (const [actionKey, actionConfig] of actionEntries) {
		if (!(actionKey in ACTION_LABELS)) {
			continue;
		}

		const option = document.createElement('option');
		option.value = actionKey;
		option.textContent = ACTION_LABELS[actionKey] || actionConfig.label || actionKey;
		select.appendChild(option);
	}

	if (actionEntries.some(([actionKey]) => actionKey === previousValue)) {
		select.value = previousValue;
	}
}

function updateToggleAllState() {
	const checkboxes = getBulkCheckboxes();
	const toggleAllBulk = document.getElementById('toggleAllBulk');

	if (!checkboxes.length) {
		toggleAllBulk.checked = false;
		toggleAllBulk.indeterminate = false;
		return;
	}

	const checkedCount = checkboxes.filter((cb) => cb.checked).length;

	toggleAllBulk.checked = checkedCount === checkboxes.length;
	toggleAllBulk.indeterminate =
		checkedCount > 0 && checkedCount < checkboxes.length;
}

function updateSelectionCount() {
	const count = getSelectedCount();
	const label = document.getElementById('bulkSelectionCount');

	label.textContent = `${count} selezionat${count === 1 ? 'o' : 'i'}`;
}

function updateRunButtonState() {
	const button = document.getElementById('runBulkAction');
	const hasSelection = getSelectedCount() > 0;
	const hasAction = !!document.getElementById('bulkActionSelect').value;

	button.disabled = !(hasSelection && hasAction);
}

function updateBulkUiState() {
	updateToggleAllState();
	updateSelectionCount();
	populateBulkActionSelect();
	updateRunButtonState();
}

function showBulkFeedback(type, message) {
	const feedback = document.getElementById('bulkActionFeedback');

	feedback.hidden = false;
	feedback.className = `bulk-feedback ${type}`;
	feedback.textContent = message;
}

function clearBulkFeedback() {
	const feedback = document.getElementById('bulkActionFeedback');

	feedback.hidden = true;
	feedback.className = 'bulk-feedback';
	feedback.textContent = '';
}

function getSelectedAction() {
	return document.getElementById('bulkActionSelect').value;
}

function getActionHrefForCheckbox(checkbox, actionKey) {
	const actions = parseActionsJson(checkbox);

	return actions[actionKey]?.href || null;
}

document.getElementById('toggleAllBulk').addEventListener('change', function (event) {
	const checked = event.target.checked;

	getBulkCheckboxes().forEach((cb) => {
		cb.checked = checked;
	});

	updateBulkUiState();
});

document.getElementById('bulkActionSelect').addEventListener('change', function () {
	updateRunButtonState();
});

document.addEventListener('change', function (event) {
	if (
		event.target instanceof HTMLInputElement &&
		event.target.matches('input.bulk-checkbox[id^="BULK_"]')
	) {
		updateBulkUiState();
	}
});

document.getElementById('runBulkAction').addEventListener('click', async function () {
	clearBulkFeedback();

	const button = this;
	const selectedCheckboxes = getSelectedCheckboxes();
	const actionKey = getSelectedAction();

	if (!selectedCheckboxes.length) {
		showBulkFeedback('error', 'Nessun record selezionato.');
		return;
	}

	if (!actionKey) {
		showBulkFeedback('error', 'Seleziona un’azione.');
		return;
	}

	const hrefs = selectedCheckboxes
		.map((cb) => getActionHrefForCheckbox(cb, actionKey))
		.filter(Boolean);

	if (!hrefs.length) {
		showBulkFeedback('error', 'Nessuna action disponibile per i record selezionati.');
		return;
	}

	button.disabled = true;

	try {
		const authToken =
			(window.Liferay && Liferay.authToken) ||
			document.querySelector('meta[name="csrf-token"]')?.content;

		const results = await Promise.allSettled(
			hrefs.map((href) =>
				fetch(href, {
					method: 'PUT',
					credentials: 'same-origin',
					headers: {
						'Content-Type': 'application/json',
						'x-csrf-token': authToken || '',
					},
					body: '{}',
				})
			)
		);

		const failed = results.filter((result) => {
			if (result.status === 'rejected') {
				return true;
			}

			return !result.value.ok;
		});

		if (failed.length) {
			showBulkFeedback(
				'error',
				`Operazione completata con errori. Falliti: ${failed.length}.`
			);
			return;
		}

		const selectedLabel =
			document.querySelector('#bulkActionSelect option:checked')?.textContent ||
			'Azione eseguita';

		const successCount = hrefs.length;

		alert(`${selectedLabel}: ${successCount} record aggiornati con successo.`);

		window.location.reload();
	}
	catch (error) {
		console.error('Bulk action error:', error);
		showBulkFeedback('error', 'Errore durante la bulk action.');
	}
	finally {
		button.disabled = false;
		updateRunButtonState();
	}
});

updateBulkUiState();