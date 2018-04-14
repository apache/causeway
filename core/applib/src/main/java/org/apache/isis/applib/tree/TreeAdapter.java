package org.apache.isis.applib.tree;

import java.util.Optional;
import java.util.stream.Stream;

public interface TreeAdapter<T> {

	public Optional<T> parentOf(T value);
	
	public int childCountOf(T value);
	
	public Stream<T> childrenOf(T value);
	
}
