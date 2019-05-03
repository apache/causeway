package org.apache.isis.core.commons.collections;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

import lombok.Value;

@Value(staticConstructor="of")
final class Bin_Empty<T> implements Bin<T> {
	
	static final Bin_Empty<?> INSTANCE = new Bin_Empty<>(); 
	
	@Override
	public Cardinality getCardinality() {
		return Cardinality.ZERO;
	}

	@Override
	public Stream<T> stream() {
		return Stream.empty();
	}

	@Override
	public Optional<T> getSingleton() {
		return Optional.empty();
	}

	@Override
	public Optional<T> getFirst() {
		return Optional.empty();
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public Iterator<T> iterator() {
		return Collections.<T>emptyList().iterator();
	}

}
