package contracts.client.serdes.v1_0;

import contracts.client.dto.v1_0.Label;
import contracts.client.json.BaseJSONParser;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.Generated;

/**
 * @author NCLCST95H
 * @generated
 */
@Generated("")
public class LabelSerDes {

	public static Label toDTO(String json) {
		LabelJSONParser labelJSONParser = new LabelJSONParser();

		return labelJSONParser.parseToDTO(json);
	}

	public static Label[] toDTOs(String json) {
		LabelJSONParser labelJSONParser = new LabelJSONParser();

		return labelJSONParser.parseToDTOs(json);
	}

	public static String toJSON(Label label) {
		if (label == null) {
			return "null";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("{");

		if (label.getText() != null) {
			if (sb.length() > 1) {
				sb.append(", ");
			}

			sb.append("\"text\": ");

			sb.append("\"");

			sb.append(_escape(label.getText()));

			sb.append("\"");
		}

		sb.append("}");

		return sb.toString();
	}

	public static Map<String, Object> toMap(String json) {
		LabelJSONParser labelJSONParser = new LabelJSONParser();

		return labelJSONParser.parseToMap(json);
	}

	public static Map<String, String> toMap(Label label) {
		if (label == null) {
			return null;
		}

		Map<String, String> map = new TreeMap<>();

		if (label.getText() == null) {
			map.put("text", null);
		}
		else {
			map.put("text", String.valueOf(label.getText()));
		}

		return map;
	}

	public static class LabelJSONParser extends BaseJSONParser<Label> {

		@Override
		protected Label createDTO() {
			return new Label();
		}

		@Override
		protected Label[] createDTOArray(int size) {
			return new Label[size];
		}

		@Override
		protected boolean parseMaps(String jsonParserFieldName) {
			if (Objects.equals(jsonParserFieldName, "text")) {
				return false;
			}

			return false;
		}

		@Override
		protected void setField(
			Label label, String jsonParserFieldName,
			Object jsonParserFieldValue) {

			if (Objects.equals(jsonParserFieldName, "text")) {
				if (jsonParserFieldValue != null) {
					label.setText((String)jsonParserFieldValue);
				}
			}
		}

	}

	private static String _escape(Object object) {
		String string = String.valueOf(object);

		for (String[] strings : BaseJSONParser.JSON_ESCAPE_STRINGS) {
			string = string.replace(strings[0], strings[1]);
		}

		return string;
	}

	private static String _toJSON(Map<String, ?> map) {
		StringBuilder sb = new StringBuilder("{");

		@SuppressWarnings("unchecked")
		Set set = map.entrySet();

		@SuppressWarnings("unchecked")
		Iterator<Map.Entry<String, ?>> iterator = set.iterator();

		while (iterator.hasNext()) {
			Map.Entry<String, ?> entry = iterator.next();

			sb.append("\"");
			sb.append(entry.getKey());
			sb.append("\": ");

			Object value = entry.getValue();

			sb.append(_toJSON(value));

			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}

		sb.append("}");

		return sb.toString();
	}

	private static String _toJSON(Object value) {
		if (value instanceof Map) {
			return _toJSON((Map)value);
		}

		Class<?> clazz = value.getClass();

		if (clazz.isArray()) {
			StringBuilder sb = new StringBuilder("[");

			Object[] values = (Object[])value;

			for (int i = 0; i < values.length; i++) {
				sb.append(_toJSON(values[i]));

				if ((i + 1) < values.length) {
					sb.append(", ");
				}
			}

			sb.append("]");

			return sb.toString();
		}

		if (value instanceof String) {
			return "\"" + _escape(value) + "\"";
		}

		return String.valueOf(value);
	}

}