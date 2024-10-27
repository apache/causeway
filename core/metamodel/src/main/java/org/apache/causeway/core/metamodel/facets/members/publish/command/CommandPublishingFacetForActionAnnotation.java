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

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.services.commanddto.processor.CommandDtoProcessor;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.metamodel.facets.ActionConfigOptions;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;

public abstract class CommandPublishingFacetForActionAnnotation extends CommandPublishingFacetAbstract {

    static class Enabled extends CommandPublishingFacetForActionAnnotation {
        Enabled(final CommandDtoProcessor processor, final FacetHolder holder, final ServiceInjector servicesInjector) {
            super(processor, holder, servicesInjector);
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    static class Disabled extends CommandPublishingFacetForActionAnnotation {
        Disabled(final CommandDtoProcessor processor, final FacetHolder holder, final ServiceInjector servicesInjector) {
            super(processor, holder, servicesInjector);
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    }

    public static Optional<CommandPublishingFacet> create(
            final Optional<Action> actionsIfAny,
            final CausewayConfiguration configuration,
            final ServiceInjector servicesInjector,
            final FacetHolder holder) {

        var publishingPolicy = ActionConfigOptions.actionCommandPublishingPolicy(configuration);

        return actionsIfAny
                .filter(action -> action.commandPublishing() != Publishing.NOT_SPECIFIED)
                .map(action -> {
                    Publishing publishing = action.commandPublishing();

                    final Class<? extends CommandDtoProcessor> processorClass = action.commandDtoProcessor();
                    final CommandDtoProcessor processor = newProcessorElseNull(processorClass);

                    if(processor != null) {
                        publishing = Publishing.ENABLED;
                    }

                    switch (publishing) {
                        case AS_CONFIGURED:
                            switch (publishingPolicy) {
                                case NONE:
                                    return new CommandPublishingFacetForActionAnnotationAsConfigured.None(holder, servicesInjector);
                                case IGNORE_QUERY_ONLY:
                                case IGNORE_SAFE:
                                    return hasSafeSemantics(holder)
                                            ? new CommandPublishingFacetForActionAnnotationAsConfigured.IgnoreSafe(holder, servicesInjector)
                                            : new CommandPublishingFacetForActionAnnotationAsConfigured.IgnoreSafeYetNot(holder, servicesInjector);
                                case ALL:
                                    return new CommandPublishingFacetForActionAnnotationAsConfigured.All(holder, servicesInjector);
                                default:
                                    throw new IllegalStateException(String.format("configured action.commandPublishing policy '%s' not recognised", publishingPolicy));
                            }
                        case DISABLED:
                            return new CommandPublishingFacetForActionAnnotation.Disabled(processor, holder, servicesInjector);
                        case ENABLED:
                            return new CommandPublishingFacetForActionAnnotation.Enabled(processor, holder, servicesInjector);
                        default:
                            throw new IllegalStateException(String.format("@Action#commandPublishing '%s' not recognised", publishing));
                    }
                });
    }

    static boolean hasSafeSemantics(final FacetHolder holder) {
        var actionSemanticsFacet = holder.getFacet(ActionSemanticsFacet.class);
        return actionSemanticsFacet != null && actionSemanticsFacet.value().isSafeInNature();
    }

    CommandPublishingFacetForActionAnnotation(
            final CommandDtoProcessor processor,
            final FacetHolder holder,
            final ServiceInjector servicesInjector) {
        super(processor, holder, servicesInjector);
    }

}
