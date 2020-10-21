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

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.jdo.annotations.Unique;
import javax.jdo.annotations.Uniques;

import org.junit.Assert;
import org.junit.Test;

public abstract class WithFieldUniqueContractTestAllAbstract<T> {
    protected final Class<T> interfaceType;
    protected final String fieldName;
    protected final String prefix;

    public WithFieldUniqueContractTestAllAbstract(
            final String prefix, String fieldName, Class<T> interfaceType) {
        this.prefix = prefix;
        this.fieldName = fieldName;
        this.interfaceType = interfaceType;
    }

    private static <T> boolean declaresUniquenesForField(final Class<? extends T> subtype, final Unique unique, String fieldName) {
        final String[] members = unique.members();
        return members != null && members.length == 1 && members[0].equals(fieldName);
    }

    @Test
    public void searchAndTest() {

        final StringBuilder buf = new StringBuilder();

        Set<Class<? extends T>> domainObjectClasses = reflections.getSubTypesOf(interfaceType);
        checkClass:
        for (final Class<? extends T> subtype : domainObjectClasses) {
            if(subtype.isInterface() || subtype.isAnonymousClass() || subtype.isLocalClass() || subtype.isMemberClass()) {
                // skip (probably a testing class)
                continue;
            }

            try {
                subtype.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                // assume inherited field, so skip.
                continue;
            }

            final Annotation[] annotations = subtype.getAnnotations();

            // @Unique(...)
            for (Annotation annotation : annotations) {
                if(annotation instanceof Unique) {
                    final Unique unique = (Unique) annotation;
                    if(declaresUniquenesForField(subtype, unique, fieldName)) {
                        continue checkClass;
                    }
                }
            }

            // @Uniques({ @Unique(...), @Unique(...), ... })
            for (Annotation annotation : annotations) {
                if(annotation instanceof Uniques) {
                    Uniques uniques = (Uniques) annotation;
                    for (Unique unique : uniques.value()) {
                        if(declaresUniquenesForField(subtype, unique, fieldName)) {
                            continue checkClass;
                        }
                    }
                }
            }

            buf.append("\n" + subtype.getName() + " is not annotated with @Unique(members={\"" + fieldName + "\"})");
        }
        if(buf.length() > 0) {
            Assert.fail(buf.toString());
        }
    }
}
