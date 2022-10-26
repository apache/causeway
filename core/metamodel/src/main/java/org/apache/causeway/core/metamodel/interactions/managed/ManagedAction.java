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
package org.apache.causeway.core.metamodel.interactions.managed;

import java.io.Serializable;
import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.applib.services.routing.RoutingService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Railway;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember.AuthorizationException;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

public final class ManagedAction extends ManagedMember {

    // -- FACTORIES

    public static final ManagedAction of(
            final @NonNull ManagedObject owner,
            final @NonNull ObjectAction action,
            final @NonNull Where where) {
        return new ManagedAction(owner, action, where, Can::empty);
    }

    public static final Optional<ManagedAction> lookupAction(
            final @NonNull ManagedObject owner,
            final @NonNull String memberId,
            final @NonNull Where where) {

        return ManagedMember.<ObjectAction>lookup(owner, Identifier.Type.ACTION, memberId)
        .map(objectAction -> of(owner, objectAction, where));
    }

    public static final Optional<ManagedAction> lookupActionWithMultiselect(
            final @NonNull ManagedObject owner,
            final @NonNull String memberId,
            final @NonNull Where where,
            final @NonNull MultiselectChoices multiselectChoices) {

        return ManagedMember.<ObjectAction>lookup(owner, Identifier.Type.ACTION, memberId)
                .map(objectAction -> new ManagedAction(owner, objectAction, where, multiselectChoices));
    }

    // -- IMPLEMENTATION

    @Getter private final ObjectAction action;
    @Getter private final MultiselectChoices multiselectChoices;

    private ManagedAction(
            final @NonNull ManagedObject owner,
            final @NonNull ObjectAction action,
            final @NonNull Where where,
            final @NonNull MultiselectChoices multiselectChoices) {

        super(owner, where);
        /* entities might become removed, but even though removed, an entity delete mixin say,
            may still want to provide an action result, that does not need the mixee instance to be produced;
            eg. delete ApplicationUser mixin that returns a collection of all remaining users
            after deleting the selected one */
        if(!owner.getSpecialization().isEntity()) {
            _Assert.assertFalse(ManagedObjects.isNullOrUnspecifiedOrEmpty(owner), ()->
                    String.format("cannot create managed-action for action %s with an empty owner %s",
                            action.getFeatureIdentifier(),
                            owner));
        }
        this.action = action;
        this.multiselectChoices = multiselectChoices;
    }

    //CAUSEWAY-2897 ... don't memoize the head, as owner might dynamically re-attach (when entity)
    ActionInteractionHead interactionHead() {
        return action.interactionHead(getOwner());
    }

    /**
     * @returns a new {@link ParameterNegotiationModel} that is associated with this managed-action;
     * parameters if any are initialized with their defaults (taking into account any supporting methods)
     */
    public ParameterNegotiationModel startParameterNegotiation() {
        return interactionHead().defaults(this);
    }

    @Override
    public ObjectAction getMetaModel() {
        return getAction();
    }

    @Override
    public Identifier.Type getMemberType() {
        return Identifier.Type.ACTION;
    }

    // -- INTERACTION

    public Railway<InteractionVeto, ManagedObject> invoke(
            final @NonNull Can<ManagedObject> actionParameters,
            final @NonNull InteractionInitiatedBy interactionInitiatedBy) {

        final ManagedObject actionResult = getAction()
                // under the hood intercepts cases, where the owner is a value-type;
                // executions on value-types have no rule checking and trigger no domain events
                .execute(interactionHead(), actionParameters, interactionInitiatedBy);

        return Railway.success(route(actionResult));
    }

    public Railway<InteractionVeto, ManagedObject> invoke(
            final @NonNull Can<ManagedObject> actionParameters) {
        return invoke(actionParameters, InteractionInitiatedBy.USER);
    }

    @SneakyThrows
    public ManagedObject invokeWithRuleChecking(
            final @NonNull Can<ManagedObject> actionParameters) throws AuthorizationException {

        final ManagedObject actionResult = getAction()
                // under the hood intercepts cases, where the owner is a value-type;
                // executions on value-types have no rule checking and trigger no domain events
                .executeWithRuleChecking(
                        interactionHead(), actionParameters, InteractionInitiatedBy.USER, getWhere());

        return route(actionResult);
    }

    // -- ACTION RESULT ROUTING

    private ManagedObject route(final @Nullable ManagedObject actionResult) {

        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(actionResult)) {
            return ManagedObject.empty(action.getReturnType());
        }

        val resultPojo = actionResult.getPojo();
        val objManager = mmc().getObjectManager();

        val resultAdapter = getRoutingServices().stream()
                .filter(routingService->routingService.canRoute(resultPojo))
                .map(routingService->routingService.route(resultPojo))
                .filter(_NullSafe::isPresent)
                .map(objManager::adapt)
                .filter(_NullSafe::isPresent)
                .findFirst()
                .orElse(actionResult);

        // resolve injection-points for the result
        getServiceInjector().injectServicesInto(resultAdapter.getPojo());
        return resultAdapter;
    }

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

    // -- MEMENTO FOR ARGUMENT LIST

    public MementoForArgs getMementoForArgs(final Can<ManagedObject> args) {
        return MementoForArgs.create(
                getMetaModel().getMetaModelContext().getObjectManager(),
                args);
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class MementoForArgs implements Serializable {
        private static final long serialVersionUID = 1L;

        static MementoForArgs create(
                final ObjectManager objectManager,
                final Can<ManagedObject> args) {
            return new MementoForArgs(args.map(objectManager::mementifyElseFail));
        }

        private final Can<ObjectMemento> argsMementos;

        public Can<ManagedObject> getArgumentList(final ObjectAction actionMeta) {
            val argTypes = actionMeta.getParameterTypes();
            val objectManager = actionMeta.getMetaModelContext().getObjectManager();
            return argsMementos.zipMap(argTypes, (argSpec, argMemento)->
                objectManager.demementify(argMemento, argSpec));
        }
    }


}
