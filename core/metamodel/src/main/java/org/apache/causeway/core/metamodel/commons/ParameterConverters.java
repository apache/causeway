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
package org.apache.causeway.core.metamodel.commons;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.reflection._MethodFacades.ParameterConverter;
import org.apache.causeway.commons.semantics.CollectionSemantics;

import lombok.experimental.UtilityClass;

/**
 * Helper for {@link CanonicalInvoker}.
 */
@UtilityClass
public class ParameterConverters {

    public static ParameterConverter DEFAULT = new Default();

    static class Default implements ParameterConverter {

        @Override
        public <T> T convert(final Class<T> parameterType, final Object parameterValue) {
            return _Casts.uncheckedCast(_adaptToType(parameterType, parameterValue));
        }

        private Object _adaptToType(final Class<?> parameterType, final Object parameterValue) {

            if(parameterValue==null) {
                return parameterType.isPrimitive()
                        ? ClassUtil.defaultByPrimitive.get(parameterType)
                        : null;
            }

            return CollectionSemantics.valueOf(parameterType)
            .map(collectionType->collectionType
                    .unmodifiableCopyOf(parameterType, (Iterable<?>) parameterValue))
            .orElse(parameterValue);
        }

    }

}
