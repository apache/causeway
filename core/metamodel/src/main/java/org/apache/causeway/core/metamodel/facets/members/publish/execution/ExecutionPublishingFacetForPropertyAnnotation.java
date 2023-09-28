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
package org.apache.causeway.core.metamodel.facets.members.publish.execution;

import java.util.Optional;

import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.commons.internal.base._Optionals;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.metamodel.facets.PropertyConfigOptions;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

import lombok.val;

public abstract class ExecutionPublishingFacetForPropertyAnnotation
extends ExecutionPublishingFacetAbstract {

    static class Enabled extends ExecutionPublishingFacetForPropertyAnnotation {
        Enabled(FacetHolder holder) {
            super(holder);
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    static class Disabled extends ExecutionPublishingFacetForPropertyAnnotation {
        Disabled(FacetHolder holder) {
            super(holder);
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    }

    public static Optional<ExecutionPublishingFacet> create(
            final Optional<Property> propertyIfAny,
            final CausewayConfiguration configuration,
            final FacetHolder holder) {

        val publishingPolicy = PropertyConfigOptions.propertyExecutionPublishingPolicy(configuration);

        return _Optionals.orNullable(

            propertyIfAny
            .map(Property::executionPublishing)
            .filter(publishing -> publishing != Publishing.NOT_SPECIFIED)
            .map(publishing -> {

                switch (publishing) {
                    case AS_CONFIGURED:
                        switch (publishingPolicy) {
                            case NONE:
                                return new ExecutionPublishingFacetForPropertyAnnotationAsConfigured.None(holder);
                            case ALL:
                                return new ExecutionPublishingFacetForPropertyAnnotationAsConfigured.All(holder);
                            default:
                                throw new IllegalStateException(String.format("configured publishingPolicy '%s' not recognised", publishingPolicy));
                        }
                    case DISABLED:
                        return new ExecutionPublishingFacetForPropertyAnnotation.Disabled(holder);
                    case ENABLED:
                        return new ExecutionPublishingFacetForPropertyAnnotation.Enabled(holder);
                    default:
                        throw new IllegalStateException(String.format("executionPublishing '%s' not recognised", publishing));
                }
            })
            ,
            // if not specified
            () -> {
                switch (publishingPolicy) {
                    case NONE:
                        return new ExecutionPublishingFacetForPropertyFromConfiguration.None(holder);
                    case ALL:
                        return new ExecutionPublishingFacetForPropertyFromConfiguration.All(holder);
                    default:
                        throw new IllegalStateException(String.format("configured publishingPolicy '%s' not recognised", publishingPolicy));
                }
            }
        );
    }

    public ExecutionPublishingFacetForPropertyAnnotation(final FacetHolder holder) {
        super(holder);
    }

}
