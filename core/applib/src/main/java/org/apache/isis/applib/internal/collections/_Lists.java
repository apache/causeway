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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.isis.applib.internal.base._NullSafe;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Common List creation idioms.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/> 
 * These may be changed or removed without notice!
 * </p>
 * 
 * @since 2.0.0
 */
public final class _Lists {

	private _Lists(){}
	
	// -- LIST ACCESS
	
	public static <T> T lastElementIfAny(@Nullable List<T> list) {
		if(_NullSafe.isEmpty(list)) {
			return null;
		}
		return list.get(list.size()-1);
	}
	
	// -- UNMODIFIABLE LIST
	
	/**
	 * Copies all elements into a new unmodifiable List.
	 * @param elements
	 * @return non null
	 */
	@SafeVarargs
	public static <T> List<T> unmodifiable(T ... elements) {
		Objects.requireNonNull(elements); // don't accept null elements
		if(elements.length==0) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(Arrays.asList(elements));
	}
	
	/**
	 * Copies all elements from iterable into a new unmodifiable List.
	 * @param iterable
	 * @return non null
	 */
	public static <T> List<T> unmodifiable(Iterable<T> iterable) {
		if(iterable==null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(
				_NullSafe.stream(iterable)
				.collect(Collectors.toList()));
	}
	
	// -- ARRAY LIST
	
	public static <T> ArrayList<T> newArrayList() {
		return new ArrayList<T>();
	}
	
	public static <T> ArrayList<T> newArrayList(@Nullable Collection<T> collection) {
		if(collection==null) {
			return newArrayList();
		}
		return new ArrayList<T>(collection);
	}
	
	public static <T> ArrayList<T> newArrayList(@Nullable Iterable<T> iterable) {
		return _Collections.collectFromIterable(iterable, _Lists::newArrayList, 
				()->Collectors.<T, ArrayList<T>>toCollection(ArrayList::new) );
	}
	
	// -- LINKED LIST
	
	public static <T> LinkedList<T> newLinkedList() {
		return new LinkedList<T>();
	}
	
	public static <T> LinkedList<T> newLinkedList(@Nullable Collection<T> collection) {
		if(collection==null) {
			return newLinkedList();
		}
		return new LinkedList<T>(collection);
	}
	
	public static <T> LinkedList<T> newLinkedList(@Nullable Iterable<T> iterable) {
		return _Collections.collectFromIterable(iterable, _Lists::newLinkedList, 
				()->Collectors.<T, LinkedList<T>>toCollection(LinkedList::new) );
	}
	
	// -- TRANSFORMATION
	
	public static <T, R> List<R> transform(@Nullable List<T> input, Function<T, R> mapper) {
		if(input==null) {
			return Collections.emptyList();
		}
		Objects.requireNonNull(mapper);
		return input.stream()
				.map(mapper)
				.collect(Collectors.toList());
	}
	
	
}
