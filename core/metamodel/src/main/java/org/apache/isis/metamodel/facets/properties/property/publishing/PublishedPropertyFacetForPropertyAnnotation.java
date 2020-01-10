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

package org.apache.isis.metamodel.facets.properties.property.publishing;

import java.util.Optional;

import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.metamodel.facets.PublishPropertiesConfiguration;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.properties.publish.PublishedPropertyFacet;
import org.apache.isis.metamodel.facets.properties.publish.PublishedPropertyFacetAbstract;

public class PublishedPropertyFacetForPropertyAnnotation extends PublishedPropertyFacetAbstract {

    public static PublishedPropertyFacet create(
            final Optional<Property> propertyIfAny,
            final IsisConfiguration configuration,
            final FacetHolder holder) {

        final PublishPropertiesConfiguration setting = configuration.getServices().getPublish().getProperties();

        return propertyIfAny
                .map(Property::publishing)
                .filter(publishing -> publishing != Publishing.NOT_SPECIFIED)
                .map(publishing -> {

                    switch (publishing) {
                    case AS_CONFIGURED:
                        switch (setting) {
                        case NONE:
                            return null;
                        default:
                            return (PublishedPropertyFacet)
                                    new PublishedPropertyFacetForPropertyAnnotationAsConfigured(holder);
                        }
                    case DISABLED:
                        return null;
                    case ENABLED:
                        return new PublishedPropertyFacetForPropertyAnnotation(holder);
                    default:
                    }
                    return null;

                })
                .orElseGet(() -> {
                    switch (setting) {
                    case NONE:
                        return null;
                    default:
                        return new PublishedPropertyFacetFromConfiguration(holder);
                    }
                });

    }

    public PublishedPropertyFacetForPropertyAnnotation(final FacetHolder holder) {
        super(holder);
    }

}
