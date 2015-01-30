/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.unittestsupport.sortedsets;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;
import java.util.SortedSet;

import org.reflections.ReflectionUtils;
import org.reflections.Reflections;

import org.apache.isis.core.unittestsupport.AbstractApplyToAllContractTest;

public abstract class SortedSetsContractTestAbstract extends AbstractApplyToAllContractTest {

    protected SortedSetsContractTestAbstract(
            final String packagePrefix) {
        super(packagePrefix);
    }

    @Override
    protected void applyContractTest(Class<?> entityType) {
        final Set<Field> collectionFields = ReflectionUtils.getAllFields(entityType, ReflectionUtils.withTypeAssignableTo(Collection.class));
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
                ReflectionUtils.withTypeAssignableTo(SortedSet.class).apply(collectionField), is(true));
    }
    
    private String desc(Class<?> entityType, Field collectionField) {
        return entityType.getSimpleName() + "#" + collectionField.getName();
    }


}
