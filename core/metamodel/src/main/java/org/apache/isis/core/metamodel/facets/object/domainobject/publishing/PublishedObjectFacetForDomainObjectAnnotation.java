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

package org.apache.isis.core.metamodel.facets.object.domainobject.publishing;

import java.util.Optional;

import org.apache.isis.applib.annotation.ExecutionDispatch;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.metamodel.facets.PublishObjectsConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.publishedobject.PublishedObjectFacet;
import org.apache.isis.core.metamodel.facets.object.publishedobject.PublishedObjectFacetAbstract;

public class PublishedObjectFacetForDomainObjectAnnotation extends PublishedObjectFacetAbstract {

    public static PublishedObjectFacet create(
            final Optional<ExecutionDispatch> publishingIfAny,
            final IsisConfiguration configuration,
            final FacetHolder holder) {

        final PublishObjectsConfiguration setting = configuration.getApplib().getAnnotation().getDomainObject().getPublishing();
        
        return publishingIfAny.map(publishing -> {
            switch (publishing) {
            case NOT_SPECIFIED:
            case AS_CONFIGURED:
                return setting == PublishObjectsConfiguration.NONE
                ? null
                        : (PublishedObjectFacet)new PublishedObjectFacetForDomainObjectAnnotationAsConfigured(holder)
                        ;
            case DISABLED:
                return null;
            case ENABLED:
                return new PublishedObjectFacetForDomainObjectAnnotation(holder);
            }
            throw new IllegalStateException("domainObject.publishing() not recognised, is " + publishing);

        })
        .orElseGet(() -> setting == PublishObjectsConfiguration.NONE
                ? null
                        : new PublishedObjectFacetFromConfiguration(holder));
        

    }

    PublishedObjectFacetForDomainObjectAnnotation(final FacetHolder holder) {
        super(holder);
    }

}
