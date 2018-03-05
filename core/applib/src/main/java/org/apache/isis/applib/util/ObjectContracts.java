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

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

import org.apache.isis.applib.internal.base._Casts;
import org.apache.isis.applib.internal.base._NullSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public final class ObjectContracts {
	
	private static final Logger LOG = LoggerFactory.getLogger(ObjectContracts.class);
	
	private ObjectContracts() {}
	
	public static <T> ToString<T> toString(String name, Function<T, ?> getter) {
		return ToString.toString(name, getter);
	}
	
	public static <T> Equality<T> checkEquals(Function<T, ?> getter) {
		return Equality.checkEquals(getter);
	}
	
	public static <T> Hashing<T> hashing(Function<T, ?> getter) {
		return Hashing.hashing(getter);
	}
	
    /**
     * WARNING Possible misuse because of forgetting respectively the last method 
     * argument with {@code equals}, [@code hashCode} and {@code toString}!
     *  
     * @since 2.0.0
     * @param <T>
     */
	public static interface ObjectContract<T> {

		public int compare(T obj, T other);

		public boolean equals(T obj, Object other);

		public int hashCode(T obj);

		public String toString(T obj);
		
	    // -- TO STRING EVALUATION

		/**
		 * True 'wither' (each call returns a new instance of ObjectContract)!
		 * @param valueToStringFunction
		 * @return ObjectContract with valueToStringFunction to apply to property values when 
		 * processing the toString algorithm.
		 */
		public ObjectContract<T> withValueToStringFunction(Function<Object, String> valueToStringFunction);
		
		// -- COMPOSITION
		
		/**
		 * Contract composition. Any valueToStringFunction is 'copied over'.
		 * @param propertyLabel a label to use for property to string output 
		 * (often, but not necessarily the property name)
		 * @param getter function giving the property value for an object
		 * @param comparator
		 * @return
		 */
		public ObjectContract<T> thenUse(String propertyLabel, Function<T, ?> getter, Comparator<T> comparator);

		public static <T> ObjectContract<T> empty() {
			return new ObjectContract_Empty<>();
		}
		
	}
	
	public static <T, C extends Comparable<C>> ObjectContract<T> use(
			Class<T> target, String propertyLabel, Function<T, ?> getter, Comparator<T> comparator) {
		
		return new ObjectContract_Impl<T>(
				Equality.checkEquals(getter), 
				Hashing.hashing(getter), 
				ToString.toString(propertyLabel, getter), 
				comparator);
	}
			
	
	public static <T> ObjectContract<T> parse(Class<T> target, String propertyNames) {
		return ObjectContract_Parser.parse(target, propertyNames);
	}

	// -- BACKWARDS COMPATIBILITY TO-STRING EVALUATOR
	
    public interface ToStringEvaluator {
    	
        public boolean canEvaluate(Object o);
        public String evaluate(Object o);
        
        public static Function<Object, String> combineToFunction(ToStringEvaluator ... evaluators){
        	return value -> {
    	        if(value == null) {
    	            return null;
    	        }
    	        if(!_NullSafe.isEmpty(evaluators)) {
    		        for (ToStringEvaluator evaluator : evaluators) {
    		            if(evaluator.canEvaluate(value)) {
    		                return evaluator.evaluate(value);
    		            }
    		        }
    	        }
    	        return value.toString();
    		};
        }
    }
    
    // -- BACKWARDS COMPATIBILITY
	
	@Deprecated // uses reflection on each call
	public static <T> String toString(T obj, String propertyNames) {
		Objects.requireNonNull(obj, "obj required, otherwise undecidable");
		
		return parse(_Casts.uncheckedCast(obj.getClass()), propertyNames)
				.toString(obj);
	}
	
	@Deprecated // uses reflection on each call
	public static <T> boolean equals(T obj, Object other, String propertyNames) {
		
		if(obj==null && other==null) {
			if(LOG.isWarnEnabled()) {
				LOG.warn("potential misuse of <T> ObjectContracts::equals(T obj, Object other, "
						+ "String propertyNames). First argument is not expected to be null!");
			}
			return true;
		}
		
		Objects.requireNonNull(obj, "obj required, otherwise undecidable"); 
		
		return parse(_Casts.uncheckedCast(obj.getClass()), propertyNames)
				.equals(obj, other);
	}
	
	@Deprecated // uses reflection on each call
	public static int hashCode(Object obj, String propertyNames) {
		Objects.requireNonNull(obj, "obj required, otherwise undecidable");

		return parse(_Casts.uncheckedCast(obj.getClass()), propertyNames)
				.hashCode(obj);
	}

	@Deprecated // uses reflection on each call
	public static <T> int compare(T obj, T other, String propertyNames) {
		Objects.requireNonNull(obj, "obj required, otherwise undecidable");

		return parse(_Casts.uncheckedCast(obj.getClass()), propertyNames)
				.compare(obj, other);
	}

	
	
}
