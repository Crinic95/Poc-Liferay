type CellRendererProps = {
	itemData: Record<string, unknown>;
	value: unknown;
};

function createBadge(text: string) {
	const span = document.createElement('span');

	span.textContent = text;
	span.style.display = 'inline-block';
	span.style.padding = '2px 8px';
	span.style.borderRadius = '999px';
	span.style.fontSize = '12px';
	span.style.fontWeight = '600';
	span.style.background = '#eef2ff';
	span.style.color = '#3730a3';

	return span;
}

function createTitleCell(itemData: Record<string, unknown>) {
	const wrapper = document.createElement('div');
	wrapper.style.display = 'flex';
	wrapper.style.flexDirection = 'column';
	wrapper.style.gap = '4px';

	const title = document.createElement('div');
	title.textContent = String(itemData.detailTitle || itemData.cardTitle || itemData.title || '');
	title.style.fontWeight = '600';
	title.style.lineHeight = '1.3';

	const meta = document.createElement('div');
	meta.style.display = 'flex';
	meta.style.gap = '8px';
	meta.style.alignItems = 'center';

	if (itemData.contentType) {
		meta.appendChild(createBadge(String(itemData.contentType)));
	}

	if (itemData.cardDate) {
		const date = document.createElement('span');
		date.textContent = String(itemData.cardDate);
		date.style.fontSize = '12px';
		date.style.color = '#6b7280';
		meta.appendChild(date);
	}

	wrapper.appendChild(title);
	wrapper.appendChild(meta);

	return wrapper;
}

export default function renderCell({itemData, value}: CellRendererProps) {
	if ('detailTitle' in itemData || 'cardTitle' in itemData) {
		return createTitleCell(itemData);
	}

	const span = document.createElement('span');
	span.textContent = value == null ? '' : String(value);

	return span;
}