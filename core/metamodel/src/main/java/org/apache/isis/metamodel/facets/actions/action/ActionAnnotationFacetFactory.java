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

package org.apache.isis.metamodel.facets.actions.action;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.services.HasUniqueId;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Collections;
import org.apache.isis.commons.internal.environment.IsisSystemEnvironment;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.metamodel.facets.actions.action.associateWith.AssociatedWithFacetForActionAnnotation;
import org.apache.isis.metamodel.facets.actions.action.command.CommandFacetForActionAnnotation;
import org.apache.isis.metamodel.facets.actions.action.hidden.HiddenFacetForActionAnnotation;
import org.apache.isis.metamodel.facets.actions.action.invocation.ActionDomainEventFacetAbstract;
import org.apache.isis.metamodel.facets.actions.action.invocation.ActionDomainEventFacetDefault;
import org.apache.isis.metamodel.facets.actions.action.invocation.ActionDomainEventFacetForActionAnnotation;
import org.apache.isis.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventAbstract;
import org.apache.isis.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventFromActionAnnotation;
import org.apache.isis.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventFromDefault;
import org.apache.isis.metamodel.facets.actions.action.prototype.PrototypeFacetForActionAnnotation;
import org.apache.isis.metamodel.facets.actions.action.publishing.PublishedActionFacetForActionAnnotation;
import org.apache.isis.metamodel.facets.actions.action.semantics.ActionSemanticsFacetForActionAnnotation;
import org.apache.isis.metamodel.facets.actions.action.typeof.TypeOfFacetForActionAnnotation;
import org.apache.isis.metamodel.facets.members.order.annotprop.MemberOrderFacetForActionAnnotation;
import org.apache.isis.metamodel.facets.object.domainobject.domainevents.ActionDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.util.EventUtil;

import lombok.val;

public class ActionAnnotationFacetFactory extends FacetFactoryAbstract {

