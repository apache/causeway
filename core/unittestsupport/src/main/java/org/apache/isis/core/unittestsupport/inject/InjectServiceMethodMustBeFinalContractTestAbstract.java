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
package org.apache.isis.core.unittestsupport.inject;

import static org.apache.isis.commons.internal.collections._Collections.toHashSet;
import static org.apache.isis.commons.internal.reflection._Reflect.withPrefix;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

import org.apache.isis.commons.internal.reflection._Reflect;
import org.apache.isis.core.unittestsupport.AbstractApplyToAllContractTest;

/**
 * Ensure that subclasses do not inadvertently override an <tt>injectXxx()</tt> method that
 * is defined in a superclass.
 *
 * <p>
 * Doing this will result in the subclass's field being populated, but the superclass's field not
 * (leading to possible {@link NullPointerException}s).
 */
public abstract class InjectServiceMethodMustBeFinalContractTestAbstract extends AbstractApplyToAllContractTest {

    protected InjectServiceMethodMustBeFinalContractTestAbstract(
            final String packagePrefix) {
        super(packagePrefix);
    }

    @Override
    protected void applyContractTest(Class<?> entityType) {
        final Set<Method> injectMethods = _Reflect.streamAllMethods(entityType, true)
                .filter(withPrefix("inject"))
                .collect(toHashSet());

        for (Method injectMethod : injectMethods) {
            try {
                final String desc = desc(entityType, injectMethod);
                out.println("processing " + desc);
                out.incrementIndent();
                process(entityType, injectMethod);
            } finally {
                out.decrementIndent();
            }
        }
    }

    private void process(Class<?> entityType, Method injectMethod) {
        assertThat(
                desc(entityType, injectMethod) + " must be final",
                Modifier.isFinal(injectMethod.getModifiers()), is(true));
    }

    private String desc(Class<?> entityType, Method injectMethod) {
        return entityType.getSimpleName() + "#" + injectMethod.getName() + "(...)";
    }


}
