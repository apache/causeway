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

package org.apache.isis.applib.internal.memento;

import java.util.Set;

import org.apache.isis.applib.services.urlencoding.UrlEncodingService;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Provides framework internal memento support.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/> 
 * These may be changed or removed without notice!
 * </p>
 * 
 * @since 2.0.0
 */
public final class _Mementos {

	private _Mementos(){}
	
	// -- MEMENTO INTERFACE
	
    public static interface Memento {

        public Memento set(String name, Object value);

        public <T> T get(String name, Class<T> cls);

        /**
         * @return To-String serialization of this Memento. 
         */
        public String asString();
        
        public Set<String> keySet();
    }
    
    // -- CONSTRUCTION
    
    /**
     * Creates an empty {@link Memento}.
     * 
     * <p>
     * Typically followed by {@link Memento#set(String, Object)} for each of the data values to
     * add to the {@link Memento}, then {@link Memento#asString()} to convert to a string format.
     *
     */
    public static Memento create(UrlEncodingService codec) {
    	return new _Mementos_MementoDefault(codec);
    }

    /**
     * Parse string returned from {@link Memento#asString()}
     * 
     * <p>
     * Typically followed by {@link Memento#get(String, Class)} for each of the data values held
     * in the {@link Memento}. 
     *
     */
    public static Memento parse(UrlEncodingService codec, final String str) {
		return _Mementos_MementoDefault.parse(codec, str);
    	
    }
	
}
