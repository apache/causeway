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

package org.apache.isis.core.metamodel.facets.properties.update.modify;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.ImperativeFacet;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;

public class PropertySetterFacetViaModifyMethod extends PropertySetterFacetAbstract implements ImperativeFacet {

    private final Method method;
    private final ServicesInjector servicesInjector;

    public PropertySetterFacetViaModifyMethod(final Method method, final FacetHolder holder, final ServicesInjector servicesInjector) {
        super(holder);
        this.method = method;
        this.servicesInjector = servicesInjector;
    }

    /**
     * Returns a singleton list of the {@link Method} provided in the
     * constructor.
     */
    @Override
    public List<Method> getMethods() {
        return Collections.singletonList(method);
    }

    @Override
    public Intent getIntent(final Method method) {
        return Intent.MODIFY_PROPERTY_SUPPORTING;
    }

    @Override
    public void setProperty(
            final ObjectAdapter targetAdapter,
            final ObjectAdapter valueAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final CommandContext commandContext = getCommandContext();
        final Command command = commandContext.getCommand();

        // cf similar code in ActionInvocationFacetForDomainEventFacet
        command.setExecutor(Command.Executor.USER);

        command.setTarget(CommandUtil.bookmarkFor(targetAdapter));
        command.setTargetClass(CommandUtil.targetClassNameFor(targetAdapter));
        command.setTargetAction(Command.ACTION_IDENTIFIER_FOR_EDIT);
        command.setMemberIdentifier(Command.ACTION_IDENTIFIER_FOR_EDIT);

        command.setExecuteIn(org.apache.isis.applib.annotation.Command.ExecuteIn.FOREGROUND);
        command.setPersistence(org.apache.isis.applib.annotation.Command.Persistence.IF_HINTED);

        command.setStartedAt(getClockService().nowAsJavaSqlTimestamp());

        ObjectAdapter.InvokeUtils.invoke(method, targetAdapter, valueAdapter);
    }

    @Override
    protected String toStringValues() {
        return "method=" + method;
    }


    private ServicesInjector getServicesInjector() {
        return servicesInjector;
    }

    private CommandContext getCommandContext() {
        return lookupService(CommandContext.class);
    }

    private ClockService getClockService() {
        return lookupService(ClockService.class);
    }

    private <T> T lookupService(final Class<T> serviceClass) {
        T service = lookupServiceIfAny(serviceClass);
        if(service == null) {
            throw new IllegalStateException("The '" + serviceClass.getName() + "' service is not registered!");
        }
        return service;
    }
    private <T> T lookupServiceIfAny(final Class<T> serviceClass) {
        return getServicesInjector().lookupService(serviceClass);
    }


}
