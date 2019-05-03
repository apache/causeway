package org.apache.isis.core.commons.collections;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName="of")
final class Bin_Singleton<T> implements Bin<T> {
	
	private final T element;
	
	@Getter(lazy=true, onMethod=@__({@Override})) 
	private final Optional<T> singleton = Optional.of(element);

	@Override
	public Cardinality getCardinality() {
		return Cardinality.ONE;
	}

	@Override
	public Stream<T> stream() {
		return Stream.of(element);
	}

	@Override
	public Optional<T> getFirst() {
		return getSingleton();
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public Iterator<T> iterator() {
		return Collections.singletonList(element).iterator();
	}

}
