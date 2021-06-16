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

import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.services.commanddto.processor.CommandDtoProcessor;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.internal.base._Optionals;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.metamodel.facets.PublishingPolicies;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

import lombok.val;

public class CommandPublishingFacetForPropertyAnnotation extends CommandPublishingFacetAbstract {

    public static Optional<CommandPublishingFacet> create(
            final Optional<Property> propertyIfAny,
            final IsisConfiguration configuration,
            final FacetHolder holder,
            final ServiceInjector servicesInjector) {

        val publishingPolicy = PublishingPolicies.propertyCommandPublishingPolicy(configuration);

        return _Optionals.orNullable(

            propertyIfAny
            .filter(property -> property.commandPublishing() != Publishing.NOT_SPECIFIED)
            .map(property -> {
                Publishing commandReification = property.commandPublishing();

                final Class<? extends CommandDtoProcessor> processorClass =
                        property.commandDtoProcessor();
                final CommandDtoProcessor processor = newProcessorElseNull(processorClass);

                if(processor != null) {
                    commandReification = Publishing.ENABLED;
                }
                switch (commandReification) {
                case AS_CONFIGURED:
                    switch (publishingPolicy) {
                    case NONE:
                        return null;
                    default:
                        return (CommandPublishingFacet)new CommandPublishingFacetForPropertyAnnotationAsConfigured(holder, servicesInjector);
                    }
                case DISABLED:
                    return null;
                case ENABLED:
                    return new CommandPublishingFacetForPropertyAnnotation(holder, processor, servicesInjector);
                default:
                }
                throw new IllegalStateException("command '" + commandReification + "' not recognised");
            })

            ,

            () -> {
                switch (publishingPolicy) {
                case NONE:
                    return null;
                default:
                    return CommandPublishingFacetFromConfiguration.create(holder, servicesInjector);
                }
            }

        );
    }


    CommandPublishingFacetForPropertyAnnotation(
            final FacetHolder holder,
            final CommandDtoProcessor processor,
            final ServiceInjector servicesInjector) {
        super(processor, holder, servicesInjector);
    }


}
