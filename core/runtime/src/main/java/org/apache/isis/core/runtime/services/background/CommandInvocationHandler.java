/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.runtime.services.background;

import static org.apache.isis.commons.internal.base._With.requires;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import org.apache.isis.applib.services.background.BackgroundCommandService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.CommandUtil;
import org.apache.isis.core.metamodel.services.command.CommandDtoServiceInternal;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionMixedIn;
import org.apache.isis.core.metamodel.specloader.specimpl.dflt.ObjectSpecificationDefault;
import org.apache.isis.schema.cmd.v1.CommandDto;

class CommandInvocationHandler<T> implements InvocationHandler {

    private final BackgroundCommandService backgroundCommandService;
    private final T target;
    private final Object mixedInIfAny;
    private final SpecificationLoader specificationLoader;
    private final CommandDtoServiceInternal commandDtoServiceInternal;
    private final CommandContext commandContext;
    private final Supplier<ObjectAdapterProvider> adapterProviderSupplier;

    CommandInvocationHandler(
            BackgroundCommandService backgroundCommandService,
            T target,
            Object mixedInIfAny,
            SpecificationLoader specificationLoader,
            CommandDtoServiceInternal commandDtoServiceInternal,
            CommandContext commandContext,
            Supplier<ObjectAdapterProvider> adapterProviderSupplier) {
        this.backgroundCommandService = requires(backgroundCommandService, "backgroundCommandService");
        this.target = requires(target, "target");
        this.mixedInIfAny = mixedInIfAny;
        this.specificationLoader = requires(specificationLoader, "specificationLoader");
        this.commandDtoServiceInternal = requires(commandDtoServiceInternal, "commandDtoServiceInternal");
        this.commandContext = requires(commandContext, "commandContext");
        this.adapterProviderSupplier = requires(adapterProviderSupplier, "adapterProviderSupplier");
    }

    @Override
    public Object invoke(
            final Object proxied,
            final Method proxyMethod,
            final Object[] args) throws Throwable {

        final boolean inheritedFromObject = proxyMethod.getDeclaringClass().equals(Object.class);
        if(inheritedFromObject) {
            return proxyMethod.invoke(target, args);
        }

        final ObjectSpecificationDefault targetObjSpec = getJavaSpecificationOfOwningClass(proxyMethod);
        final ObjectMember member = targetObjSpec.getMember(proxyMethod);

        if(member == null) {
            return proxyMethod.invoke(target, args);
        }

        if(!(member instanceof ObjectAction)) {
            throw new UnsupportedOperationException(
                    "Only actions can be executed in the background "
                            + "(method " + proxyMethod.getName() + " represents a " + member.getFeatureType().name() + "')");
        }

        ObjectAction action = (ObjectAction) member;

        final Object domainObject;
        if (mixedInIfAny == null) {
            domainObject = target;
        } else {
            domainObject = mixedInIfAny;
            // replace action with the mixedIn action of the domain object itself
            action = findMixedInAction(action, mixedInIfAny);
        }

        final ObjectAdapter domainObjectAdapter = getObjectAdapterProvider().adapterFor(domainObject);
        final String domainObjectClassName = CommandUtil.targetClassNameFor(domainObjectAdapter);

        final String targetActionName = CommandUtil.targetMemberNameFor(action);

        final ObjectAdapter[] argAdapters = adaptersFor(args);
        final String targetArgs = CommandUtil.argDescriptionFor(action, argAdapters);

        final Command command = commandContext.getCommand();

        final List<ObjectAdapter> targetList = Collections.singletonList(domainObjectAdapter);
        final CommandDto dto =
                commandDtoServiceInternal.asCommandDto(targetList, action, argAdapters);

        backgroundCommandService
        .schedule(dto, command, domainObjectClassName, targetActionName, targetArgs);

        return null;
    }

    // -- HELPER

    private ObjectAdapterProvider getObjectAdapterProvider() {
        return adapterProviderSupplier.get();
    }

    private ObjectAction findMixedInAction(final ObjectAction action, final Object domainObject) {
        final String actionId = action.getId();
        final ObjectSpecification domainSpec = getObjectAdapterProvider().adapterFor(domainObject).getSpecification();
        List<ObjectAction> objectActions = domainSpec.getObjectActions(Contributed.INCLUDED);
        for (ObjectAction objectAction : objectActions) {
            if(objectAction instanceof ObjectActionMixedIn) {
                ObjectActionMixedIn objectActionMixedIn = (ObjectActionMixedIn) objectAction;
                if(objectActionMixedIn.hasMixinAction(action)) {
                    return objectActionMixedIn;
                }
            }
        }

        throw new IllegalArgumentException(String.format(
                "Unable to find mixin action '%s' for %s", actionId, domainSpec.getFullIdentifier()));
    }

    private ObjectAdapter[] adaptersFor(final Object[] args) {
        final ObjectAdapterProvider adapterProvider = getObjectAdapterProvider();
        return CommandUtil.adaptersFor(args, adapterProvider);
    }

    private ObjectSpecificationDefault getJavaSpecificationOfOwningClass(final Method method) {
        return getJavaSpecification(method.getDeclaringClass());
    }

    private ObjectSpecificationDefault getJavaSpecification(final Class<?> cls) {
        final ObjectSpecification objectSpec = getSpecification(cls);
        if (!(objectSpec instanceof ObjectSpecificationDefault)) {
            throw new UnsupportedOperationException(
                    "Only Java is supported "
                            + "(specification is '" + objectSpec.getClass().getCanonicalName() + "')");
        }
        return (ObjectSpecificationDefault) objectSpec;
    }

    private ObjectSpecification getSpecification(final Class<?> type) {
        return specificationLoader.loadSpecification(type);
    }


}
