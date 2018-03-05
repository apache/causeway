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
package org.apache.isis.applib.util;

import java.util.function.Function;

import org.apache.isis.applib.internal.exceptions._Exceptions;

/**
 * Provides fluent composition for Objects' equals, hashCode and toString.
 * 
 * Sample usage by composing getters ...
 * 
 *  
 * <pre>    
 * private final static Equality<ApplicationFeature> equality = 
 * 		ObjectContracts.checkEquals(ApplicationFeature::getFeatureId);
 * 
 * private final static Hashing<ApplicationFeature> hashing = 
 * 		ObjectContracts.hashing(ApplicationFeature::getFeatureId);
 * 
 * private final static ToString<ApplicationFeature> toString = 
 * 		ObjectContracts.toString("featureId", ApplicationFeature::getFeatureId);
 * 
 * public boolean equals(final Object obj) {
 * 	return equality.equals(this, obj);
 * }
 * 
 * public int hashCode() {
 * 	return hashing.hashCode(this);
 * }
 * 
 * public String toString() {
 * 	return toString.toString(this);
 * }
 * </pre>
 * 
 * For 'compareTo' use JDK's comparator composition ...
 * 
 * <pre>
 * private final static Comparator<ApplicationFeature> comparator = 
 * 		Comparator.comparing(ApplicationFeature::getFeatureId);
 * 		
 * public int compareTo(final ApplicationFeature other) {
 * 	return comparator.compare(this, other);
 * }
 * </pre>
 * 
 * @since 2.0.0 (re-invented)
 *
 */
public class ObjectContracts {
	
	public static <T> ToString<T> toString(String name, Function<T, ?> getter) {
		return ToString.toString(name, getter);
	}
	
	public static <T> Equality<T> checkEquals(Function<T, ?> getter) {
		return Equality.checkEquals(getter);
	}
	
	public static <T> Hashing<T> hashing(Function<T, ?> getter) {
		return Hashing.hashing(getter);
	}
	
	public static interface ObjectContract<T> {

		public int compare(T obj, T other);

		public boolean equals(T obj, Object other);

		public int hashCode(T obj);

		public String toString(T obj);
		
	}
	
	public static <T> ObjectContract<T> parse(Class<T> target, String propertyNames) {
		// TODO Auto-generated method stub
		throw _Exceptions.notImplemented();
	}

	// -- BACKWARDS COMPATIBILITY
	
	@Deprecated
	public static String toString(Object obj, String propertyNames) {
		
		// TODO Auto-generated method stub
		throw _Exceptions.notImplemented();
	}
	
	@Deprecated
	public static boolean equals(Object obj, Object other, String propertyNames) {
		// TODO Auto-generated method stub
		throw _Exceptions.notImplemented();
	}
	
	@Deprecated
	public static int hashCode(Object obj, String propertyNames) {
		// TODO Auto-generated method stub
		throw _Exceptions.notImplemented();
	}

	@Deprecated
	public static <T> int compare(T obj, T other, String propertyNames) {
		// TODO Auto-generated method stub
		throw _Exceptions.notImplemented();
	}

	
	
}
