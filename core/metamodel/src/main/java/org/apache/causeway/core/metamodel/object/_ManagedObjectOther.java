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

import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * (package private) specialization corresponding to {@link Specialization#OTHER}
 * @see ManagedObject.Specialization#OTHER
 */
final class _ManagedObjectOther
extends _ManagedObjectSpecified
implements Bookmarkable.NoBookmark {

    @Getter(onMethod_ = {@Override}) @Accessors(makeFinal = true)
    private final @NonNull Object pojo;

    _ManagedObjectOther(
            final ObjectSpecification spec,
            final Object pojo) {
        super(ManagedObject.Specialization.OTHER, spec);
        _Assert.assertTrue(!spec.isValue());
        _Assert.assertTrue(!spec.isEntityOrViewModel());
        this.pojo = assertCompliance(pojo);
    }

    @Override
    public String getTitle() {
        return "other object, not recognized by the framework's meta-model";
    }

}