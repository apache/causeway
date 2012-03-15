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

package org.apache.isis.core.metamodel.specloader.classsubstitutor;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

public class ClassSubstitutorComposite implements ClassSubstitutor {

    private final List<ClassSubstitutor> substitutors = Lists.newArrayList();

    public ClassSubstitutorComposite(final List<ClassSubstitutor> substitutors) {
        this.substitutors.addAll(substitutors);
    }

    public ClassSubstitutorComposite(final ClassSubstitutor... substitutors) {
        this(Arrays.asList(substitutors));
    }

    @Override
    public void init() {
        for (final ClassSubstitutor classSubstitutor : substitutors) {
            classSubstitutor.init();
        }
    }

    @Override
    public void shutdown() {
        for (final ClassSubstitutor classSubstitutor : substitutors) {
            classSubstitutor.shutdown();
        }
    }

    @Override
    public Class<?> getClass(Class<?> cls) {
        Class<?> clsBefore;
        do {
            clsBefore = cls;
            for (final ClassSubstitutor substitutor : substitutors) {
                cls = substitutor.getClass(cls);
                if (cls == null) {
                    return null;
                }
            }
        } while (clsBefore != cls);
        return cls;
    }

    @Override
    public void injectInto(final Object candidate) {
        if (ClassSubstitutorAware.class.isAssignableFrom(candidate.getClass())) {
            final ClassSubstitutorAware cast = ClassSubstitutorAware.class.cast(candidate);
            cast.setClassSubstitutor(this);
        }
    }

}
