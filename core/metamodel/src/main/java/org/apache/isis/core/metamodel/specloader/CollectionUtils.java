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

package org.apache.isis.core.metamodel.specloader;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Consumer;

import javax.annotation.Nullable;

/**
 * Defines the types which are considered to be collections.
 * 
 * <p>
 * In this way there are similarities with the way in which value types are
 * specified using <tt>@Value</tt>. However, we need to maintain a repository of
 * these collection types once nominated so that when we introspect classes we
 * look for collections first, and then properties second.
 */
public final class CollectionUtils {

    private CollectionUtils() {}

    // -- PREDICATES
    
    public static boolean isCollectionType(@Nullable final Class<?> cls) {
        return cls!=null ? java.util.Collection.class.isAssignableFrom(cls) : false;
    }

    public static boolean isArrayType(@Nullable final Class<?> cls) {
        return cls!=null ? cls.isArray() : false;
    }
    
    /**
     * 
     * @param parameterType
     * @param genericParameterType
     * @return whether the parameter is a (collection or array) and has an infer-able element type
     */
    public static boolean isParamCollection(
    		@Nullable final Class<?> parameterType,
    		@Nullable final Type genericParameterType) {
    	if(inferElementTypeFromArrayType(parameterType) != null) {
    		return true;
    	}
    	if(isCollectionType(parameterType) && inferElementTypeFromGenericType(genericParameterType)!=null) {
    		return true;
    	}
    	return false;
    }

    // -- ELEMENT TYPE INFERENCE (ARRAY)
    
    /**
     * Returns the inferred element type of the specified array type 
     * @param type of the array for which to infer the element type 
     * @return inferred type or null if inference fails
     */
    public static @Nullable Class<?> inferElementTypeFromArrayType(@Nullable final Class<?> type) {
        if(!isArrayType(type)) {
            return null;
        }
        return type.getComponentType();
    }

    // -- ELEMENT TYPE INFERENCE (FROM GENERIC TYPE)
    
    /**
     * Returns the inferred element type of the specified array type
     * @param collectionType
     * @param genericParameterType
     * @return inferred type or null if inference fails
     */
    public static @Nullable Class<?> inferElementTypeFromGenericType(@Nullable final Type genericType) {

    	if(genericType==null) {
    		return null;
    	}

        if(genericType instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType) genericType;
            final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if(actualTypeArguments.length == 1) {
                final Type actualTypeArgument = actualTypeArguments[0];
                if(actualTypeArgument instanceof Class) {
                    final Class<?> actualType = (Class<?>) actualTypeArgument;
                    return actualType;
                }
            }
        }
        
        return null;
    }

    // -- ELEMENT TYPE INFERENCE (FROM FIELD)
    
    /**
	 * If field is of type (or a sub-type of) Collection&lt;T&gt; with generic type T present, 
	 * then call action with the element type.
	 * @param field
	 * @param action
	 */
    public static void ifIsCollectionWithGenericTypeThen(Field field, Consumer<Class<?>> action) {
		
		final Class<?> fieldType = field.getType();
		
		if(isCollectionType(fieldType)) {
			
			final Class<?> elementType = inferElementTypeFromGenericType(field.getGenericType());
			
			if(elementType!=null) {
				action.accept(elementType);
			}
        }
		
	}

}
