package contracts.client.dto.v1_0;

import contracts.client.function.UnsafeSupplier;
import contracts.client.serdes.v1_0.LabelSerDes;

import java.io.Serializable;

import java.util.Objects;

import javax.annotation.Generated;

/**
 * @author NCLCST95H
 * @generated
 */
@Generated("")
public class Label implements Cloneable, Serializable {

	public static Label toDTO(String json) {
		return LabelSerDes.toDTO(json);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setText(UnsafeSupplier<String, Exception> textUnsafeSupplier) {
		try {
			text = textUnsafeSupplier.get();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected String text;

	@Override
	public Label clone() throws CloneNotSupportedException {
		return (Label)super.clone();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof Label)) {
			return false;
		}

		Label label = (Label)object;

		return Objects.equals(toString(), label.toString());
	}

	@Override
	public int hashCode() {
		String string = toString();

		return string.hashCode();
	}

	public String toString() {
		return LabelSerDes.toJSON(this);
	}

}