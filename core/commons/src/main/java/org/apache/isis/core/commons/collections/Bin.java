package org.apache.isis.core.commons.collections;

import static org.apache.isis.commons.internal.base._With.requires;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.enterprise.inject.Instance;

import org.apache.isis.commons.internal.base._NullSafe;

import lombok.val;

/**
 * 
 * Immutable 'multi-set'. 
 * 
 * @param <T>
 * @since 2.0.0-M3
 */
public interface Bin<T> extends Iterable<T> {

	Cardinality getCardinality();
	int size();
	
	Stream<T> stream();
	
	Optional<T> getFirst();
	Optional<T> getSingleton();
	
	// -- FACTORIES
	
	@SuppressWarnings("unchecked") // this is how the JDK does it for eg. empty lists
	public static <T> Bin<T> empty() {
		return (Bin<T>) Bin_Empty.INSTANCE;
	}
	
	public static <T> Bin<T> ofNullable(@Nullable T element) {
		if(element==null) {
			return empty();
		}
		return Bin_Singleton.of(element);
	}
	
	public static <T> Bin<T> ofSingleton(T element) {
		requires(element, "element");
		return Bin_Singleton.of(element);
	}
	
	public static <T> Bin<T> ofCollection(@Nullable Collection<T> collection) {
		
		if(_NullSafe.size(collection)==0) {
			return empty();
		}
		
		val maxSize = collection.size();
		
		val nonNullElements = collection.stream()
				.filter(_NullSafe::isPresent)
				.collect(Collectors.toCollection(()->new ArrayList<>(maxSize)));
		
		nonNullElements.trimToSize(); // in case we have a 'sparse' collection as input to this method
		
		val size = nonNullElements.size();
		
		if(size==0) {
			return empty();
		}
		
		if(size==1) {
			return ofSingleton(((List<T>)nonNullElements).get(0));
		}
		
		return Bin_Multiple.of(nonNullElements);
	}
	
	public static <T> Bin<T> ofStream(@Nullable Stream<T> stream) {
        
        if(stream==null) {
            return empty();
        }
        
        val nonNullElements = stream
                .filter(_NullSafe::isPresent)
                .collect(Collectors.toCollection(()->new ArrayList<>()));        
        
        val size = nonNullElements.size();
        
        if(size==0) {
            return empty();
        }
        
        if(size==1) {
            return ofSingleton(((List<T>)nonNullElements).get(0));
        }
        
        return Bin_Multiple.of(nonNullElements);
    }
	
	public static <T> Bin<T> ofInstance(@Nullable Instance<T> instance) {
		if(instance==null || instance.isUnsatisfied()) {
			return empty();
		}
		if(instance.isResolvable()) { 
			return Bin_Singleton.of(instance.get());
		}
		val nonNullElements = instance.stream()
				.collect(Collectors.toCollection(()->new ArrayList<>()));
		
		return Bin_Multiple.of(nonNullElements);
		
	}
	
	
	// -- OPERATORS
	
	public default Bin<T> filter(@Nullable Predicate<? super T> predicate) {
		if(predicate==null || isEmpty()) {
			return this;
		}
		
		// optimization for the singleton case
		if(isCardinalityOne()) {
			val singleton = getSingleton().get();
			return predicate.test(singleton)
					? this
							: empty();
		}
		
		val filteredElements = 
		stream()
		.filter(predicate)
		.collect(Collectors.toCollection(ArrayList::new));
		
		// optimization for the case when the filter accepted all
		if(filteredElements.size()==size()) {
			return this;
		}
		
		return ofCollection(filteredElements);
	}
	
	public static <T> Bin<T> concat(@Nullable Bin<T> bin, @Nullable T variant) {
		if(bin==null || bin.isEmpty()) {
			return ofNullable(variant);
		}
		if(variant==null) {
			return bin;
		}
		// at this point: bin is not empty and variant is not null
		val newSize = bin.size() + 1;
    	val union = bin.stream().collect(Collectors.toCollection(()->new ArrayList<>(newSize)));
    	union.add(variant);
    	return Bin_Multiple.of(union);
    }
	
    default <R> Bin<R> map(Function<? super T, R> mapper) {
    	
        if(isEmpty()) {
            return empty();
        }
        
        requires(mapper, "mapper");
        
        val mappedElements = 
        		stream()
        		.map(mapper)
        		.filter(_NullSafe::isPresent)
        		.collect(Collectors.toCollection(ArrayList::new));
        
        return ofCollection(mappedElements);
    }
	
	// -- TRAVERSAL
	
    default void forEach(Consumer<? super T> action) {
        requires(action, "action");
        stream().forEach(action);
    }
	
	// -- SHORTCUTS FOR PREDICATES
	
	default boolean isEmpty() {
		return getCardinality().isZero();
	}
	
	default boolean isNotEmpty() {
		return !getCardinality().isZero();
	}
	
	default boolean isCardinalityOne() {
		return getCardinality().isOne();
	}
	
	default boolean isCardinalityMultiple() {
		return getCardinality().isMultiple();
	}
	
	
}
