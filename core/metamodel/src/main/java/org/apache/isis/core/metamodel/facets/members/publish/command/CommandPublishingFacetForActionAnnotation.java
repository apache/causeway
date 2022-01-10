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
package org.apache.isis.core.metamodel.facets.members.publish.command;

import java.util.Optional;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.services.commanddto.processor.CommandDtoProcessor;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.internal.base._Optionals;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.metamodel.facets.PublishingPolicies;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;

import lombok.val;

public class CommandPublishingFacetForActionAnnotation extends CommandPublishingFacetAbstract {

    public static Optional<CommandPublishingFacet> create(
            final Optional<Action> actionsIfAny,
            final IsisConfiguration configuration,
            final ServiceInjector servicesInjector,
            final FacetHolder holder) {

        val publishingPolicy = PublishingPolicies.actionCommandPublishingPolicy(configuration);

        return _Optionals.orNullable(

        actionsIfAny
        .filter(action -> action.commandPublishing() != Publishing.NOT_SPECIFIED)
        .<CommandPublishingFacet>map(action -> {

            Publishing commandPublishing = action.commandPublishing();
            final Class<? extends CommandDtoProcessor> processorClass = action.commandDtoProcessor();
            final CommandDtoProcessor processor = newProcessorElseNull(processorClass);

            if(processor != null) {
                commandPublishing = Publishing.ENABLED;
            }

            switch (commandPublishing) {
            case AS_CONFIGURED:
                switch (publishingPolicy) {
                case NONE:
                    return null;
                case IGNORE_QUERY_ONLY:
                case IGNORE_SAFE:
                    if (hasSafeSemantics(holder)) {
                        return null;
                    }
                    // else fall through
                default:
                    return new CommandPublishingFacetForActionAnnotationAsConfigured(
                            holder, servicesInjector);
                }
            case DISABLED:
                return null;
            case ENABLED:
                return new CommandPublishingFacetForActionAnnotation(
                        processor, holder, servicesInjector);
            default:
            }
            throw new IllegalStateException("command '" + commandPublishing + "' not recognised");
        })

        ,

        () -> {
            switch (publishingPolicy) {
            case NONE:
                return null;
            case IGNORE_QUERY_ONLY:
            case IGNORE_SAFE:
                if (hasSafeSemantics(holder)) {
                    return null;
                }
                // else fall through
            default:
                return CommandPublishingFacetFromConfiguration.create(holder, servicesInjector);
            }
        }

        );
    }

    private static boolean hasSafeSemantics(final FacetHolder holder) {
        final ActionSemanticsFacet actionSemanticsFacet = holder.getFacet(ActionSemanticsFacet.class);
        if(actionSemanticsFacet == null) {
            throw new IllegalStateException("Require ActionSemanticsFacet in order to process");
        }
        if(actionSemanticsFacet.value().isSafeInNature()) {
            return true;
        }
        return false;
    }

    CommandPublishingFacetForActionAnnotation(
            final CommandDtoProcessor processor,
            final FacetHolder holder,
            final ServiceInjector servicesInjector) {
        super(processor, holder, servicesInjector);
    }


}
