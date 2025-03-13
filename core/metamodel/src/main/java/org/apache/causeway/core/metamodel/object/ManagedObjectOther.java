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

import org.jspecify.annotations.NonNull;

import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

/**
 * (package private) specialization corresponding to {@link Specialization#OTHER}
 * @see ManagedObject.Specialization#OTHER
 */
record ManagedObjectOther(
    @NonNull ObjectSpecification objSpec,
    @NonNull Object pojo)
implements ManagedObject, Bookmarkable.NoBookmark {

    ManagedObjectOther(
            final ObjectSpecification objSpec,
            final Object pojo) {
        this.objSpec = objSpec;
        _Assert.assertTrue(!objSpec.isValue());
        _Assert.assertTrue(!objSpec.isEntityOrViewModel());
        this.pojo = specialization().assertCompliance(objSpec, pojo);
    }

    @Override
    public String getTitle() {
        return "other object, not recognized by the framework";
    }

    @Override
    public Specialization specialization() {
        return ManagedObject.Specialization.OTHER;
    }

    @Override
    public Object getPojo() {
        return pojo;
    }

}