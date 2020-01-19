/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.core.commons.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.enterprise.inject.Instance;

import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;

import static org.apache.isis.core.commons.internal.base._With.requires;

import lombok.val;

/**
 * 
 * Immutable {@link Iterable}, that can specifically represent 3 possible variants of 
 * {@link Cardinality}. 
 * <p>
 * Java's {@link Optional}, can be seen as a holder of element(s), that is restricted to 
 * cardinality ZERO or ONE. {@link Can} is the logical extension to that, allowing also a 
 * cardinality of MULTIPLE.
 * <p> 
 * Same idiomatic convention applies: References to {@link Can} 
 * should never be initialized to {@code null}.
 * <p>
 * A {@link Can} must not contain elements equal to {@code null}.
 * 
 * @param <T>
 * @since 2.0
 */
public interface Can<T> extends Iterable<T> {

    /**
     * @return this Can's cardinality
     */
    Cardinality getCardinality();

    /**
     * @return number of elements this Can contains 
     */
    int size();
    
    /**
     * @param elementIndex
     * @return optionally this Can's element with index {@code elementIndex} 
     */
    Optional<T> get(int elementIndex);
    
    /**
     * Shortcut to {@code get(elementIndex).orElseThrow(...)}
     * @param elementIndex
     * @return this Can's element with index {@code elementIndex}
     * @throws NoSuchElementException
     * @see {@link #get(int)} 
     */
    default T getElseFail(final int elementIndex) {
        return get(elementIndex)
                .orElseThrow(()->new NoSuchElementException(
                        "no element with elementIndex = " + elementIndex));
    }

    /**
     * @return Stream of elements this Can contains
     */
    Stream<T> stream();

    /**
     * @return this Can's first element or an empty Optional if no such element
     */
    Optional<T> getFirst();

    /**
     * Shortcut for {@code getFirst().orElseThrow(_Exceptions::unexpectedCodeReach)}
     */
    default T getFirstOrFail() {
        return getFirst().orElseThrow(_Exceptions::unexpectedCodeReach);
    }

    /**
     * @return this Can's single element or an empty Optional if this Can has any cardinality other than ONE 
     */
    Optional<T> getSingleton();
    
    /**
     * Shortcut for {@code getSingleton().orElseThrow(_Exceptions::unexpectedCodeReach)}
     */
    default T getSingletonOrFail() {
        return getSingleton().orElseThrow(_Exceptions::unexpectedCodeReach);
    }

    // -- FACTORIES

    /**
     * Returns an empty {@code Can}.
     * @param <T>
     */
    @SuppressWarnings("unchecked") // this is how the JDK does it for eg. empty lists
    public static <T> Can<T> empty() {
        return (Can<T>) Can_Empty.INSTANCE;
    }

    /**
     * Returns either a {@code Can} with the given {@code element} or an empty {@code Can} if the
     * {@code element} is {@code null}.
     * @param <T>
     * @param element
     * @return non-null
     */
    public static <T> Can<T> ofNullable(@Nullable T element) {
        if(element==null) {
            return empty();
        }
        return Can_Singleton.of(element);
    }

    /**
     * Returns either a {@code Can} with the given {@code element} or throws if the
     * {@code element} is {@code null}.
     * @param <T>
     * @param element
     * @return non-null
     * @throws NullPointerException if {@code element} is {@code null}
     */
    public static <T> Can<T> ofSingleton(T element) {
        requires(element, "element");
        return Can_Singleton.of(element);
    }

    public static <T> Can<T> ofArray(@Nullable T[] array) {

        if(_NullSafe.size(array)==0) {
            return empty();
        }

        // this is just an optimization, to pre-allocate a reasonable list size,
        // specifically targeted at small list sizes
        val maxSize = Math.min(array.length, 1024);
        
        val nonNullElements = Stream.of(array)
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

        return Can_Multiple.of(nonNullElements);

    }

    /**
     * Returns either a {@code Can} with all the elements from given {@code collection} 
     * or an empty {@code Can} if the {@code collection} is {@code null}. Any elements
     * equal to {@code null} are ignored and will not be contained in the resulting {@code Can}.
     * @param <T>
     * @param collection
     * @return non-null
     */
    public static <T> Can<T> ofCollection(@Nullable Collection<T> collection) {

        if(_NullSafe.size(collection)==0) {
            return empty();
        }

        // this is just an optimization, to pre-allocate a reasonable list size,
        // specifically targeted at small list sizes
        val maxSize = Math.min(collection.size(), 1024); 

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

        return Can_Multiple.of(nonNullElements);
    }
    
    public static <T> Can<T> ofIterable(@Nullable Iterable<T> iterable) {
        
        if(iterable==null) {
            return empty();
        }
        
        val elements = new ArrayList<T>();
        iterable.forEach(elements::add);
        
        return ofCollection(elements);
    }

    /**
     * Returns either a {@code Can} with all the elements from given {@code stream} 
     * or an empty {@code Can} if the {@code stream} is {@code null}. Any elements
     * equal to {@code null} are ignored and will not be contained in the resulting {@code Can}.
     * @param <T>
     * @param stream
     * @return non-null
     */
    public static <T> Can<T> ofStream(@Nullable Stream<T> stream) {

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

        return Can_Multiple.of(nonNullElements);
    }

