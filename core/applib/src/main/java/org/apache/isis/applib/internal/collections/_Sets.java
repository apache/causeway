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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.stream.Stream;

import javax.annotation.Nullable;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Common Set creation idioms.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/> 
 * These may be changed or removed without notice!
 * </p>
 * 
 * @since 2.0.0
 */
public final class _Sets {

	private _Sets(){}
	
	// -- UNMODIFIABLE SET
	
	@SafeVarargs
	public static <T> Set<T> unmodifiable(T ... elements) {
		Objects.requireNonNull(elements); // don't accept null elements
		if(elements.length==0) {
			return Collections.emptySet();
		}
		
		final Set<T> setPreservingOrder = newLinkedHashSet();
		
		Stream.of(elements)
		.forEach(setPreservingOrder::add);
		
		return Collections.unmodifiableSet(setPreservingOrder);
	}
	
	// -- HASH SET
	
	public static <T> HashSet<T> newHashSet() {
		return new HashSet<T>();
	}
	
	public static <T> HashSet<T> newHashSet(@Nullable Collection<T> collection) {
		if(collection==null) {
			return newHashSet();
		}
		return new HashSet<T>(collection);
	}
	
	// -- LINKED HASH SET
	
	public static <T> LinkedHashSet<T> newLinkedHashSet() {
		return new LinkedHashSet<T>();
	}
	
	public static <T> LinkedHashSet<T> newLinkedHashSet(@Nullable Collection<T> collection) {
		if(collection==null) {
			return newLinkedHashSet();
		}
		return new LinkedHashSet<T>(collection);
	}
	
	// -- CONCURRENT HASH SET
	
	public static <T> KeySetView<T, Boolean> newConcurrentHashSet() {
		return ConcurrentHashMap.newKeySet();
	}
	
	public static <T> KeySetView<T, Boolean> newConcurrentHashSet(@Nullable Collection<T> collection) {
		final KeySetView<T, Boolean> keySetView = newConcurrentHashSet();
		if(collection!=null) {
			keySetView.addAll(collection);
		}
		return keySetView;
	}
	
	// -- 

	
}
