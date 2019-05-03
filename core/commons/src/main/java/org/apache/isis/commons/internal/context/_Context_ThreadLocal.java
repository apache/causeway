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

package org.apache.isis.commons.internal.context;

import static org.apache.isis.commons.internal.base._With.requires;

import java.util.HashMap;
import java.util.Map;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.core.commons.collections.Bin;

import lombok.val;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Package private mixin for _Context. 
 * Provides a context for storing and retrieving thread local object references.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 * @since 2.0.0-M3
 */
final class _Context_ThreadLocal {

	//TODO [2033] cleanup comments ...
	
    // -- MIXINS
    
	static <T> void put(Class<? super T> type, T variant) {
		requires(type, "type");
    	requires(variant, "variant");
    	THREAD_LOCAL_MAP.get()
    	.compute(type, (k, v) -> v == null 
    		? Bin.<T>ofSingleton(variant)
    				: Bin.<T>concat(_Casts.uncheckedCast(v), variant));
    }
	
//    static <T> void put(Class<? super T> type, Object payload, Runnable onCleanup) {
//    	requires(type, "type");
//    	requires(payload, "payload");
//    	requires(onCleanup, "onCleanup");
//    	THREAD_LOCAL_MAP.get().put(type, Payload.of(payload, onCleanup));
//    }
	
	static <T> Bin<T> select(Class<? super T> type, Class<? super T> instanceOf) {
		val bin = _Context_ThreadLocal.<T>get(type);
		return bin.filter(t -> isInstanceOf(t, instanceOf));
	}
	
	private static boolean isInstanceOf(Object obj, Class<?> type) {
		return type.isAssignableFrom(obj.getClass());
	}
    
    static <T> Bin<T> get(Class<? super T> type) {
    	val bin = THREAD_LOCAL_MAP.get().get(type);
    	if(bin!=null) {
    		return _Casts.uncheckedCast(bin);
    	}
    	return Bin.empty();
    }
    
    static void cleanupThread() {
//    	THREAD_LOCAL_MAP.get().forEach((key, payload)->payload.cleanUp());
    	THREAD_LOCAL_MAP.remove();
    }
    
    // -- HELPER
    
    private _Context_ThreadLocal(){}

	/**
	 * Inheritable... allows to have concurrent computations utilizing the ForkJoinPool.
	 */
    private final static ThreadLocal<Map<Class<?>, Bin<?>>> THREAD_LOCAL_MAP = 
    		InheritableThreadLocal.withInitial(HashMap::new);


//  @Value(staticConstructor="of")
//  private final static class Payload<T> {
//		final Instance<T> instance;
//  	final Runnable onCleanup;
//  	void cleanUp() {
//			if(onCleanup!=null) {
//				onCleanup.run();
//			}
//		}
//  }


    
    
}