    /**
     * Returns either a {@code Can} with all the elements from given {@code instance} 
     * or an empty {@code Can} if the {@code instance} is {@code null}. Any elements
     * equal to {@code null} are ignored and will not be contained in the resulting {@code Can}.
     * @param <T>
     * @param instance
     * @return non-null
     */
    public static <T> Can<T> ofInstance(@Nullable Instance<T> instance) {
        if(instance==null || instance.isUnsatisfied()) {
            return empty();
        }
        if(instance.isResolvable()) { 
            return Can_Singleton.of(instance.get());
        }
        val nonNullElements = instance.stream()
                .collect(Collectors.toCollection(()->new ArrayList<>()));

        return Can_Multiple.of(nonNullElements);

    }


    // -- OPERATORS

    /**
     * Returns a {@code Can} with all the elements from this {@code Can},
     * that are accepted by the given {@code predicate}. If {@code predicate}
     * is {@code null} <em>all</em> elements are accepted.
     * @param predicate - if absent accepts all
     * @return non-null
     */
    public default Can<T> filter(@Nullable Predicate<? super T> predicate) {
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

    /**
     * Returns a {@code Can} with all the elements from this {@code Can}
     * 'transformed' by the given {@code mapper} function. Any resulting elements
     * equal to {@code null} are ignored and will not be contained in the resulting {@code Can}.
     * 
     * @param <R>
     * @param mapper - if absent throws if this {@code Can} is non-empty 
     * @return non-null
     */
    default <R> Can<R> map(Function<? super T, R> mapper) {

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

    // -- CONCATENATION

    /**
     * Returns a {@code Can} with all the elements from given {@code can} joined by 
     * the given {@code element}. If any of given {@code can} or {@code element} are {@code null}
     * these do not contribute any elements and are ignored.
     * @param <T>
     * @param can - nullable
     * @param element - nullable
     * @return non-null
     */
    public static <T> Can<T> concat(@Nullable Can<T> can, @Nullable T element) {
        if(can==null || can.isEmpty()) {
            return ofNullable(element);
        }
        if(element==null) {
            return can;
        }
        // at this point: can is not empty and variant is not null
        val newSize = can.size() + 1;
        val union = can.stream().collect(Collectors.toCollection(()->new ArrayList<>(newSize)));
        union.add(element);
        return Can_Multiple.of(union);
    }

    // -- TRAVERSAL

    @Override
    default void forEach(Consumer<? super T> action) {
        requires(action, "action");
        stream().forEach(action);
    }
    
    // -- MANIPULATION
    
    Can<T> add(T element);
    Can<T> addAll(Can<T> other);
    
    /**
     * Inserts the specified element at the specified position in this list
     * (optional operation).  Shifts the element currently at that position
     * (if any) and any subsequent elements to the right (adds one to their
     * indices).
     *
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     * @return new instance
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this list
     * @throws NullPointerException if the specified element is null
     * @throws IllegalArgumentException if some property of the specified
     *         element prevents it from being added to this list
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt; size()</tt>)
     */
    Can<T> add(int index, T element);

    /**
     * Removes the element at the specified position in this list (optional
     * operation).  Shifts any subsequent elements to the left (subtracts one
     * from their indices).  Returns the element that was removed from the
     * list.
     *
     * @param index the index of the element to be removed
     * @return new instance
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    Can<T> remove(int index);
    
    // -- SEARCH
    
    /**
     * Returns the index of the first occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the lowest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     *
     * @param element element to search for
     * @return the index of the first occurrence of the specified element in
     *         this list, or -1 if this list does not contain the element
     * @throws ClassCastException if the type of the specified element
     *         is incompatible with this list
     *         (<a href="Collection.html#optional-restrictions">optional</a>)
     * @throws NullPointerException if the specified element is null and this
     *         list does not permit null elements
     *         (<a href="Collection.html#optional-restrictions">optional</a>)
     */
    int indexOf(T element);
    
    // -- EQUALITY
    
    /**
     * @param other
     * @return whether this is element-wise equal to {@code other}
     */
    default boolean isEqualTo(Can<?> other) {
        if(other==null) {
            return false;
        }
        if(this.size()!=other.size()) {
            return false;
        }
        
        val otherIterator = other.iterator();
        
        for(T element: this) {
            val otherElement = otherIterator.next();
            if(!element.equals(otherElement)) {
                return false;
            }
        }
        
        return true;
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

    // -- COLLECTORS

    public static <T> 
    Collector<T, ?, Can<T>> toCan() {
        
        return Collectors.collectingAndThen(
                Collectors.toList(), 
                Can::ofCollection);
    }

    // -- CONVERSIONS
    
    /**
     * @return a serializable and immutable List, containing the elements of this Can
     */
    List<T> toList();

//XXX to implement when needed    
//    Set<T> toSet();
//    Set<T> toSortedSet();
//    Set<T> toSortedSet(Comparator<T> comparator);

    /**
     * @param <C>
     * @param collectionFactory
     * @return a collection, containing the elements of this Can
     */
    <C extends Collection<T>> C toCollection(Supplier<C> collectionFactory);

    /**
     * @param a the array into which the elements of this Can are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose.
     * @return a non-null array, containing the elements of this Can
     */
    default T[] toArray(T[] a) {
        return toList().toArray(a);
    }
    
    /**
     * @param elementType the {@code Class} object representing the component
     *          type of the new array
     * @return a non-null array, containing the elements of this Can
     */
    T[] toArray(Class<T> elementType);
    
}
