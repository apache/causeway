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
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
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
    public boolean impliesResolve() {
        return true;
    }

    @Override
    public boolean impliesObjectChanged() {
        return true;
    }

    @Override
    public void setProperty(final ObjectAdapter targetAdapter, final ObjectAdapter valueAdapter) {

        final CommandContext commandContext = getServicesInjector().lookupService(CommandContext.class);
        final Command command;

        if (commandContext != null) {
            command = commandContext.getCommand();

            // cf similar code in ActionInvocationFacetForDomainEventFacet
            command.setExecutor(Command.Executor.USER);

            command.setTarget(CommandUtil.bookmarkFor(targetAdapter));
            command.setTargetClass(CommandUtil.targetClassNameFor(targetAdapter));
            command.setTargetAction("(edit)");
            command.setMemberIdentifier("(edit)");

            command.setExecuteIn(org.apache.isis.applib.annotation.Command.ExecuteIn.FOREGROUND);
            command.setPersistence(org.apache.isis.applib.annotation.Command.Persistence.IF_HINTED);

            command.setStartedAt(Clock.getTimeAsJavaSqlTimestamp());
        }

        ObjectAdapter.InvokeUtils.invoke(method, targetAdapter, valueAdapter);
    }

    @Override
    protected String toStringValues() {
        return "method=" + method;
    }


    private ServicesInjector getServicesInjector() {
        return servicesInjector;
    }

}
