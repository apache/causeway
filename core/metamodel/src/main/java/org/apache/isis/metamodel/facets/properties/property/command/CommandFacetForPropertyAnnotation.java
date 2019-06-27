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
package org.apache.isis.metamodel.facets.properties.property.command;

import java.util.List;

import org.apache.isis.applib.annotation.CommandExecuteIn;
import org.apache.isis.applib.annotation.CommandPersistence;
import org.apache.isis.applib.annotation.CommandReification;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.services.command.CommandDtoProcessor;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.actions.action.command.CommandFacetFromConfiguration;
import org.apache.isis.metamodel.facets.actions.command.CommandFacet;
import org.apache.isis.metamodel.facets.actions.command.CommandFacetAbstract;

public class CommandFacetForPropertyAnnotation extends CommandFacetAbstract {

    public static CommandFacet create(
            final List<Property> properties,
            final IsisConfiguration configuration,
            final FacetHolder holder,
            final ServiceInjector servicesInjector) {

        final CommandPropertiesConfiguration setting = CommandPropertiesConfiguration.parse(configuration);

        return properties.stream()
                .filter(property -> property.command() != CommandReification.NOT_SPECIFIED)
                .findFirst()
                .map(property -> {
                    CommandReification command = property.command();
                    final CommandPersistence commandPersistence = property.commandPersistence();
                    final CommandExecuteIn commandExecuteIn = property.commandExecuteIn();

                    final Class<? extends CommandDtoProcessor> processorClass =
                            property != null ? property.commandDtoProcessor() : null;
                            final CommandDtoProcessor processor = newProcessorElseNull(processorClass);

                            if(processor != null) {
                                command = CommandReification.ENABLED;
                            }
                            switch (command) {
                            case AS_CONFIGURED:
                                switch (setting) {
                                case NONE:
                                    return null;
                                default:
                                    return (CommandFacet)new CommandFacetForPropertyAnnotationAsConfigured(commandPersistence,
                                            commandExecuteIn, Enablement.ENABLED, holder, servicesInjector);
                                }
                            case DISABLED:
                                return null;
                            case ENABLED:
                                return new CommandFacetForPropertyAnnotation(commandPersistence, commandExecuteIn, Enablement.ENABLED, holder, processor, servicesInjector);
                            default:
                            }
                            throw new IllegalStateException("command '" + command + "' not recognised");
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
            final CommandPersistence persistence,
            final CommandExecuteIn executeIn,
            final Enablement enablement,
            final FacetHolder holder,
            final CommandDtoProcessor processor,
            final ServiceInjector servicesInjector) {
        super(persistence, executeIn, enablement, processor, holder, servicesInjector);
    }


}
