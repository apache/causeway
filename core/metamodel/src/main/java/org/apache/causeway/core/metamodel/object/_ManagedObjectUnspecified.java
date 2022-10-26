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

import java.util.Objects;
import java.util.function.Supplier;

import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;

/**
 * (package private) specialization corresponding to {@link Specialization#UNSPECIFIED}
 * @see ManagedObject.Specialization#UNSPECIFIED
 */
final class _ManagedObjectUnspecified
implements ManagedObject, Bookmarkable.NoBookmark {

    static final ManagedObject INSTANCE = new _ManagedObjectUnspecified();

    @Override
    public Specialization getSpecialization() {
        return Specialization.UNSPECIFIED;
    }

    @Override
    public ObjectSpecification getSpecification() {
        throw _Exceptions.unsupportedOperation();
    }

    @Override
    public Object getPojo() {
        return null;
    }

    @Override
    public MetaModelContext getMetaModelContext() {
        throw _Exceptions
                .illegalArgument("Can only retrieve MetaModelContext from ManagedObjects "
                        + "that have an ObjectSpecification.");
    }

    @Override
    public Supplier<ManagedObject> asSupplier() {
        return ()->this;
    }

    @Override
    public <T> T assertCompliance(final @NonNull T pojo) {
        return pojo; // no-op
    }

    @Override
    public String getTitle() {
        return "unspecified object";
    }

    @Override
    public boolean equals(final Object other) {
        return Objects.equals(this, other);
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public final String toString() {
        return "ManagedObject(UNSPECIFIED)";
    }

}
