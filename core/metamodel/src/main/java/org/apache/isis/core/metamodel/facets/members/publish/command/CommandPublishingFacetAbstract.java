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

import java.util.function.BiConsumer;

import org.apache.isis.applib.services.commanddto.processor.CommandDtoProcessor;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

public abstract class CommandPublishingFacetAbstract
extends FacetAbstract
implements CommandPublishingFacet {

    private static final Class<? extends Facet> type() {
        return CommandPublishingFacet.class;
    }

    private final CommandDtoProcessor processor;

    public CommandPublishingFacetAbstract(
            final CommandDtoProcessor processor,
            final FacetHolder holder,
            final ServiceInjector servicesInjector) {
        super(type(), holder);
        inject(processor, servicesInjector);
        this.processor = processor;
    }

    private static void inject(
            final CommandDtoProcessor processor, final ServiceInjector servicesInjector) {
        if(processor == null || servicesInjector == null) {
            return;
        }
        servicesInjector.injectServicesInto(processor);
    }

    @Override
    public CommandDtoProcessor getProcessor() {
        return processor;
    }

    /**
     * For benefit of subclasses.
     */
    protected static CommandDtoProcessor newProcessorElseNull(final Class<?> cls) {
        if(cls == null) {
            return null;
        }
        if(cls == CommandDtoProcessor.class) {
            // ie the default value, namely the interface
            return null;
        }
        if (!(CommandDtoProcessor.class.isAssignableFrom(cls))) {
            return null;
        }
        try {
            return (CommandDtoProcessor) cls.newInstance();
        } catch (final InstantiationException | IllegalAccessException e) {
            return null;
        }
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("dtoProcessor", processor);
    }
}
