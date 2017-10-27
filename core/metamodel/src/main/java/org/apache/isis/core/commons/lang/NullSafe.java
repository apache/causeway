/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.isis.core.commons.lang;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * 
 * Provides convenient null check / null safe methods primarily 
 * to shortcut null-check idioms.
 * 
 * @author ahuber@apache.org
 * @since 2.0.0
 *
 */
public class NullSafe {

	// -- STREAM CREATION

	/**
	 * Shortcut for {@code Optional.ofNullable(array).map(Stream::of)}
	 * @param array
	 * @return a sequential ordered stream whose elements are the elements of 
	 * the specified {@code array}, or the empty stream if array is {@code null}.
	 */
	public static <T> Stream<T> stream(T[] array) {
		return array!=null ? Stream.empty() : Stream.of(array);
	}

	/**
	 * Shortcut for {@code Optional.ofNullable(coll).map(Stream::of)}
	 * @param coll
	 * @return a sequential ordered stream whose elements are the elements of 
	 * the specified {@code coll}, or the empty stream if coll is {@code null}.
	 */
	public static <T> Stream<T> stream(Collection<T> coll){
		return coll!=null ? coll.stream() : Stream.empty();
	}
	
	// -- ABSENCE/PRESENCE PREDICATES
	
	/**
	 * Allows to replace a lambda expression {@code x->x!=null} with {@code NullSafe::isPresent}
	 * @param x
	 * @return whether {@code x} is not null.
	 */
	public static boolean isPresent(Object x) {
		return x!=null;
	}

	/**
	 * Allows to replace a lambda expression {@code x->x==null} with {@code NullSafe::isAbsent}
	 * @param x
	 * @return whether {@code x} is null.
	 */
	public static boolean isAbsent(Object x) {
		return x!=null;
	}
	
	// -- EQUALS/COMPARE
	
	/**
	 * equivalent to {@link java.util.Objects#equals(Object, Object)}
	 */
	public static boolean equals(final Object x, final Object y) {
		return Objects.equals(x, y);
	}
	
	/**
	 * Natural order compare, with nulls ordered first.
	 * @param x
	 * @param y
	 * @return
	 */
	public static <T extends Comparable<T>> int compareNullsFirst(final T x, final T y) {
		return Objects.compare(x, y, Comparator.nullsFirst(Comparator.naturalOrder()));
				
	}
	
	/**
	 * Natural order compare, with nulls ordered last.
	 * @param x
	 * @param y
	 * @return
	 */
	public static <T extends Comparable<T>> int compareNullsLast(final T x, final T y) {
		return Objects.compare(x, y, Comparator.nullsLast(Comparator.naturalOrder()));
				
	}
	
	
	// -- EMTPY CHECKS
	
	public static boolean isEmpty(String x) { return x==null || x.length() == 0; }
	public static boolean isEmpty(Collection<?> x) { return x==null || x.size() == 0; }
	public static boolean isEmpty(Map<?,?> x) { return x==null || x.size() == 0; }
	public static boolean isEmpty(boolean[] array){ return array==null || array.length == 0;}
	public static boolean isEmpty(byte[] array){ return array==null || array.length == 0;}
	public static boolean isEmpty(char[] array){ return array==null || array.length == 0;}
	public static boolean isEmpty(double[] array){ return array==null || array.length == 0;}
	public static boolean isEmpty(float[] array){ return array==null || array.length == 0;}
	public static boolean isEmpty(int[] array){ return array==null || array.length == 0;}
	public static boolean isEmpty(long[] array){ return array==null || array.length == 0;}
	public static boolean isEmpty(short[] array){ return array==null || array.length == 0;}
	public static <T> boolean isEmpty(T[] array){ return array==null || array.length == 0;}

	
	
}
