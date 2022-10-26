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
package org.apache.causeway.core.metamodel.objectmanager.memento;

import org.apache.causeway.applib.services.bookmark.Oid;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facets.object.value.ValueFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;

import lombok.RequiredArgsConstructor;

interface _Recreatable {

    @RequiredArgsConstructor
    enum RecreateStrategy implements _Recreatable {
        /**
         * The {@link ManagedObject} that this is the memento for, directly has
         * an {@link ValueFacet} (it is almost certainly a value), and so is
         * stored directly.
         */
        VALUE(new _RecreatableValue()),
        /**
         * The {@link ManagedObject} that this is for, is already known by its
         * (persistent) {@link Oid}.
         */
        LOOKUP(new _RecreatableLookup());

        private final _Recreatable delegate;

        @Override
        public ManagedObject recreateObject(final ObjectMementoForScalar memento, final MetaModelContext mmc) {
            return delegate.recreateObject(memento, mmc);
        }

        @Override
        public boolean equals(final ObjectMementoForScalar memento, final ObjectMementoForScalar otherMemento) {
            return delegate.equals(memento, otherMemento);
        }

        @Override
        public int hashCode(final ObjectMementoForScalar memento) {
            return delegate.hashCode();
        }

    }

    ManagedObject recreateObject(ObjectMementoForScalar memento, MetaModelContext mmc);

    boolean equals(ObjectMementoForScalar memento, ObjectMementoForScalar otherMemento);

    int hashCode(ObjectMementoForScalar memento);

}