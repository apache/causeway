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

import java.io.Serializable;

import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.commons.internal.base._Strings;

import lombok.NonNull;

public record CausewayBeanMetaData(
        @NonNull LogicalType logicalType,
        @NonNull BeanSort beanSort,
        @NonNull DiscoveredBy discoveredBy,
        @NonNull ManagedBy managedBy,
        @NonNull PersistenceStack persistenceStack)
implements Serializable {

    public enum PersistenceStack {
        NONE,
        JPA,
        JDO,
        /**
         * eg. abstract entity type
         */
        UNSPECIFIED;
        public boolean isNone() { return this == NONE; }
        public boolean isJpa() { return this == JPA; }
        public boolean isJdo() { return this == JDO; }
        public boolean isUnspecified() { return this == UNSPECIFIED; }

        public String titleCase() {
            return _Strings.capitalize(_Strings.lower(name()));
        }
    }

    public enum DiscoveredBy {
        CAUSEWAY,
        SPRING;
        boolean isCauseway() { return this == CAUSEWAY; }
        boolean isSpring() { return this == SPRING; }
    }

    public enum ManagedBy {
        NONE,
        CAUSEWAY,
        SPRING,
        PERSISTENCE,
        /** other Spring managed component, or not managed at all */
        UNSPECIFIED;
        public boolean isNone() { return this == NONE; }
        public boolean isCauseway() { return this == CAUSEWAY; }
        public boolean isSpring() { return this == SPRING; }
        public boolean isPersistence() { return this == PERSISTENCE; }
        public boolean isUnspecified() { return this == UNSPECIFIED; }
    }

    public Class<?> getCorrespondingClass() {
        return logicalType.correspondingClass();
    }

    public String getBeanName() {
        return logicalType.logicalName();
    }

    // -- FACTORIES

    public static CausewayBeanMetaData vetoed(
            final @NonNull LogicalType logicalType,
            final @NonNull DiscoveredBy discoveredBy) {
        return new CausewayBeanMetaData(logicalType, BeanSort.VETOED, discoveredBy, ManagedBy.NONE, PersistenceStack.NONE);
    }
    public static CausewayBeanMetaData value(
            final @NonNull LogicalType logicalType,
            final @NonNull DiscoveredBy discoveredBy) {
        return new CausewayBeanMetaData(logicalType, BeanSort.VALUE, discoveredBy, ManagedBy.NONE, PersistenceStack.NONE);
    }
    public static CausewayBeanMetaData collection(
            final @NonNull LogicalType logicalType,
            final @NonNull DiscoveredBy discoveredBy) {
        return new CausewayBeanMetaData(logicalType, BeanSort.COLLECTION, discoveredBy, ManagedBy.CAUSEWAY, PersistenceStack.NONE);
    }
    public static CausewayBeanMetaData interfaceOrAbstract(
            final @NonNull LogicalType logicalType,
            final @NonNull DiscoveredBy discoveredBy) {
        return new CausewayBeanMetaData(logicalType, BeanSort.ABSTRACT, discoveredBy, ManagedBy.UNSPECIFIED, PersistenceStack.NONE);
    }
    public static CausewayBeanMetaData viewModel(
            final @NonNull LogicalType logicalType,
            final @NonNull DiscoveredBy discoveredBy) {
        return new CausewayBeanMetaData(logicalType, BeanSort.VIEW_MODEL, discoveredBy, ManagedBy.CAUSEWAY, PersistenceStack.NONE);
    }
    public static CausewayBeanMetaData entity(
            final @NonNull LogicalType logicalType,
            final @NonNull DiscoveredBy discoveredBy,
            final @NonNull PersistenceStack persistenceStack) {
        return new CausewayBeanMetaData(logicalType, BeanSort.ENTITY, discoveredBy, ManagedBy.PERSISTENCE, persistenceStack);
    }
    public static CausewayBeanMetaData mixin(
            final @NonNull LogicalType logicalType,
            final @NonNull DiscoveredBy discoveredBy) {
        return new CausewayBeanMetaData(logicalType, BeanSort.MIXIN, discoveredBy, ManagedBy.CAUSEWAY, PersistenceStack.NONE);
    }
    public static CausewayBeanMetaData programmatic(
            final @NonNull LogicalType logicalType) {
        return new CausewayBeanMetaData(logicalType, BeanSort.PROGRAMMATIC, DiscoveredBy.CAUSEWAY, ManagedBy.CAUSEWAY, PersistenceStack.NONE);
    }
    public static CausewayBeanMetaData springContributing(
            final @NonNull LogicalType logicalType) {
        return new CausewayBeanMetaData(logicalType, BeanSort.MANAGED_BEAN_CONTRIBUTING, DiscoveredBy.SPRING, ManagedBy.SPRING, PersistenceStack.NONE);
    }
    public static CausewayBeanMetaData springNotContributing(
            final @NonNull LogicalType logicalType) {
        return new CausewayBeanMetaData(logicalType, BeanSort.MANAGED_BEAN_NOT_CONTRIBUTING, DiscoveredBy.SPRING, ManagedBy.SPRING, PersistenceStack.NONE);
    }
    public static CausewayBeanMetaData unspecified(
            final @NonNull LogicalType logicalType,
            final @NonNull DiscoveredBy discoveredBy,
            final @NonNull BeanSort beanSort) {
        return new CausewayBeanMetaData(logicalType, beanSort, discoveredBy, ManagedBy.UNSPECIFIED, PersistenceStack.NONE);
    }

    // -- FACTORIES

    @Deprecated
    public static CausewayBeanMetaData springManaged(
            final @NonNull DiscoveredBy discoveredBy,
            final @NonNull BeanSort beanSort,
            final @NonNull LogicalType logicalType) {
        return new CausewayBeanMetaData(logicalType, beanSort, discoveredBy, ManagedBy.SPRING, PersistenceStack.NONE);
    }

    /**
     * If discovered by Spring, let Spring decide whether it wants to manage this type. We do not interfere.
     */
    @Deprecated
    public static CausewayBeanMetaData unspecified(
            final @NonNull DiscoveredBy discoveredBy,
            final @NonNull BeanSort beanSort,
            final @NonNull LogicalType logicalType) {
        return new CausewayBeanMetaData(logicalType, beanSort, discoveredBy, ManagedBy.UNSPECIFIED, PersistenceStack.NONE);
    }

}
