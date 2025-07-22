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

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;

import org.apache.causeway.applib.services.wrapper.control.AsyncControl;
import org.apache.causeway.commons.internal.reflection._GenericResolver;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.MixedInMember;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.runtimeservices.session.InteractionIdGenerator;
import org.apache.causeway.core.runtimeservices.wrapper.handlers.DomainObjectInvocationHandler;

import lombok.NonNull;
import lombok.val;

class InvocationHandlerForAsyncWrapMixin<T, R> extends InvocationHandlerforAsyncAbstract<T,R> {

    private final @NonNull Object mixeePojo;

    public InvocationHandlerForAsyncWrapMixin(
            final MetaModelContext metaModelContext,
            final InteractionIdGenerator interactionIdGenerator,
            final ExecutorService commonExecutorService,
            final @NonNull AsyncControl<R> asyncControl,
            final T targetPojo,
            final ObjectSpecification targetSpecification,
            final @NonNull Object mixeePojo) {
        super(metaModelContext, interactionIdGenerator, commonExecutorService, asyncControl, targetPojo, targetSpecification);
        this.mixeePojo = mixeePojo;
    }

    @Override
    public Object invoke(Object proxyObject, Method method, Object[] args) throws Throwable {

        val resolvedMethod = _GenericResolver.resolveMethod(method, targetPojo.getClass())
                .orElseThrow(); // fail early on attempt to invoke method that is not part of the meta-model

        if (isInheritedFromJavaLangObject(method)) {
            return method.invoke(targetPojo, args);
        }

        if (shouldCheckRules(asyncControl)) {
            val doih = new DomainObjectInvocationHandler<>(
                    metaModelContext,
                    null, targetSpecification
            );

            try {
                doih.invoke(proxyObject, method, args);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        val memberAndTarget = locateMemberAndTarget(resolvedMethod, mixeePojo, targetSpecification);
        if (!memberAndTarget.isMemberFound()) {
            return method.invoke(targetPojo, args);
        }

        return submitAsync(memberAndTarget, args, asyncControl);
    }

    <Q> MemberAndTarget locateMemberAndTarget(
            final _GenericResolver.ResolvedMethod method,
            final Q mixeePojo,
            final ObjectSpecification targetSpecification
    ) {

        final var mixinMember = targetSpecification.getMember(method).orElse(null);
        if (mixinMember == null) {
            return MemberAndTarget.notFound();
        }

        // find corresponding action of the mixee (this is the 'real' target, the target usable for invocation).
        final var mixeeClass = mixeePojo.getClass();

        // don't care about anything other than actions
        // (contributed properties and collections are read-only).
        final ObjectAction targetAction = metaModelContext.getSpecificationLoader().specForType(mixeeClass)
                .flatMap(mixeeSpec->mixeeSpec.streamAnyActions(MixedIn.ONLY)
                        .filter(act -> ((MixedInMember)act).hasMixinAction((ObjectAction) mixinMember))
                        .findFirst()
                )
                .orElseThrow(()->new UnsupportedOperationException(String.format(
                        "Could not locate objectAction delegating to mixinAction id='%s' on mixee class '%s'",
                        mixinMember.getId(), mixeeClass.getName())));

        return MemberAndTarget.foundAction(targetAction, metaModelContext.getObjectManager().adapt(mixeePojo), method.method());
    }

}
