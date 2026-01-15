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
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.runtimeservices.session.InteractionIdGenerator;
import org.apache.causeway.core.runtimeservices.wrapper.handlers.DomainObjectInvocationHandler;

import lombok.NonNull;
import lombok.val;

class InvocationHandlerforAsyncWrapRegular<T, R> extends InvocationHandlerforAsyncAbstract<T,R> {

    private final ManagedObject targetAdapter;

    public InvocationHandlerforAsyncWrapRegular(
            final MetaModelContext metaModelContext,
            final InteractionIdGenerator interactionIdGenerator,
            final ExecutorService commonExecutorService,
            final AsyncControl<R> asyncControl,
            final @NonNull T targetPojo,
            final ManagedObject targetAdapter) {
        super(metaModelContext, interactionIdGenerator, commonExecutorService, asyncControl, targetPojo, targetAdapter.objSpec());
        this.targetAdapter = targetAdapter;
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

        val memberAndTarget = locateMemberAndTarget(resolvedMethod, targetAdapter);
        if (!memberAndTarget.isMemberFound()) {
            return method.invoke(targetPojo, args);
        }

        return submitAsync(memberAndTarget, args, asyncControl);
    }

    MemberAndTarget locateMemberAndTarget(
            final _GenericResolver.ResolvedMethod method,
            final ManagedObject targetAdapter) {

        final var objectMember = targetAdapter.objSpec().getMember(method).orElse(null);
        if(objectMember == null) {
            return MemberAndTarget.notFound();
        }

        if (objectMember instanceof OneToOneAssociation) {
            return MemberAndTarget.foundProperty((OneToOneAssociation) objectMember, targetAdapter, method.method());
        }
        if (objectMember instanceof ObjectAction) {
            return MemberAndTarget.foundAction((ObjectAction) objectMember, targetAdapter, method.method());
        }

        throw new UnsupportedOperationException(
                "Only properties and actions can be executed in the background "
                        + "(method " + method.name() + " represents a " + objectMember.getFeatureType().name() + "')");
    }
}
