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
package org.apache.isis.subdomains.base.applib.with;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import org.apache.isis.commons.internal.reflection._Reflect;
import org.apache.isis.commons.internal.reflection._Reflect.InterfacePolicy;

public abstract class ComparableByNameContractTestAbstract_compareTo {
    protected final String packagePrefix;
    protected Map<Class<?>, Class<?>> noninstantiableSubstitutes;

    public ComparableByNameContractTestAbstract_compareTo(
            String packagePrefix, Map<Class<?>, Class<?>> noninstantiableSubstitutes) {
        this.packagePrefix = packagePrefix;
        this.noninstantiableSubstitutes = noninstantiableSubstitutes;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void searchAndTest() {

        Set<Class<? extends WithNameComparable>> subtypes =
                _Reflect.streamTypeHierarchy(WithNameComparable.class, InterfacePolicy.EXCLUDE)
                .collect(Collectors.toSet());
                
        for (Class<? extends WithNameComparable> subtype : subtypes) {
            if(subtype.isInterface() || subtype.isAnonymousClass() || subtype.isLocalClass() || subtype.isMemberClass()) {
                // skip (probably a testing class)
                continue;
            }
            subtype = instantiable(subtype);
            test(subtype);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Class<? extends WithNameComparable> instantiable(Class<? extends WithNameComparable> cls) {
        final Class<?> substitute = noninstantiableSubstitutes.get(cls);
        return (Class<? extends WithNameComparable>) (substitute!=null?substitute:cls);
    }

    private <T extends WithNameComparable<T>> void test(Class<T> cls) {
        new ComparableByNameContractTester<>(cls).test();
    }
}
