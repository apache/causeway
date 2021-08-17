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
package org.apache.isis.core.config.beans;

import java.util.Set;
import java.util.function.Predicate;

import org.checkerframework.checker.nullness.qual.Nullable;

import org.springframework.context.ApplicationContext;

import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.context._Plugin;

import lombok.NonNull;
import lombok.Value;

/**
 * ServiceLoader SPI that allows for implementing instances to have a say during bean type scanning.
 * @since 2.0
 */
public interface IsisBeanTypeClassifier {

    // -- INTERFACE

    /**
     * Returns the bean classification for given {@code type}.
     *
     * @apiNote Initially used to collect all concrete types that are considered by Spring
     * for type inspection, most likely without any {@code context} yet being available,
     * but later used by the {@code SpecificationLoader} to also
     * classify non-concrete types (interfaces and abstract classes).
     */
    BeanClassification classify(Class<?> type, @Nullable BeanClassificationContext context);

    // -- FACTORY

    /**
     * in support of JUnit testing
     */
    static IsisBeanTypeClassifier createInstance() {
        return new IsisBeanTypeClassifierImpl(Can.empty());
    }

    static IsisBeanTypeClassifier createInstance(final @NonNull ApplicationContext applicationContext) {
        return new IsisBeanTypeClassifierImpl(
                Can.ofArray(applicationContext.getEnvironment().getActiveProfiles()));
    }

    // -- LOOKUP

    public static Can<IsisBeanTypeClassifier> get() {
        return Can.ofCollection(_Plugin.loadAll(IsisBeanTypeClassifier.class));
    }

    // -- BEAN CLASSIFICATION CONTEXT

    @Value
    public static class BeanClassificationContext {
        private final @NonNull Predicate<Class<?>> isRegisteredValueType;
    }

    static BeanClassificationContext newContext(final Set<Class<?>> registeredValueTypes) {
        return new BeanClassificationContext(registeredValueTypes::contains);
    }

    // -- BEAN CLASSIFICATION RESULT

    @Value(staticConstructor = "of")
    public static class BeanClassification {

        BeanSort beanSort;
        String explicitLogicalTypeName;
        boolean delegateLifecycleManagement;

        // -- FACTORIES

        public static BeanClassification delegated(BeanSort beanSort, String explicitLogicalTypeName) {
            return of(beanSort, explicitLogicalTypeName, true);
        }

        public static BeanClassification delegated(BeanSort beanSort) {
            return delegated(beanSort, null);
        }

        public static BeanClassification selfManaged(BeanSort beanSort, String explicitLogicalTypeName) {
            return of(beanSort, explicitLogicalTypeName, false);
        }

        public static BeanClassification selfManaged(BeanSort beanSort) {
            return selfManaged(beanSort, null);
        }

    }





}
