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
package org.apache.isis.core.metamodel.util.pchain;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Implements a caching {@code Map} where objects are stored referenced by 
 * a unique key, while {@codeMap.Entries} might be garbage collected any time. 
 * 
 * @author ahuber@apache.org
 *
 * @param <K>
 * @param <T>
 */
class SoftCache<K,T> {
	
	private Map<K,SoftReference<T>> data;
	
	public SoftCache() {
		data=newMap();
	}
	
	public SoftCache(Supplier<Map<K,SoftReference<T>>> mapFactory) {
		data=mapFactory.get();
	}
	
	/**
	 * Note: might be overridden to use a different map implementation for storage
	 * @return
	 */
	protected Map<K,SoftReference<T>> newMap(){
		return new HashMap<>();
	}
	
	/**
	 * Note: call to this method will fool the garbage collector, 
	 * so that last objects in the entry set will be kept longer, 
	 * due to latest access  
	 * @return number of currently usable SoftReferences 
	 */
	public int computeSize(){
		Map<K,SoftReference<T>> keep = newMap();
		for(Map.Entry<K,SoftReference<T>> entry : data.entrySet()){
			if(entry.getValue()!=null) keep.put(entry.getKey(),entry.getValue()); 
		}
		data.clear();
		data=keep;
		return data.size();
	}
	
	// keep private! (result is not guaranteed to be accurate, 
	// since the garbage collector may change the soft references any time)
	@SuppressWarnings("unused")
	private boolean contains(K key){
		return get(key)!=null;
	}
	
	public void put(K key, T x){
		data.put(key, new SoftReference<T>(x));
	}
	
	public T get(K key){
		SoftReference<T> ref = data.get(key); 
		if(ref==null) {
			data.remove(key);
			return null;
		}
		return ref.get();
	}

	public void clear() {
		data.clear();		
	}

	/**
	 * Tries to fetch a value from cache and returns it if it's a hit. 
	 * Otherwise stores and returns the value supplied by the mappingFunction.
	 * @param key
	 * @param mappingFunction
	 * @return either the value stored under key or (if there is no such key) the result from the factory 
	 */
	public T computeIfAbsent(K key, Function<? super K,? extends T> mappingFunction){
		return computeIfAbsent(key,()->mappingFunction.apply(key));
	}
	
	/**
	 * Tries to fetch a value from cache and returns it if it's a hit. 
	 * Otherwise stores and returns the value supplied by the factory.
	 * @param key
	 * @param factory
	 * @return either the value stored under key or (if there is no such key) the result from the factory 
	 */
	public T computeIfAbsent(K key, Supplier<T> factory) {
		T res = get(key);
		if(res!=null)
			return res;
		res = factory.get();
		put(key,res); 
		return res;
	}
	
}

