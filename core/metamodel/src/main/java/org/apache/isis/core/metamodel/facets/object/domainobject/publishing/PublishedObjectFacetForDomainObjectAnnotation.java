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

import java.util.List;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.publishedobject.PublishedObjectFacet;
import org.apache.isis.core.metamodel.facets.object.publishedobject.PublishedObjectFacetAbstract;

public class PublishedObjectFacetForDomainObjectAnnotation extends PublishedObjectFacetAbstract {

    public static PublishedObjectFacet create(
            final List<DomainObject> domainObjects,
            final IsisConfiguration configuration,
            final FacetHolder holder) {

        final Publishing publishing = domainObjects != null ? domainObjects.publishing() : Publishing.AS_CONFIGURED;

        switch (publishing) {
            case AS_CONFIGURED:

                final PublishObjectsConfiguration setting = PublishObjectsConfiguration.parse(configuration);
                switch (setting) {
                    case NONE:
                        return null;
                    default:
                        return domainObjects != null
                            ? new PublishedObjectFacetForDomainObjectAnnotationAsConfigured(holder)
                            : new PublishedObjectFacetFromConfiguration(holder);
                }
            case DISABLED:
                return null;
            case ENABLED:
                return new PublishedObjectFacetForDomainObjectAnnotation(holder);
        }
        return null;
    }

    PublishedObjectFacetForDomainObjectAnnotation(final FacetHolder holder) {
        super(holder);
    }

}
