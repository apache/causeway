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
package org.apache.isis.testing.unittestsupport.applib.core.sortedsets;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.isis.core.commons.internal.reflection._Reflect;
import org.apache.isis.core.unittestsupport.AbstractApplyToAllContractTest;

import static org.apache.isis.core.commons.internal.collections._Collections.toHashSet;
import static org.apache.isis.core.commons.internal.reflection._Reflect.withTypeAssignableTo;

/**
 * <p>
 *     Used by domain apps only.
 * </p>
 */
public abstract class SortedSetsContractTestAbstract extends AbstractApplyToAllContractTest {

    protected SortedSetsContractTestAbstract(
            final String packagePrefix) {
        super(packagePrefix);
    }

    @Override
    protected void applyContractTest(Class<?> entityType) {
        final Set<Field> collectionFields = _Reflect.streamAllFields(entityType, true)
                .filter(withTypeAssignableTo(Collection.class))
                .collect(toHashSet());

        for (Field collectionField : collectionFields) {
            try {
                final String desc = desc(entityType, collectionField);
                out.println("processing " + desc);
                out.incrementIndent();
                process(entityType, collectionField);
            } finally {
                out.decrementIndent();
            }
        }
    }

    private void process(Class<?> entityType, Field collectionField) {
        assertThat(
                desc(entityType, collectionField) + " must be a SortedSet",
                _Reflect.withTypeAssignableTo(SortedSet.class).test(collectionField), is(true));
    }

    private String desc(Class<?> entityType, Field collectionField) {
        return entityType.getSimpleName() + "#" + collectionField.getName();
    }


}
