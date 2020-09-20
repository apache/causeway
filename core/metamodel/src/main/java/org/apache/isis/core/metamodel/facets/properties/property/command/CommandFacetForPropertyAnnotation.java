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
package org.apache.isis.core.metamodel.facets.properties.property.command;

import java.util.Optional;

import org.apache.isis.applib.annotation.CommandReification;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.services.commanddto.processor.CommandDtoProcessor;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.metamodel.facets.CommandPropertiesConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.actions.action.command.CommandFacetFromConfiguration;
import org.apache.isis.core.metamodel.facets.actions.command.CommandFacet;
import org.apache.isis.core.metamodel.facets.actions.command.CommandFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.audit.AuditableFacetAbstract;

import lombok.val;

public class CommandFacetForPropertyAnnotation extends CommandFacetAbstract {

    public static CommandFacet create(
            final Optional<Property> propertyIfAny,
            final IsisConfiguration configuration,
            final FacetHolder holder,
            final ServiceInjector servicesInjector) {

        final CommandPropertiesConfiguration setting = configuration.getApplib().getAnnotation().getProperty().getCommand();

        return propertyIfAny
                .filter(property -> property.command() != CommandReification.NOT_SPECIFIED)
                .map(property -> {
                    CommandReification commandReification = property.command();

                    final Class<? extends CommandDtoProcessor> processorClass =
                            property.commandDtoProcessor();
                    final CommandDtoProcessor processor = newProcessorElseNull(processorClass);

                    if(processor != null) {
                        commandReification = CommandReification.ENABLED;
                    }
                    switch (commandReification) {
                    case AS_CONFIGURED:
                        switch (setting) {
                        case NONE:
                            return null;
                        default:
                            return (CommandFacet)new CommandFacetForPropertyAnnotationAsConfigured(holder, servicesInjector);
                        }
                    case DISABLED:
                        return null;
                    case ENABLED:
                        return new CommandFacetForPropertyAnnotation(holder, processor, servicesInjector);
                    default:
                    }
                    throw new IllegalStateException("command '" + commandReification + "' not recognised");
                })
                .orElseGet(() -> {
                    switch (setting) {
                    case NONE:
                        return null;
                    default:
                        return CommandFacetFromConfiguration.create(holder, servicesInjector);
                    }
                });
    }


    CommandFacetForPropertyAnnotation(
            final FacetHolder holder,
            final CommandDtoProcessor processor,
            final ServiceInjector servicesInjector) {
        super(processor, holder, servicesInjector);
    }


}
