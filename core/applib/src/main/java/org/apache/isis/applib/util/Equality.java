package org.apache.isis.applib.util;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.collections._Lists;

/**
 * Fluent Object Equality Composition.
 * 
 * @param <T>
 * @since 2.0.0
 */
public class Equality<T> {

	public static <T> Equality<T> checkEquals(Function<? super T, ?> getter) {
		Objects.requireNonNull(getter);
		return new Equality<>(getter);
	}
	
	private final List<Function<? super T, ?>> getters = _Lists.newArrayList();

	private Equality(Function<? super T, ?> getter) {
		getters.add(getter);
	}
	
	public Equality<T> thenCheckEquals(Function<? super T, ?> getter){
		Objects.requireNonNull(getter);
		getters.add(getter);
		return this;
	}

	public boolean equals(T target, Object other){
		if(target==null && other==null) {
            return true;
        }
        if(target==null || other==null) {
            return false;
        }
        if(target.getClass() != other.getClass()) {
            return false;
        }
		final T o = _Casts.uncheckedCast(other);
		
		for(Function<? super T, ?> getter : getters) {
			if(!Objects.equals(getter.apply(target), getter.apply(o)))
				return false;
		}
		
		return true;
	}

	
	
}
