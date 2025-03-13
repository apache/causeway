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

import lombok.experimental.UtilityClass;

@UtilityClass
class _Compliance {

    <T> T assertCompliance(
        final ObjectSpecification objSpec,
        final ManagedObject.Specialization specialization,
        final @NonNull T pojo) {

        MmAssertionUtils.assertPojoNotWrapped(pojo);
        if(objSpec.isAbstract()) {
            _Assert.assertFalse(specialization.getTypePolicy().isExactTypeRequired(),
                    ()->String.format("Specialization %s does not allow abstract type %s",
                            specialization,
                            objSpec));
        }
        if(specialization.getTypePolicy().isExactTypeRequired()) {
            MmAssertionUtils.assertExactType(objSpec, pojo);
        }
        if(specialization.getInjectionPolicy().isAlwaysInject()) {
            var isInjectionPointsResolved = objSpec.entityFacet()
                .map(entityFacet->entityFacet.isInjectionPointsResolved(pojo))
                .orElse(false);
            if(!isInjectionPointsResolved) {
                objSpec.getServiceInjector().injectServicesInto(pojo); // might be redundant
            }
        }
        return pojo;
    }

}
