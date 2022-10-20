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
package org.apache.causeway.core.config.beans;

import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.context._Context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;

@RequiredArgsConstructor(staticName = "of")
final class ScannedTypeMetaData {

    /**
     * Fully qualified name of the underlying class.
     */
    @Getter private final String className;

    /**
     * As proposed by IoC, before any overrides.
     */
    @Getter private final String proposedBeanName;

    /**
     * Name override, applied only if not empty.
     */
    @Getter @Setter private String beanNameOverride;

    /**
     * Whether this type is vetoed for injection,
     * otherwise is made available for Spring to decide whether to use for injection.
     */
    @Getter @Setter private boolean vetoedForInjection = false;

    @Getter(lazy=true)
    private final Try<Class<?>> underlyingClass = resolveClass();

    // -- UTILITY

    public String getEffectiveBeanName() {
        return _Strings.isNullOrEmpty(beanNameOverride)
                ? proposedBeanName
                : beanNameOverride;
    }

    // -- HELPER

    /**
     * @return the underlying class of this TypeMetaData
     */
    private Try<Class<?>> resolveClass() {
        return Try.<Class<?>>call(()->_Context.loadClass(className))
        .mapFailure(ex->{
            val msg = String.format("Failed to load class for name '%s'", className);
            return new RuntimeException(msg, ex);
        });
    }


}
