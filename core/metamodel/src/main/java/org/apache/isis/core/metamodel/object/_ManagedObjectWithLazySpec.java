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
package org.apache.isis.core.metamodel.object;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@EqualsAndHashCode(of = "pojo", callSuper = false)
final class _ManagedObjectWithLazySpec
extends _ManagedObjectWithBookmark {

    @NonNull private final Function<Class<?>, ObjectSpecification> specLoader;

    @Getter @NonNull private /*final*/ Object pojo;

    private final _Lazy<ObjectSpecification> specification = _Lazy.threadSafe(this::loadSpec);

    public _ManagedObjectWithLazySpec(
            final @NonNull Function<Class<?>, ObjectSpecification> specLoader,
            final @NonNull Object pojo) {
        this.specLoader = specLoader;
        this.pojo = pojo;
    }

    @Override
    public ObjectSpecification getSpecification() {
        return specification.get();
    }

    @Override //ISIS-2317 make sure toString() is without side-effects
    public String toString() {
        if(specification.isMemoized()) {
            return String.format("ManagedObject[spec=%s, pojo=%s]",
                    ""+getSpecification(),
                    ""+getPojo());
        }
        return String.format("ManagedObject[spec=%s, pojo=%s]",
                "[lazy not loaded]",
                ""+getPojo());
    }

    private ObjectSpecification loadSpec() {
        return specLoader.apply(pojo.getClass());
    }

    @Override
    public void replacePojo(final UnaryOperator<Object> replacer) {
        pojo = replacer.apply(pojo);
        if(specification.isMemoized()) {
            assertSpecIsInSyncWithPojo();
        }
    }

}