    public ActionAnnotationFacetFactory() {
        super(FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        processInvocation(processMethodContext);
        processHidden(processMethodContext);
        processRestrictTo(processMethodContext);
        processSemantics(processMethodContext);

        // must come after processing semantics
        processCommand(processMethodContext);

        // must come after processing semantics
        processPublishing(processMethodContext);

        processTypeOf(processMethodContext);
        processAssociateWith(processMethodContext);
    }

    void processInvocation(final ProcessMethodContext processMethodContext) {

        val actionMethod = processMethodContext.getMethod();

        try {
            val returnType = actionMethod.getReturnType();
            val returnSpec = getSpecificationLoader().loadSpecification(returnType);
            if (returnSpec == null) {
                return;
            }

            val cls = processMethodContext.getCls();
            val typeSpec = getSpecificationLoader().loadSpecification(cls);
            val holder = processMethodContext.getFacetHolder();

            //
            // Set up ActionDomainEventFacet, which will act as the hiding/disabling/validating advisor
            //
            val actionIfAny = processMethodContext.synthesizeOnMethod(Action.class);

            // search for @Action(domainEvent=...), else use the default event type
            final ActionDomainEventFacetAbstract actionDomainEventFacet =
                    actionIfAny
                    .map(Action::domainEvent)
                    .filter(domainEvent -> domainEvent != ActionDomainEvent.Default.class)
                    .filter(domainEvent -> {
                        
                        
                        if(!ActionDomainEvent.class.isAssignableFrom(domainEvent)) {
                            System.out.println("#### " + actionMethod + " -> " + domainEvent);
                            return false;
                        }
                        
                        return true;
                    })
                    .map(domainEvent ->
                    (ActionDomainEventFacetAbstract) new ActionDomainEventFacetForActionAnnotation(
                            defaultFromDomainObjectIfRequired(typeSpec, domainEvent), holder))
                    .orElse(
                            new ActionDomainEventFacetDefault(
                                    defaultFromDomainObjectIfRequired(typeSpec, ActionDomainEvent.Default.class), holder)
                            );

            if(EventUtil.eventTypeIsPostable(
                    actionDomainEventFacet.getEventType(),
                    ActionDomainEvent.Noop.class,
                    ActionDomainEvent.Default.class,
                    getConfiguration().getReflector().getFacet().getActionAnnotation().getDomainEvent().isPostForDefault())) {
                FacetUtil.addFacet(actionDomainEventFacet);
            }

            // replace the current actionInvocationFacet with one that will
            // emit the appropriate domain event and then delegate onto the underlying

            final ActionInvocationFacetForDomainEventAbstract actionInvocationFacet;
            if (actionDomainEventFacet instanceof ActionDomainEventFacetForActionAnnotation) {
                actionInvocationFacet = new ActionInvocationFacetForDomainEventFromActionAnnotation(
                        actionDomainEventFacet.getEventType(), actionMethod, typeSpec, returnSpec, holder);
            } else
                // default
            {
                actionInvocationFacet = new ActionInvocationFacetForDomainEventFromDefault(
                        actionDomainEventFacet.getEventType(), actionMethod, typeSpec, returnSpec, holder);
            }
            FacetUtil.addFacet(actionInvocationFacet);

        } finally {
            processMethodContext.removeMethod(actionMethod);
        }
    }

    private static Class<? extends ActionDomainEvent<?>> defaultFromDomainObjectIfRequired(
            final ObjectSpecification typeSpec,
            final Class<? extends ActionDomainEvent<?>> actionDomainEventType) {
        
        if (actionDomainEventType == ActionDomainEvent.Default.class) {
            val typeFromDomainObject =
                    typeSpec.getFacet(ActionDomainEventDefaultFacetForDomainObjectAnnotation.class);
            if (typeFromDomainObject != null) {
                return typeFromDomainObject.getEventType();
            }
        }
        return actionDomainEventType;
    }

    void processHidden(final ProcessMethodContext processMethodContext) {
        val holder = processMethodContext.getFacetHolder();

        // search for @Action(hidden=...)
        val actionIfAny = processMethodContext.synthesizeOnMethod(Action.class);
        val facet = HiddenFacetForActionAnnotation.create(actionIfAny, holder);
        FacetUtil.addFacet(facet);
    }

    void processRestrictTo(final ProcessMethodContext processMethodContext) {
        val holder = processMethodContext.getFacetHolder();

        // search for @Action(restrictTo=...)
        val actionIfAny = processMethodContext.synthesizeOnMethod(Action.class);
        val facet = PrototypeFacetForActionAnnotation.create(actionIfAny, holder,
                ()->IsisSystemEnvironment.get().getDeploymentType());

        FacetUtil.addFacet(facet);
    }

    void processSemantics(final ProcessMethodContext processMethodContext) {
        val holder = processMethodContext.getFacetHolder();

        // check for @Action(semantics=...)
        val actionIfAny = processMethodContext.synthesizeOnMethod(Action.class);
        val facet = ActionSemanticsFacetForActionAnnotation.create(actionIfAny, holder);

        FacetUtil.addFacet(facet);
    }

    void processCommand(final ProcessMethodContext processMethodContext) {

        val facetHolder = processMethodContext.getFacetHolder();

        //
        // this rule inspired by a similar rule for auditing and publishing, see DomainObjectAnnotationFacetFactory
        //
        if(HasUniqueId.class.isAssignableFrom(processMethodContext.getCls())) {
            // do not install on any implementation of HasTransactionId
            // (ie commands, audit entries, published events).
            return;
        }

        // check for @Action(command=...)
        val actionIfAny = processMethodContext.synthesizeOnMethod(Action.class);
        val commandFacet = CommandFacetForActionAnnotation.create(actionIfAny, getConfiguration(), getServiceInjector(), facetHolder);

        FacetUtil.addFacet(commandFacet);
    }

    void processPublishing(final ProcessMethodContext processMethodContext) {

        val holder = processMethodContext.getFacetHolder();

        //
        // this rule inspired by a similar rule for auditing and publishing, see DomainObjectAnnotationFacetFactory
        // and for commands, see above
        //
        if(HasUniqueId.class.isAssignableFrom(processMethodContext.getCls())) {
            // do not install on any implementation of HasTransactionId
            // (ie commands, audit entries, published events).
            return;
        }

        // check for @Action(publishing=...)
        val actionIfAny = processMethodContext.synthesizeOnMethod(Action.class);
        val facet = PublishedActionFacetForActionAnnotation.create(actionIfAny, getConfiguration(), holder);

        FacetUtil.addFacet(facet);
    }

    void processTypeOf(final ProcessMethodContext processMethodContext) {

        val method = processMethodContext.getMethod();
        val holder = processMethodContext.getFacetHolder();

        val methodReturnType = method.getReturnType();
        if (!_Collections.isCollectionOrArrayType(methodReturnType)) {
            return;
        }

        // check for @Action(typeOf=...)
        val actionIfAny = processMethodContext.synthesizeOnMethod(Action.class);
        TypeOfFacet typeOfFacet = actionIfAny
                .map(Action::typeOf)
                .filter(typeOf -> typeOf != null && typeOf != Object.class)
                .map(typeOf -> new TypeOfFacetForActionAnnotation(typeOf, holder))
                .orElse(null);

        // infer from return type
        if(typeOfFacet == null) {
            val returnType = method.getReturnType();
            typeOfFacet = TypeOfFacet.Util.inferFromArrayType(holder, returnType);
        }

        // infer from generic return type
        if(typeOfFacet == null) {
            val cls = processMethodContext.getCls();
            typeOfFacet = TypeOfFacet.Util.inferFromGenericReturnType(cls, method, holder);
        }

        FacetUtil.addFacet(typeOfFacet);
    }

    void processAssociateWith(final ProcessMethodContext processMethodContext) {

        val holder = processMethodContext.getFacetHolder();

        // check for @Action(associateWith=...)

        val actionIfAny = processMethodContext.synthesizeOnMethod(Action.class);
        actionIfAny.ifPresent(action->{
            val associateWith = action.associateWith();
            if(!_Strings.isNullOrEmpty(associateWith)) {
                val associateWithSequence = action.associateWithSequence();
                FacetUtil.addFacet(
                        new MemberOrderFacetForActionAnnotation(associateWith, associateWithSequence, holder));
                FacetUtil.addFacet(
                        new AssociatedWithFacetForActionAnnotation(associateWith, holder));
            }
        });
        

    }


}
