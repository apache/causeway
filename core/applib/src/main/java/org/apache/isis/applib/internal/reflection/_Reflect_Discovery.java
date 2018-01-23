package org.apache.isis.applib.internal.reflection;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import org.apache.isis.applib.internal.base._NullSafe;
import org.apache.isis.applib.internal.reflection._Reflect.Discovery;
import org.reflections.Reflections;

/**
 * 
 * package private mixin for utility class {@link _Reflect}
 *
 */
class _Reflect_Discovery implements _Reflect.Discovery {
	
	private final Reflections reflections;
	
	// -- CONSTRUCTORS
	
	public static Discovery of(List<String> packagePrefixes) {
		return new _Reflect_Discovery(packagePrefixes);
	}

	public static Discovery of(String packageNamePrefix) {
		return new _Reflect_Discovery(packageNamePrefix);
	}
	
	public static Discovery of(final Object... params) {
		return new _Reflect_Discovery(params);
	}
	
	// -- HIDDEN CONSTRUCTOR
	
	private _Reflect_Discovery(final Object... params) {
		this.reflections = new Reflections(params);
	}
	
	// -- IMPLEMENTATION

	@Override @NotNull
	public Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation) {
		
		Objects.requireNonNull(annotation);

		// ensure unique entries
		return streamTypesAnnotatedWith(annotation).collect(Collectors.toCollection(HashSet::new));
	}

	@Override @NotNull
	public <T> Set<Class<? extends T>> getSubTypesOf(Class<T> type) {
		
		Objects.requireNonNull(type);
		
		// ensure unique entries
		return streamSubTypesOf(type).collect(Collectors.toCollection(HashSet::new));
	}
	
	// -- HELPER
	
	
	private Stream<Class<?>> streamTypesAnnotatedWith(Class<? extends Annotation> annotation) {
		
		// ensure non-null elements
		return _NullSafe.stream(reflections.getTypesAnnotatedWith(annotation))
				.filter(_NullSafe::isPresent);
	}

	private <T> Stream<Class<? extends T>> streamSubTypesOf(final Class<T> type) {
		
		// ensure non-null elements
		return _NullSafe.stream(reflections.getSubTypesOf(type))
				.filter(_NullSafe::isPresent);
	}


	
}
