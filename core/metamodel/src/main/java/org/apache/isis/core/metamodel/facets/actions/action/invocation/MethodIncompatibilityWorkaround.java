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

package org.apache.isis.core.metamodel.facets.actions.action.invocation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import org.apache.isis.applib.internal.base._NullSafe;

/**
 * Package private utility for method invocation pre-processing. 
 */
class MethodIncompatibilityWorkaround {

	static Object invoke(Method method, Object targetPojo, Object[] executionParameters) 
			throws IllegalAccessException, InvocationTargetException {

		if (_NullSafe.isEmpty(executionParameters)) {
			return method.invoke(targetPojo, executionParameters);
		}
		
		final Class<?>[] parameterTypes = method.getParameterTypes();
		final Object[] adaptedExecutionParameters = new Object[executionParameters.length]; 
		
		int i=0;
		
		for(Object param : executionParameters) {
			adaptedExecutionParameters[i] = adapt(param, parameterTypes[i]);
			++i;
		}
		
		return method.invoke(targetPojo, adaptedExecutionParameters);
	}

	// -- OBJECT ADAPTER
	
	/**
	 * Replaces obj (if required) to be conform with the parameterType
	 * @param obj
	 * @param parameterType
	 * @return
	 */
	private static Object adapt(Object obj, Class<?> parameterType) {

		if(obj==null) {
			return null;
		}
		
		// allow no side effects on List arguments
		if(List.class.equals(parameterType)) {
			return adaptAsList((List<?>)obj);
		}

		// adapt set
		if(Set.class.equals(parameterType)) {
			return adaptAsSet((List<?>)obj);
		}
		
		// adapt sorted
		if(SortedSet.class.equals(parameterType)) {
			return adaptAsSortedSet((List<?>)obj);
		}
		
		return obj;
	}
	
	// -- COLLECTION ADAPTER

	/**
	 * Adapts the list as unmodifiable list.
	 * @param list
	 * @return
	 */
	private static <T> List<T> adaptAsList(final List<T> list) {
		return Collections.unmodifiableList(list);
	}

	/**
	 * Preserves order, adapts the Set interface.
	 * @param list
	 * @return
	 */
	private static <T> Set<T> adaptAsSet(final List<T> list) {
		return Collections.unmodifiableSet(
				list.stream()
				.collect(Collectors.toCollection(LinkedHashSet::new)));
	}

	private final static String JUST_AN_ADAPTER = 
			"this set is just an adapter, it has no information about the intended comparator";
	
	/**
	 * Preserves order, adapts the SortedSet interface.
	 * @param list
	 * @return
	 */
	private static <T> SortedSet<T> adaptAsSortedSet(final List<T> list) {
		return new SortedSet<T>() {
			
			@Override
			public int size() {
				return list.size();
			}

			@Override
			public boolean isEmpty() {
				return list.isEmpty();
			}

			@Override
			public boolean contains(Object o) {
				throw new UnsupportedOperationException(JUST_AN_ADAPTER);
			}

			@Override
			public Iterator<T> iterator() {
				return list.iterator();
			}

			@Override
			public Object[] toArray() {
				return list.toArray();
			}

			@Override
			public <X> X[] toArray(X[] a) {
				return list.toArray(a);
			}

			@Override
			public boolean add(T e) {
				throw new UnsupportedOperationException("unmodifiable");
			}

			@Override
			public boolean remove(Object o) {
				throw new UnsupportedOperationException("unmodifiable");
			}

			@Override
			public boolean containsAll(Collection<?> c) {
				throw new UnsupportedOperationException(JUST_AN_ADAPTER);
			}

			@Override
			public boolean addAll(Collection<? extends T> c) {
				throw new UnsupportedOperationException("unmodifiable");
			}

			@Override
			public boolean retainAll(Collection<?> c) {
				throw new UnsupportedOperationException("unmodifiable");
			}

			@Override
			public boolean removeAll(Collection<?> c) {
				throw new UnsupportedOperationException("unmodifiable");
			}

			@Override
			public void clear() {
				throw new UnsupportedOperationException("unmodifiable");
			}

			@Override
			public Comparator<? super T> comparator() {
				throw new UnsupportedOperationException(JUST_AN_ADAPTER);
			}

			@Override
			public SortedSet<T> subSet(T fromElement, T toElement) {
				throw new UnsupportedOperationException(JUST_AN_ADAPTER);
			}

			@Override
			public SortedSet<T> headSet(T toElement) {
				throw new UnsupportedOperationException(JUST_AN_ADAPTER);
			}

			@Override
			public SortedSet<T> tailSet(T fromElement) {
				throw new UnsupportedOperationException(JUST_AN_ADAPTER);
			}

			@Override
			public T first() {
				if(size()==0) {
					throw new NoSuchElementException("set is empty");
				}
				return list.get(0);
			}

			@Override
			public T last() {
				if(size()==0) {
					throw new NoSuchElementException("set is empty");
				}
				return list.get(size()-1);
			}
		};
	}

}
