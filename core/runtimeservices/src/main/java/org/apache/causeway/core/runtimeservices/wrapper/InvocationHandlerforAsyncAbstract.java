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
package org.apache.causeway.core.runtimeservices.wrapper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.locale.UserLocale;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionLayer;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.wrapper.control.AsyncControl;
import org.apache.causeway.applib.services.wrapper.control.ExecutionMode;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.command.CommandDtoFactory;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.runtimeservices.session.InteractionIdGenerator;
import org.apache.causeway.schema.cmd.v2.CommandDto;

import lombok.NonNull;
import lombok.val;

abstract class InvocationHandlerforAsyncAbstract<T, R> implements InvocationHandler {

    final MetaModelContext metaModelContext;
    final InteractionIdGenerator interactionIdGenerator;
    private final ExecutorService commonExecutorService;
    final @NonNull AsyncControl<R> asyncControl;
    final @NonNull T targetPojo;
    final ObjectSpecification targetSpecification;

    public InvocationHandlerforAsyncAbstract(
            final MetaModelContext metaModelContext,
            final InteractionIdGenerator interactionIdGenerator,
            final ExecutorService commonExecutorService,
            final @NonNull AsyncControl<R> asyncControl,
            final @NonNull T targetPojo,
            final ObjectSpecification targetSpecification) {
        this.metaModelContext = metaModelContext;
        this.interactionIdGenerator = interactionIdGenerator;
        this.commonExecutorService = commonExecutorService;
        this.asyncControl = asyncControl;
        this.targetPojo = targetPojo;
        this.targetSpecification = targetSpecification;
    }

    boolean isInheritedFromJavaLangObject(final Method method) {
        return method.getDeclaringClass().equals(Object.class);
    }

    boolean shouldCheckRules(final AsyncControl<?> asyncControl) {
        val executionModes = asyncControl.getExecutionModes();
        val skipRules = executionModes.contains(ExecutionMode.SKIP_RULE_VALIDATION);
        return !skipRules;
    }

    private InteractionLayer currentInteractionLayer() {
        return getInteractionService().currentInteractionLayerElseFail();
    }

    private InteractionService getInteractionService() {
        return metaModelContext.getInteractionService();
    }

    private CommandDtoFactory getCommandDtoFactory() {
        return metaModelContext.getCommandDtoFactory();
    }


    <R> Object submitAsync(
            final MemberAndTarget memberAndTarget,
            final Object[] args,
            final AsyncControl<R> asyncControl) {

        final var interactionLayer = currentInteractionLayer();
        final var interactionContext = interactionLayer.getInteractionContext();
        final var asyncInteractionContext = interactionContextFrom(asyncControl, interactionContext);

        final var parentCommand = getInteractionService().currentInteractionElseFail().getCommand();
        final var parentInteractionId = parentCommand.getInteractionId();

        final var targetAdapter = memberAndTarget.getTarget();
        final var method = memberAndTarget.getMethod();

        final var head = InteractionHead.regular(targetAdapter);

        final var childInteractionId = interactionIdGenerator.interactionId();
        CommandDto childCommandDto;
        switch (memberAndTarget.getType()) {
            case ACTION:
                final var action = memberAndTarget.getAction();
                final var argAdapters = ManagedObject.adaptParameters(action.getParameters(), _Lists.ofArray(args));
                childCommandDto = getCommandDtoFactory()
                        .asCommandDto(childInteractionId, head, action, argAdapters);
                break;
            case PROPERTY:
                final var property = memberAndTarget.getProperty();
                final var propertyValueAdapter = ManagedObject.adaptProperty(property, args[0]);
                childCommandDto = getCommandDtoFactory()
                        .asCommandDto(childInteractionId, head, property, propertyValueAdapter);
                break;
            default:
                // shouldn't happen, already catered for this case previously
                return null;
        }
        final var oidDto = childCommandDto.getTargets().getOid().get(0);

        asyncControl.setMethod(method);
        asyncControl.setBookmark(Bookmark.forOidDto(oidDto));

        final var executorService = Optional.ofNullable(asyncControl.getExecutorService())
                .orElse(commonExecutorService);
        final var asyncTask = metaModelContext.getServiceInjector().injectServicesInto(new WrapperFactoryDefault.AsyncTask<R>(
                asyncInteractionContext,
                Propagation.REQUIRES_NEW,
                childCommandDto,
                asyncControl.getReturnType(),
                parentInteractionId)); // this command becomes the parent of child command

        final var future = executorService.submit(asyncTask);
        asyncControl.setFuture(future);

        return null;
    }

    private static <R> InteractionContext interactionContextFrom(
            final AsyncControl<R> asyncControl,
            final InteractionContext interactionContext) {

        return InteractionContext.builder()
                .clock(Optional.ofNullable(asyncControl.getClock()).orElseGet(interactionContext::getClock))
                .locale(Optional.ofNullable(asyncControl.getLocale()).map(UserLocale::valueOf).orElse(null)) // if not set in asyncControl use defaults (set override to null)
                .timeZone(Optional.ofNullable(asyncControl.getTimeZone()).orElseGet(interactionContext::getTimeZone))
                .user(Optional.ofNullable(asyncControl.getUser()).orElseGet(interactionContext::getUser))
                .build();
    }


}
