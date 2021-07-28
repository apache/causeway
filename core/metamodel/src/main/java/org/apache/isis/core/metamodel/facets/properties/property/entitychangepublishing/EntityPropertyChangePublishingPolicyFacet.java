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

package org.apache.isis.core.metamodel.facets.properties.property.entitychangepublishing;

import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.NonNull;

/**
 * Indicates whether a property should be excluded from entity change publishing (auditing).
 * @since 2.0
 */
public interface EntityPropertyChangePublishingPolicyFacet extends Facet {

    /**
     * Must be one of Publishing.ENABLED or Publishing.DISABLED.
     */
    @NonNull Publishing getEntityChangePublishing();

    default boolean isPublishingVetoed() {
        return getEntityChangePublishing() == Publishing.DISABLED;
    }

    default boolean isPublishingAllowed() {
        return getEntityChangePublishing() == Publishing.ENABLED;
    }

    static boolean isExcludedFromPublishing(final @NonNull OneToOneAssociation property) {
        return property
                .lookupFacet(EntityPropertyChangePublishingPolicyFacet.class)
                .map(EntityPropertyChangePublishingPolicyFacet::isPublishingVetoed)
                .orElse(false);
    }

}
