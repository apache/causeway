package org.apache.isis.commons.internal.base;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.Nullable;

/**
 * <h1>- internal use only -</h1>
 * <p>
 *  Provides a generic (left-fold) reduction class. 
 *  </p>
 *  <p>
 *  Most intuitive example of a reduction is finding the 
 *  minimum value from a list of values. See {@link ReductionTest} for examples.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/> 
 * These may be changed or removed without notice!
 * </p>
 * 
 * @since 2.0.0
 *
 */
public final class _Reduction<T> implements Consumer<T> {

	private final BinaryOperator<T> accumulator;
	private T result;
	private boolean initialized = false;
	
	
	/**
	 * Inspired by {@link Stream#reduce(Object, BinaryOperator)}
	 * @param identity
	 * @param accumulator
	 * @return
	 */
	public static <T> _Reduction<T> of(@Nullable T identity, BinaryOperator<T> accumulator){
		return new _Reduction<T>(identity, accumulator, true);
	}
	
	/**
	 * Inspired by {@link Stream#reduce(BinaryOperator)}
	 * @param accumulator
	 * @return
	 */
	public static <T> _Reduction<T> of(BinaryOperator<T> accumulator){
		return new _Reduction<T>(null, accumulator, false);
	}

	private _Reduction(@Nullable T identity, BinaryOperator<T> accumulator, boolean initialized) {
		Objects.requireNonNull(accumulator);
		this.initialized = initialized;
		this.result = identity;
		this.accumulator = accumulator;
	}

	@Override
	public void accept(@Nullable T next) {
		if(!initialized) {
			result = next;
			initialized = true;
			return;
		}
		result = accumulator.apply(result, next);
	}
	
	/**
	 * Returns the reduction result if ever initialized, Optional.empty() otherwise.
	 * @return non-null
	 */
	public Optional<T> getResult() {
		if(!initialized) {
			return Optional.empty();
		}
		return Optional.ofNullable(result);
	}
	
	
}
