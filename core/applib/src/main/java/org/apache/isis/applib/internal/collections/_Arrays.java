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

import javax.annotation.Nullable;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Common Array idioms.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/> 
 * These may be changed or removed without notice!
 * </p>
 * 
 * @since 2.0.0
 */
public class _Arrays {
	
    // -- PREDICATES

	/**
	 * @param cls
	 * @return whether {@code cls} represents an array
	 */
    public static boolean isArrayType(@Nullable final Class<?> cls) {
        return cls!=null ? cls.isArray() : false;
    }
    
    /**
     * For convenience also provided in {@link _Collections}.
     * @param cls
     * @return whether {@code cls} implements the java.util.Collection interface 
     * or represents an array
     */
    public static boolean isCollectionOrArrayType(final Class<?> cls) {
        return _Collections.isCollectionType(cls) || _Arrays.isArrayType(cls);
    }
	
    // -- ELEMENT TYPE INFERENCE
    
    /**
     * Returns the inferred element type of the specified array type 
     * @param type of the array for which to infer the element type 
     * @return inferred type or null if inference fails
     */
    public static @Nullable Class<?> inferElementTypeIfAny(@Nullable final Class<?> arrayType) {
        if(!isArrayType(arrayType)) {
            return null;
        }
        return arrayType.getComponentType();
    }
    
	// --
	
}
