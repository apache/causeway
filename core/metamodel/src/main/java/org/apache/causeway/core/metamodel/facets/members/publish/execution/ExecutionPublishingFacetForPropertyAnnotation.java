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
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.metamodel.facets.ActionConfigOptions;
import org.apache.causeway.core.config.metamodel.facets.PropertyConfigOptions;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.TypedHolder;
import org.apache.causeway.core.metamodel.facets.actions.contributing.ContributingFacet;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacet;

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

    public static ExecutionPublishingFacet create(
            final Optional<Property> propertyIfAny,
            final CausewayConfiguration configuration,
            final FacetHolder holder) {

        var publishingPolicy = PropertyConfigOptions.propertyExecutionPublishingPolicy(configuration);

        return propertyIfAny
            .map(Property::executionPublishing)
            .filter(publishing -> publishing != Publishing.NOT_SPECIFIED)
            .map(publishing -> {

                switch (publishing) {
                    case AS_CONFIGURED:
                        switch (publishingPolicy) {
                            case NONE:
                                return (ExecutionPublishingFacet)new ExecutionPublishingFacetForPropertyAnnotationAsConfigured.None(holder);
                            case ALL:
                                return new ExecutionPublishingFacetForPropertyAnnotationAsConfigured.All(holder);
                            default:
                                throw new IllegalStateException(String.format("configured property.executionPublishing policy '%s' not recognised", publishingPolicy));
                        }
                    case DISABLED:
                        return new ExecutionPublishingFacetForPropertyAnnotation.Disabled(holder);
                    case ENABLED:
                        return new ExecutionPublishingFacetForPropertyAnnotation.Enabled(holder);
                    default:
                        throw new IllegalStateException(String.format("@Property#executionPublishing '%s' not recognised", publishing));
                }
            })
            .orElseGet(() -> {
                // there is no publishing facet from either @Action or @Property, so use the appropriate configuration to install a default
                if (representsProperty(holder)) {
                    // we are dealing with a property
                    switch (publishingPolicy) {
                        case NONE:
                            return new ExecutionPublishingFacetForPropertyFromConfiguration.None(holder);
                        case ALL:
                            return new ExecutionPublishingFacetForPropertyFromConfiguration.All(holder);
                        default:
                            throw new IllegalStateException(String.format("configured property.executionPublishing policy '%s' not recognised", publishingPolicy));
                    }
                } else {
                    // we are dealing with an action
                    var actionPublishingPolicy = ActionConfigOptions.actionExecutionPublishingPolicy(configuration);
                    switch (actionPublishingPolicy) {
                        case NONE:
                            return new ExecutionPublishingFacetForActionFromConfiguration.None(holder);
                        case IGNORE_QUERY_ONLY:
                        case IGNORE_SAFE:
                            return ExecutionPublishingFacetForActionAnnotation.hasSafeSemantics(holder)
                                    ? new ExecutionPublishingFacetForActionFromConfiguration.IgnoreSafe(holder)
                                    : new ExecutionPublishingFacetForActionFromConfiguration.IgnoreSafeYetNot(holder);
                        case ALL:
                            return new ExecutionPublishingFacetForActionFromConfiguration.All(holder);
                        default:
                            throw new IllegalStateException(String.format("configured action.executionPublishing policy '%s' not recognised", actionPublishingPolicy));
                    }
                }
            });
    }

    private static boolean representsProperty(FacetHolder holder) {
        // a property
        if (holder instanceof TypedHolder && ((TypedHolder)holder).getFeatureType() == FeatureType.PROPERTY) {
            return true;
        }
        // or a mixin
        return  holder.containsFacet(ContributingFacet.class) &&
                holder.getFacet(ContributingFacet.class).contributed() == MixinFacet.Contributing.AS_PROPERTY;
    }

    public ExecutionPublishingFacetForPropertyAnnotation(final FacetHolder holder) {
        super(holder);
    }

}
