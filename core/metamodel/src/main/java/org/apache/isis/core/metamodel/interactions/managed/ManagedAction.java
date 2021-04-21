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

import java.util.Optional;

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
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public final class ManagedAction extends ManagedMember {

    // -- FACTORIES
    
    public static final ManagedAction of(
            final @NonNull ManagedObject owner, 
            final @NonNull ObjectAction action,
            final @NonNull Where where) {
        return new ManagedAction(owner, action, where);
    }
    
    public static final Optional<ManagedAction> lookupAction(
            @NonNull final ManagedObject owner,
            @NonNull final String memberId,
            @NonNull final Where where) {
        
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
    
    public _Either<ManagedObject, InteractionVeto> invoke(@NonNull Can<ManagedObject> actionParameters) {
            
        // param validation is not our responsibility here
        
        val action = getAction();
        
        val head = action.interactionHead(getOwner());
        
        val actionResult = action
                .execute(head , actionParameters, InteractionInitiatedBy.USER);
        
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(actionResult)) {
            return _Either.left(ManagedObject.empty(action.getReturnType()));
        }
        
        val resultPojo = actionResult.getPojo();

        //TODO same logic is in wkt's ActionModel, ultimately we want wkt to use this (common) one 
        val resultAdapter = getRoutingServices().stream()
                .filter(routingService->routingService.canRoute(resultPojo))
                .map(routingService->routingService.route(resultPojo))
                .filter(_NullSafe::isPresent)
                .map(this::toManagedObject)
                .filter(_NullSafe::isPresent)
                .findFirst()
                .orElse(actionResult);
        
        // resolve injection-points for the result
        getServiceInjector().injectServicesInto(resultAdapter.getPojo());
        
        //XXX are we sure in case of entities, that these are attached?
        
        return _Either.left(resultAdapter);
        
    }
    
    // -- POJO WRAPPING
    
    private ManagedObject toManagedObject(Object pojo) {
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

}
