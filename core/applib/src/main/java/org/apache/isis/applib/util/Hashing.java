package org.apache.isis.applib.util;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.apache.isis.applib.internal.collections._Lists;

/**
 * Fluent Object Hash Code Composition.
 *  
 * @param <T>
 * @since 2.0.0
 *
 */
public class Hashing<T> {

	public static <T> Hashing<T> hashing(Function<? super T, ?> getter){
		return new Hashing<>(getter);
	}
	
	private final List<Function<? super T, ?>> getters = _Lists.newArrayList();

	private Hashing(Function<? super T, ?> getter) {
		getters.add(getter);
	}
	
	public Hashing<T> thenHashing(Function<? super T, ?> getter){
		Objects.requireNonNull(getter);
		getters.add(getter);
		return this;
	}

	public int hashCode(T object){
		if(object==null) {
			return 0;
		}
		int result = 1;
		for(Function<? super T, ?> getter : getters) {
			final Object element = getter.apply(object); 
			result = 31 * result + (element == null ? 0 : element.hashCode());
		}
		return result;
	}


	
}
