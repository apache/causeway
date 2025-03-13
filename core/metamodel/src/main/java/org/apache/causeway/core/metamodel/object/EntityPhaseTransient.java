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

import java.util.function.BiConsumer;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.services.repository.EntityState;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.core.metamodel.object.ManagedObjectEntity.PhaseState;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

record EntityPhaseTransient(
    @NonNull ObjectSpecification objSpec,
    @NonNull Object pojo)
implements EntityPhase {

    EntityPhaseTransient(
            final ObjectSpecification objSpec,
            final Object pojo) {
        _Assert.assertTrue(objSpec.isEntity());
        this.objSpec = objSpec;
        this.pojo = _Compliance.assertCompliance(objSpec, ManagedObject.Specialization.ENTITY, pojo);
    }

    @Override
    public PhaseState phaseState() {
        return PhaseState.TRANSIENT;
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
    public EntityState reassessEntityState(final BiConsumer<EntityState, PhaseState> onNewPhaseRequired) {
        var newEntityState = objSpec().entityFacetElseFail().getEntityState(pojo);
        phaseState().reassessPhase(newEntityState, onNewPhaseRequired);
        return newEntityState;
    }

}