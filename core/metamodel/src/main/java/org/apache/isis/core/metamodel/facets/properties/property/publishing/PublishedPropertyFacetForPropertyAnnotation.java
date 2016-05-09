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

package org.apache.isis.core.metamodel.facets.properties.property.publishing;

import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.properties.publish.PublishedPropertyFacet;
import org.apache.isis.core.metamodel.facets.properties.publish.PublishedPropertyFacetAbstract;

public class PublishedPropertyFacetForPropertyAnnotation extends PublishedPropertyFacetAbstract {

    public static PublishedPropertyFacet create(
            final Property property,
            final IsisConfiguration configuration,
            final FacetHolder holder) {

        final Publishing publishing = property != null ? property.publishing() : Publishing.AS_CONFIGURED;

        switch (publishing) {
            case AS_CONFIGURED:
                final PublishPropertiesConfiguration setting = PublishPropertiesConfiguration.parse(configuration);
                switch (setting) {
                case NONE:
                    return null;
                default:
                    return property != null
                            ? new PublishedPropertyFacetForPropertyAnnotationAsConfigured(holder)
                            : new PublishedPropertyFacetFromConfiguration(holder);
                }
            case DISABLED:
                return null;
            case ENABLED:
                return new PublishedPropertyFacetForPropertyAnnotation(holder);
        }
        return null;
    }

    public PublishedPropertyFacetForPropertyAnnotation(final FacetHolder holder) {
        super(holder);
    }

}
