package contracts.client.function;

import javax.annotation.Generated;

/**
 * @author NCLCST95H
 * @generated
 */
@FunctionalInterface
@Generated("")
public interface UnsafeSupplier<T, E extends Throwable> {

	public T get() throws E;

}