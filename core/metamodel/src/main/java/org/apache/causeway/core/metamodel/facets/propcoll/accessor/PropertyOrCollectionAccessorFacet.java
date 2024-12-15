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
package org.apache.causeway.core.metamodel.facets.propcoll.accessor;

import org.springframework.lang.Nullable;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

/**
 * Provides access to an association, typically via a getter.
 */
public interface PropertyOrCollectionAccessorFacet extends Facet {

    /**
     * Returns the (nullable) pojo value of this property or collection from given {@link ManagedObject}.
     * <p>
     * If enabled via {@link CausewayConfiguration causeway.core.meta-model.filter-visibility},
     * object(s) not visible to the current user will be excluded from the result.
     * <p>
     * That is, for a property {@code null} will be returned,
     * while for a collection, objects not visible, will be omitted from that collection.
     */
    @Nullable
    Object getAssociationValueAsPojo(
            ManagedObject inObject,
            InteractionInitiatedBy interactionInitiatedBy);

    ObjectSpecification getDeclaringType();
}
