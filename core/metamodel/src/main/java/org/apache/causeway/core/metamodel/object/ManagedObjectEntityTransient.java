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
package org.apache.causeway.core.metamodel.object;

import java.util.Optional;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.services.repository.EntityState;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

/**
 * (package private) specialization corresponding to a detached {@link Specialization#ENTITY}
 * @see ManagedObject.Specialization#ENTITY
 */
record ManagedObjectEntityTransient(
    @NonNull ObjectSpecification objSpec,
    @NonNull Object pojo)
implements ManagedObject, Bookmarkable.NoBookmark, _Refetchable {

    ManagedObjectEntityTransient(
            final ObjectSpecification objSpec,
            final Object pojo) {
        _Assert.assertTrue(objSpec.isEntity());
        this.objSpec = objSpec;
        this.pojo = _Compliance.assertCompliance(objSpec, specialization(), pojo);
    }

    @Override
    public Specialization specialization() {
        return ManagedObject.Specialization.ENTITY;
    }

    @Override
    public String getTitle() {
        return "transient entity object";
    }

    @Override
    public Object getPojo() {
        return pojo;
    }

    @Override
    public Object peekAtPojo() {
        return pojo;
    }

    @Override
    public @NonNull EntityState getEntityState() {
        return objSpec().entityFacetElseFail().getEntityState(pojo);
    }

    @Override
    public Optional<ObjectMemento> getMemento() {
        return Optional.ofNullable(ObjectMemento.singularOrEmpty(this));
    }

    @Override
    public boolean equals(final Object obj) {
        return _Compliance.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return _Compliance.hashCode(this);
    }

    @Override
    public String toString() {
        return _Compliance.toString(this);
    }

}