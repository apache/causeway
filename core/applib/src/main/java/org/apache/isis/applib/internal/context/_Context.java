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

package org.apache.isis.applib.internal.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.apache.isis.applib.internal.base._Casts;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Provides a context for storing and retrieving singletons (usually application scoped).
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/> 
 * These may be changed or removed without notice!
 * </p>
 * @since 2.0.0
 */
public final class _Context {

	private _Context(){}
	
	private final static Map<String, Object> singletonMap = new HashMap<>(); 

	/**
	 * Puts a singleton instance onto the current context.
	 * @param type non-null
	 * @param singleton non-null
	 * @throws IllegalStateException if there is already an instance of same type on the current context.
	 */
	public static void putSingleton(Class<?> type, Object singleton) {
		Objects.requireNonNull(type);
		Objects.requireNonNull(singleton);
		
		if(singletonMap.containsKey(toKey(type)))
			throw new IllegalStateException("there is already a singleton of type '"+type+"' on this context.");
		
		singletonMap.put(toKey(type), singleton);
	}

	/**
	 * Gets a singleton instance of {@code type} if there is any, null otherwise.
	 * @param type non-null
	 * @return null, if there is no such instance
	 */
	public static <T> T getIfAny(Class<? super T> type) {
		return _Casts.uncheckedCast(singletonMap.get(toKey(type)));
	}
	
	/**
	 * Gets a singleton instance of {@code type} if there is any, 
	 * otherwise returns the {@code fallback}'s result,
	 * which could be null.
	 * @param type non-null
	 * @param fallback non-null
	 * @return
	 */
	public static <T> T getOrElse(Class<? super T> type, Supplier<T> fallback) {
		Objects.requireNonNull(fallback);
		final T singleton = getIfAny(type);
		if(singleton!=null) {
			return singleton;
		}
		return fallback.get(); 
	}
	
	/**
	 * Gets a singleton instance of {@code type} if there is any, 
	 * otherwise throws the {@code onNotFound}'s result,
	 * which could be null.
	 * @param type non-null
	 * @param onNotFound non-null
	 * @return
	 * @throws Exception 
	 */
	public static <T, E extends Exception> T getOrThrow(
			Class<? super T> type, 
			Supplier<E> onNotFound) 
			throws E {
		Objects.requireNonNull(onNotFound);
		final T singleton = getIfAny(type);
		if(singleton!=null) {
			return singleton;
		}
		throw onNotFound.get();
	}
	
	/**
	 * Removes any singleton references from the current context.
	 */
	public static void clear() {
		singletonMap.clear();
	}
	
	// -- DEFAULT CLASSLOADER
	
	private final static Supplier<ClassLoader> FALLBACK_CLASSLOADER = 
			Thread.currentThread()::getContextClassLoader;
	
	/**
	 * Will be set by the framework's bootstrapping mechanism if required.
	 * @return the default class loader
	 */
	public static ClassLoader getDefaultClassLoader() {
		return getOrElse(ClassLoader.class, FALLBACK_CLASSLOADER);
	}

	// -- HELPER
	
	private static String toKey(Class<?> type) {
		return type.getName();
	}

	
}
