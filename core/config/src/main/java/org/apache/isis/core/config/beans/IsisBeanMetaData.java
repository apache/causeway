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

import org.apache.isis.applib.id.LogicalType;
import org.apache.isis.applib.services.metamodel.BeanSort;

import lombok.NonNull;
import lombok.Value;

@Value(staticConstructor = "of")
public class IsisBeanMetaData {

    public enum ManagedBy {
        UNSPECIFIED,
        SPRING,
        ISIS;
        public boolean isUnspecified() {return this == ManagedBy.UNSPECIFIED; }
        public boolean isSpring() {return this == ManagedBy.SPRING; }
        public boolean isIsis() {return this == ManagedBy.ISIS; }
        /**
         * Whether Spring should make that underlying bean injectable.
         * @implNote if not managed by Isis, let ultimately Spring decide
         */
        public boolean isInjectable() {
            return !isIsis();
        }
    }

    private final @NonNull BeanSort beanSort;
    private final @NonNull LogicalType logicalType;
    private @NonNull ManagedBy managedBy;

    public Class<?> getCorrespondingClass() {
        return logicalType.getCorrespondingClass();
    }

    public String getBeanName() {
        return logicalType.getLogicalTypeName();
    }

    // -- FACTORIES

    public static IsisBeanMetaData injectable(
            final @NonNull BeanSort beanSort,
            final @NonNull LogicalType logicalType) {
        return of(beanSort, logicalType, ManagedBy.SPRING);
    }

    /**
     * Let <i>Spring</i> decide.
     */
    public static IsisBeanMetaData indifferent(
            final @NonNull BeanSort beanSort,
            final @NonNull Class<?> type) {
        return of(beanSort, LogicalType.infer(type), ManagedBy.UNSPECIFIED);
    }

    public static IsisBeanMetaData isisManaged(
            final @NonNull BeanSort beanSort,
            final @NonNull LogicalType logicalType) {
        return of(beanSort, logicalType, ManagedBy.ISIS);
    }

    public static IsisBeanMetaData isisManaged(
            final @NonNull BeanSort beanSort,
            final @NonNull Class<?> type) {
        return isisManaged(beanSort, LogicalType.infer(type));
    }

}
