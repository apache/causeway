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

package org.apache.isis.applib.internal.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Common Collection creation and adapting idioms.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/> 
 * These may be changed or removed without notice!
 * </p>
 * 
 * @since 2.0.0
 */
public class _Collections {

	// -- COLLECTION UNMODIFIABLE ADAPTERS (FOR LIST)

	/**
	 * Adapts the {@code list} as unmodifiable collection. 
	 * Same as {@link Collections#unmodifiableCollection(List)}.
	 * 
	 * @param list
	 * @return null if {@code list} is null
	 */
	public static <T> Collection<T> asUnmodifiableCollection(@Nullable final List<T> list) {
		if(list==null) {
			return null;
		}
		return Collections.unmodifiableCollection(list);
	}
	
	/**
	 * Adapts the {@code list} as unmodifiable list. 
	 * Same as {@link Collections#unmodifiableList(List)}.
	 * 
	 * @param list
	 * @return
	 */
	public static <T> List<T> asUnmodifiableList(@Nullable final List<T> list) {
		if(list==null) {
			return null;
		}
		return Collections.unmodifiableList(list);
	}

	/**
	 * Preserves order, adapts the {@code list} as Set.<br/><br/> 
	 * 
	 * Any duplicate elements of the list will not be added to the set.
	 * An element e1 is a duplicate of e2 if {@code e1.equals(e2) == true}.
	 * 
	 * @param list
	 * @return null if {@code list} is null
	 */
	public static <T> Set<T> asUnmodifiableSet(@Nullable final List<T> list) {
		if(list==null) {
			return null;
		}
		return Collections.unmodifiableSet(
				(Set<T>)
				list.stream()
				.collect(Collectors.toCollection(LinkedHashSet::new)));
	}
	
	/**
	 * Preserves order, adapts the {@code list} as SortedSet.<br/><br/>
	 * 
	 * Any duplicate elements of the list will not be added to the set.
	 * An element e1 is a duplicate of e2 if {@code e1.equals(e2) == true}.
	 * 
	 * @param list
	 * @return null if {@code list} is null
	 */
	public static <T> SortedSet<T> asUnmodifiableSortedSet(@Nullable final List<T> list) {
		if(list==null) {
			return null;
		}
		return _Collections_SortedSetOfList.of(list);
	}

	// -- STREAM TO UMODIFIABLE COLLECTION COLLECTORS
	
	/**
	 * @return a collector that collects elements of a stream into an unmodifiable List 
	 */
	public static <T> Collector<T, List<T>, List<T>> toUnmodifiableList() {
		return new _Collections_Collector<>(ArrayList::new, Collections::unmodifiableList);
	}
	
	/**
	 * @return a collector that collects elements of a stream into an unmodifiable Set 
	 */
	public static <T> Collector<T, Set<T>, Set<T>> toUnmodifiableSet() {
		return new _Collections_Collector<>(HashSet::new, Collections::unmodifiableSet);
	}
	
	/**
	 * @return a collector that collects elements of a stream into an unmodifiable SortedSet 
	 */
	public static <T> Collector<T, SortedSet<T>, SortedSet<T>> toUnmodifiableSortedSet() {
		return new _Collections_Collector<>(TreeSet::new, Collections::unmodifiableSortedSet);
	}
	
	/**
	 * @return a collector that collects elements of a stream into an unmodifiable Collection 
	 */
	public static <T> Collector<T, Collection<T>, Collection<T>> toUnmodifiableCollection() {
		return new _Collections_Collector<>(ArrayList::new, Collections::unmodifiableCollection);
	}
	
	/**
	 * @return a collector that collects elements of a stream into an unmodifiable SortedSet 
	 */
	public static <T> Collector<T, SortedSet<T>, SortedSet<T>> toUnmodifiableSortedSet(
			@Nullable Comparator<T> comparator) {
		
		if(comparator==null) {
			return toUnmodifiableSortedSet();
		}
		return new _Collections_Collector<>(()->new TreeSet<>(comparator), Collections::unmodifiableSortedSet);
	}
	
	/**
	 * @return a collector that collects elements of a stream into an unmodifiable 
	 * List, Set, SortedSet or Collection. 
	 * @throws IllegalArgumentException if the {@link typeOfCollection} is not one of 
	 * List, Set, SortedSet or Collection.
	 */
	public static <T> Collector<T, ?, ? extends Collection<T>> toUnmodifiableOfType(Class<?> typeOfCollection) {
		
		Objects.requireNonNull(typeOfCollection);
		
		if(SortedSet.class.equals(typeOfCollection)) {
			return toUnmodifiableSortedSet();
		}
		
		if(Set.class.equals(typeOfCollection)) {
			return toUnmodifiableSet();
		}
		
		if(List.class.equals(typeOfCollection)) {
			return toUnmodifiableList();
		}
		
		if(Collection.class.equals(typeOfCollection)) {
			return toUnmodifiableCollection();
		}
		
		throw new IllegalArgumentException(
				String.format("Can not collect into %s. Only List, Set, SortedSet and Collection are supported.",
						typeOfCollection.getClass().getName()));
	}
	
	// --
	
}
