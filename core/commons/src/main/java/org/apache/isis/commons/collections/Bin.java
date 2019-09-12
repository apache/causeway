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
package org.apache.isis.commons.collections;

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

import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import org.apache.isis.commons.internal.base._NullSafe;

import static org.apache.isis.commons.internal.base._With.requires;

import lombok.val;

/**
 * 
 * Immutable 'multi-set', that is particularly designed to conveniently deal 
 * with the 3 possible states of {@link Cardinality}. 
 * <p>
 * A {@code Bin} must not contain elements equal to {@code null}.
 * 
 * @param <T>
 * @since 2.0
 */
public interface Bin<T> extends Iterable<T> {

    /**
     * @return this Bin's cardinality
     */
    Cardinality getCardinality();
    
    /**
     * @return number of elements this Bin contains 
     */
    int size();

    /**
     * @return Stream of elements this Bin contains
     */
    Stream<T> stream();

    /**
     * @return this Bin's first element or an empty Optional if no such element
     */
    Optional<T> getFirst();
    
    /**
     * @return this Bin's single element or an empty Optional if this Bin has any cardinality other than ONE 
     */
    Optional<T> getSingleton();

    // -- FACTORIES

    /**
     * Returns an empty {@code Bin}.
     * @param <T>
     */
    @SuppressWarnings("unchecked") // this is how the JDK does it for eg. empty lists
    public static <T> Bin<T> empty() {
        return (Bin<T>) Bin_Empty.INSTANCE;
    }

    /**
     * Returns either a {@code Bin} with the given {@code element} or an empty {@code Bin} if the
     * {@code element} is {@code null}.
     * @param <T>
     * @param element
     * @return non-null
     */
    public static <T> Bin<T> ofNullable(@Nullable T element) {
        if(element==null) {
            return empty();
        }
        return Bin_Singleton.of(element);
    }

    /**
     * Returns either a {@code Bin} with the given {@code element} or throws if the
     * {@code element} is {@code null}.
     * @param <T>
     * @param element
     * @return non-null
     * @throws NullPointerException if {@code element} is {@code null}
     */
    public static <T> Bin<T> ofSingleton(T element) {
        requires(element, "element");
        return Bin_Singleton.of(element);
    }

    /**
     * Returns either a {@code Bin} with all the elements from given {@code collection} 
     * or an empty {@code Bin} if the {@code collection} is {@code null}. Any elements
     * equal to {@code null} are ignored and will not be contained in the resulting {@code Bin}.
     * @param <T>
     * @param collection
     * @return non-null
     */
    public static <T> Bin<T> ofCollection(@Nullable Collection<T> collection) {

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

        nonNullElements.sort(AnnotationAwareOrderComparator.INSTANCE);

        return Bin_Multiple.of(nonNullElements);
    }

    /**
     * Returns either a {@code Bin} with all the elements from given {@code stream} 
     * or an empty {@code Bin} if the {@code stream} is {@code null}. Any elements
     * equal to {@code null} are ignored and will not be contained in the resulting {@code Bin}.
     * @param <T>
     * @param stream
     * @return non-null
     */
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

        nonNullElements.sort(AnnotationAwareOrderComparator.INSTANCE);

        return Bin_Multiple.of(nonNullElements);
    }

    /**
     * Returns either a {@code Bin} with all the elements from given {@code instance} 
     * or an empty {@code Bin} if the {@code instance} is {@code null}. Any elements
     * equal to {@code null} are ignored and will not be contained in the resulting {@code Bin}.
     * @param <T>
     * @param instance
     * @return non-null
     */
    public static <T> Bin<T> ofInstance(@Nullable Instance<T> instance) {
        if(instance==null || instance.isUnsatisfied()) {
            return empty();
        }
        if(instance.isResolvable()) { 
            return Bin_Singleton.of(instance.get());
        }
        val nonNullElements = instance.stream()
                .collect(Collectors.toCollection(()->new ArrayList<>()));

        nonNullElements.sort(AnnotationAwareOrderComparator.INSTANCE);

        return Bin_Multiple.of(nonNullElements);

    }


    // -- OPERATORS

    /**
     * Returns a {@code Bin} with all the elements from this {@code Bin},
     * that are accepted by the given {@code predicate}. If {@code predicate}
     * is {@code null} <em>all</em> elements are accepted.
     * @param predicate - if absent accepts all
     * @return non-null
     */
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

    /**
     * Returns a {@code Bin} with all the elements from this {@code Bin}
     * 'transformed' by the given {@code mapper} function. Any resulting elements
     * equal to {@code null} are ignored and will not be contained in the resulting {@code Bin}.
     * 
     * @param <R>
     * @param mapper - if absent throws if this {@code Bin} is non-empty 
     * @return non-null
     */
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
    
    // -- CONCATENATION

    /**
     * Returns a {@code Bin} with all the elements from given {@code bin} joined by 
     * the given {@code element}. If any of given {@code bin} or {@code element} are {@code null}
     * these do not contribute any elements and are ignored.
     * @param <T>
     * @param bin - nullable
     * @param element - nullable
     * @return non-null
     */
    public static <T> Bin<T> concat(@Nullable Bin<T> bin, @Nullable T element) {
        if(bin==null || bin.isEmpty()) {
            return ofNullable(element);
        }
        if(element==null) {
            return bin;
        }
        // at this point: bin is not empty and variant is not null
        val newSize = bin.size() + 1;
        val union = bin.stream().collect(Collectors.toCollection(()->new ArrayList<>(newSize)));
        union.add(element);
        return Bin_Multiple.of(union);
    }
    
    // -- TRAVERSAL

    @Override
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
