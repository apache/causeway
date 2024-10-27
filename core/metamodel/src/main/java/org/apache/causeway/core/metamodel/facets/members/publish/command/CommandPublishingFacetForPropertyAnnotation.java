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
package org.apache.causeway.core.metamodel.facets.members.publish.command;

import java.util.Optional;

import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.services.commanddto.processor.CommandDtoProcessor;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.metamodel.facets.ActionConfigOptions;
import org.apache.causeway.core.config.metamodel.facets.PropertyConfigOptions;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.TypedHolder;
import org.apache.causeway.core.metamodel.facets.actions.contributing.ContributingFacet;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacet;

public abstract class CommandPublishingFacetForPropertyAnnotation extends CommandPublishingFacetAbstract {

    static class Enabled extends CommandPublishingFacetForPropertyAnnotation {
        Enabled(CommandDtoProcessor processor, FacetHolder holder, ServiceInjector servicesInjector) {
            super(processor, holder, servicesInjector);
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    static class Disabled extends CommandPublishingFacetForPropertyAnnotation {
        Disabled(CommandDtoProcessor processor, FacetHolder holder, ServiceInjector servicesInjector) {
            super(processor, holder, servicesInjector);
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    }

    public static CommandPublishingFacet create(
            final Optional<Property> propertyIfAny,
            final CausewayConfiguration configuration,
            final FacetHolder holder,
            final ServiceInjector servicesInjector) {

        var publishingPolicy = PropertyConfigOptions.propertyCommandPublishingPolicy(configuration);

        return propertyIfAny
            .filter(property -> property.commandPublishing() != Publishing.NOT_SPECIFIED)
            .map(property -> {
                Publishing publishing = property.commandPublishing();

                var processorClass = property.commandDtoProcessor();
                var processor = newProcessorElseNull(processorClass);

                if(processor != null) {
                    publishing = Publishing.ENABLED;
                }

                switch (publishing) {
                    case AS_CONFIGURED:
                        switch (publishingPolicy) {
                            case NONE:
                                return (CommandPublishingFacet)new CommandPublishingFacetForPropertyAnnotationAsConfigured.None(holder, servicesInjector);
                            case ALL:
                                return new CommandPublishingFacetForPropertyAnnotationAsConfigured.All(holder, servicesInjector);
                            default:
                                throw new IllegalStateException(String.format("configured property.commandpublishing policy '%s' not recognised", publishingPolicy));
                        }
                    case DISABLED:
                        return new CommandPublishingFacetForPropertyAnnotation.Disabled(processor, holder, servicesInjector);
                    case ENABLED:
                        return new CommandPublishingFacetForPropertyAnnotation.Enabled(processor, holder, servicesInjector);
                    default:
                        throw new IllegalStateException(String.format("@Property#commandPublishing '%s' not recognised", publishing));
                }
            })
            .orElseGet(() -> {
                // there is no publishing facet from either @Action or @Property, so use the appropriate configuration to install a default
                if (representsProperty(holder)) {
                    // we are dealing with a property
                    switch (publishingPolicy) {
                        case NONE:
                            return new CommandPublishingFacetForPropertyFromConfiguration.None(holder, servicesInjector);
                        case ALL:
                            return new CommandPublishingFacetForPropertyFromConfiguration.All(holder, servicesInjector);
                        default:
                            throw new IllegalStateException(String.format("configured property.commandPublishing policy '%s' not recognised", publishingPolicy));
                    }
                } else {
                    // we are dealing with an action
                    var actionPublishingPolicy = ActionConfigOptions.actionCommandPublishingPolicy(configuration);
                    switch (actionPublishingPolicy) {
                        case NONE:
                            return new CommandPublishingFacetForActionFromConfiguration.None(holder, servicesInjector);
                        case IGNORE_QUERY_ONLY:
                        case IGNORE_SAFE:
                            return CommandPublishingFacetForActionAnnotation.hasSafeSemantics(holder)
                                    ? new CommandPublishingFacetForActionFromConfiguration.IgnoreSafe(holder, servicesInjector)
                                    : new CommandPublishingFacetForActionFromConfiguration.IgnoreSafeYetNot(holder, servicesInjector);
                        case ALL:
                            return new CommandPublishingFacetForActionFromConfiguration.All(holder, servicesInjector);
                        default:
                            throw new IllegalStateException(String.format("configured action.commandPublishing policy '%s' not recognised", actionPublishingPolicy));
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

    CommandPublishingFacetForPropertyAnnotation(
            final CommandDtoProcessor processor,
            final FacetHolder holder,
            final ServiceInjector servicesInjector) {
        super(processor, holder, servicesInjector);
    }

}
