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
package org.apache.isis.core.metamodel.interactions.managed;

import java.io.Serializable;
import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.routing.RoutingService;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember.AuthorizationException;
import org.apache.isis.core.metamodel.spec.feature.memento.ActionMemento;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public final class ManagedAction extends ManagedMember {

    // -- FACTORIES

    public static final ManagedAction of(
            final @NonNull ManagedObject owner,
            final @NonNull ObjectAction action,
            final @NonNull Where where) {
        return new ManagedAction(owner, action, where);
    }

    public static final Optional<ManagedAction> lookupAction(
            final @NonNull ManagedObject owner,
            final @NonNull String memberId,
            final @NonNull Where where) {

        return ManagedMember.<ObjectAction>lookup(owner, MemberType.ACTION, memberId)
        .map(objectAction -> of(owner, objectAction, where));
    }

    // -- IMPLEMENTATION

    @Getter private final ObjectAction action;

    @Getter private final ActionInteractionHead interactionHead;

    private ManagedAction(
            final @NonNull ManagedObject owner,
            final @NonNull ObjectAction action,
            final @NonNull Where where) {

        super(owner, where);
        this.action = action;
        this.interactionHead = action.interactionHead(owner);
    }

    /**
     * @returns a new {@link ParameterNegotiationModel} that is associated with this managed-action;
     * parameters if any are initialized with their defaults (taking into account any supporting methods)
     */
    public ParameterNegotiationModel startParameterNegotiation() {
        return getInteractionHead().defaults();
    }

    @Override
    public ObjectAction getMetaModel() {
        return getAction();
    }

    @Override
    public MemberType getMemberType() {
        return MemberType.ACTION;
    }

    // -- INTERACTION

    //XXX potential misuse: param validation is not our responsibility here
    // hence at least should not be public
    @Deprecated
    public _Either<ManagedObject, InteractionVeto> invoke(@NonNull final Can<ManagedObject> actionParameters) {

        final var action = getAction();

        final var head = action.interactionHead(getOwner());

        final ManagedObject actionResult = action
                .execute(head , actionParameters, InteractionInitiatedBy.USER);

        return _Either.left(route(actionResult));

    }

    public ManagedObject invokeWithRuleChecking(
            final @NonNull Can<ManagedObject> actionParameters) throws AuthorizationException {

        final var action = getAction();
        final var head = action.interactionHead(getOwner());

        final ManagedObject actionResult = action
                .executeWithRuleChecking(head , actionParameters, InteractionInitiatedBy.USER, getWhere());

        return route(actionResult);
    }

    private ManagedObject route(final @Nullable ManagedObject actionResult) {

        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(actionResult)) {
            return ManagedObject.empty(action.getReturnType());
        }

        final var resultPojo = actionResult.getPojo();

        final var resultAdapter = getRoutingServices().stream()
                .filter(routingService->routingService.canRoute(resultPojo))
                .map(routingService->routingService.route(resultPojo))
                .filter(_NullSafe::isPresent)
                .map(this::toManagedObject)
                .filter(_NullSafe::isPresent)
                .findFirst()
                .orElse(actionResult);

        // resolve injection-points for the result
        getServiceInjector().injectServicesInto(resultAdapter.getPojo());
        return resultAdapter;
    }


    // -- POJO WRAPPING

    private ManagedObject toManagedObject(final Object pojo) {
        return ManagedObject.lazy(mmc().getSpecificationLoader(), pojo);
    }

    // -- ACTION RESULT ROUTING

    private Can<RoutingService> getRoutingServices() {
        return routingServices.get();
    }

    private final _Lazy<Can<RoutingService>> routingServices = _Lazy.threadSafe(this::lookupRoutingServices);

    private Can<RoutingService> lookupRoutingServices() {
        return getServiceRegistry().select(RoutingService.class);
    }

    // -- SERVICES

    private MetaModelContext mmc() {
        return getAction().getMetaModelContext();
    }

    private ServiceInjector getServiceInjector() {
        return mmc().getServiceInjector();
    }

    private ServiceRegistry getServiceRegistry() {
        return mmc().getServiceRegistry();
    }

    // -- MEMENTO

    public Memento getMemento() {
        return Memento.create(this);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Memento implements Serializable {
        private static final long serialVersionUID = 1L;

        static Memento create(final ManagedAction managedAction) {
            final var action = managedAction.getMetaModel();
            return new Memento(
                    action.getMemento(),
                    action.getObjectManager()
                        .getObjectMemorizer()
                        .serialize(managedAction.getOwner()),
                        managedAction.getWhere());
        }

        private final ActionMemento actionMemento;
        private final ObjectMemento objectMemento;
        private final Where where;
        public ManagedAction getManagedAction(final MetaModelContext mmc) {

            final var action = actionMemento.getAction(mmc::getSpecificationLoader);
            final var spec = action.getOnType();
            final var owner = mmc.getObjectManager().getObjectMemorizer().deserialize(spec, objectMemento);

            return ManagedAction.of(owner, action, where);
        }
    }


}
