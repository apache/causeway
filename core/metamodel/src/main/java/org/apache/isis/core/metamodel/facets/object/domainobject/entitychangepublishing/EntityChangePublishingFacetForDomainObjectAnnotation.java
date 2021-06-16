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
package org.apache.isis.core.metamodel.facets.object.domainobject.entitychangepublishing;


import java.util.Optional;

import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.metamodel.facets.PublishingPolicies;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.publish.entitychange.EntityChangePublishingFacet;
import org.apache.isis.core.metamodel.facets.object.publish.entitychange.EntityChangePublishingFacetAbstract;

import lombok.val;

public class EntityChangePublishingFacetForDomainObjectAnnotation
extends EntityChangePublishingFacetAbstract {

    public static Optional<EntityChangePublishingFacet> create(
            final Optional<Publishing> entityChangePublishingIfAny,
            final IsisConfiguration configuration,
            final FacetHolder holder) {

        val publish = entityChangePublishingIfAny.orElse(Publishing.AS_CONFIGURED);

        switch (publish) {
        case NOT_SPECIFIED:
        case AS_CONFIGURED:

            val publishingPolicy = PublishingPolicies.entityChangePublishingPolicy(configuration);
            switch (publishingPolicy) {
            case NONE:
                return Optional.empty();
            default:
                return Optional.of(entityChangePublishingIfAny.isPresent()
                        ? new EntityChangePublishingFacetForDomainObjectAnnotationAsConfigured(holder)
                        : new EntityChangePublishingFacetFromConfiguration(holder));
            }
        case DISABLED:
            return null;
        case ENABLED:
            return Optional.of(new EntityChangePublishingFacetForDomainObjectAnnotation(holder));

        default:
            throw _Exceptions.unmatchedCase(publish);
        }
    }

    protected EntityChangePublishingFacetForDomainObjectAnnotation(final FacetHolder holder) {
        super(holder);
    }
}

