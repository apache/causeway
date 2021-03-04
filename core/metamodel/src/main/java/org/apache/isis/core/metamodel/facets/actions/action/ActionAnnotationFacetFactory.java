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

package org.apache.isis.core.metamodel.facets.actions.action;

import java.util.Optional;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.mixins.system.HasInteractionId;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Collections;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.actions.action.associateWith.AssociatedWithFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.explicit.ActionExplicitFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.hidden.HiddenFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetAbstract;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetDefault;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventAbstract;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventFromActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventFromDefault;
import org.apache.isis.core.metamodel.facets.actions.action.prototype.PrototypeFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.semantics.ActionSemanticsFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.typeof.TypeOfFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.fileaccept.FileAcceptFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.members.order.annotprop.MemberOrderFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.members.publish.command.CommandPublishingFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.members.publish.execution.ExecutionPublishingActionFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.domainevents.ActionDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.util.EventUtil;

import lombok.val;

public class ActionAnnotationFacetFactory extends FacetFactoryAbstract {

    public ActionAnnotationFacetFactory() {
        super(FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        val actionIfAny = processMethodContext.synthesizeOnMethodOrMixinType(Action.class);

        processExplicit(processMethodContext, actionIfAny);
        processInvocation(processMethodContext, actionIfAny);
        processHidden(processMethodContext, actionIfAny);
        processRestrictTo(processMethodContext, actionIfAny);
        processSemantics(processMethodContext, actionIfAny);

        // must come after processing semantics
        processCommandPublishing(processMethodContext, actionIfAny);

        // must come after processing semantics
        processExecutionPublishing(processMethodContext, actionIfAny);

        processTypeOf(processMethodContext, actionIfAny);
        processAssociateWith(processMethodContext, actionIfAny);

        processFileAccept(processMethodContext, actionIfAny);
    }

    void processExplicit(final ProcessMethodContext processMethodContext, Optional<Action> actionIfAny) {
        val holder = processMethodContext.getFacetHolder();

        // check for @Action at all.
        val facet = ActionExplicitFacetForActionAnnotation.create(actionIfAny, holder);
        super.addFacet(facet);
    }


    void processInvocation(final ProcessMethodContext processMethodContext, Optional<Action> actionIfAny) {

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


            // search for @Action(domainEvent=...), else use the default event type
            val actionDomainEventFacet =
                    actionIfAny
                    .map(Action::domainEvent)
                    .filter(domainEvent -> domainEvent != ActionDomainEvent.Default.class)
                    .map(domainEvent ->
                            (ActionDomainEventFacetAbstract)
                            new ActionDomainEventFacetForActionAnnotation(
                                    defaultFromDomainObjectIfRequired(typeSpec, domainEvent), holder))
                    .orElse(
                            new ActionDomainEventFacetDefault(
                                    defaultFromDomainObjectIfRequired(typeSpec, ActionDomainEvent.Default.class), holder)
                            );

            if(EventUtil.eventTypeIsPostable(
                    actionDomainEventFacet.getEventType(),
                    ActionDomainEvent.Noop.class,
                    ActionDomainEvent.Default.class,
                    getConfiguration().getApplib().getAnnotation().getAction().getDomainEvent().isPostForDefault())) {
                super.addFacet(actionDomainEventFacet);
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
            super.addFacet(actionInvocationFacet);

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

    void processHidden(final ProcessMethodContext processMethodContext, Optional<Action> actionIfAny) {
        val facetedMethod = processMethodContext.getFacetHolder();

        // search for @Action(hidden=...)
        val facet = HiddenFacetForActionAnnotation.create(actionIfAny, facetedMethod);
        super.addFacet(facet);
    }

    void processRestrictTo(final ProcessMethodContext processMethodContext, Optional<Action> actionIfAny) {
        val facetedMethod = processMethodContext.getFacetHolder();

        // search for @Action(restrictTo=...)
        val facet = PrototypeFacetForActionAnnotation.create(actionIfAny, facetedMethod,
                ()->super.getSystemEnvironment().getDeploymentType());

        super.addFacet(facet);
    }

    void processSemantics(final ProcessMethodContext processMethodContext, Optional<Action> actionIfAny) {
        val facetedMethod = processMethodContext.getFacetHolder();

        // check for @Action(semantics=...)
        val facet = ActionSemanticsFacetForActionAnnotation.create(actionIfAny, facetedMethod);

        super.addFacet(facet);
    }

    void processCommandPublishing(
            final ProcessMethodContext processMethodContext,
            final Optional<Action> actionIfAny) {

        val facetedMethod = processMethodContext.getFacetHolder();

        //
        // this rule inspired by a similar rule for auditing and publishing, see DomainObjectAnnotationFacetFactory
        //
        if(HasInteractionId.class.isAssignableFrom(processMethodContext.getCls())) {
            // do not install on any implementation of HasInteractionId
            // (ie commands, audit entries, published events).
            return;
        }

        // check for @Action(commandPublishing=...)
        val commandPublishingFacet = CommandPublishingFacetForActionAnnotation
                .create(actionIfAny, getConfiguration(), getServiceInjector(), facetedMethod);

        super.addFacet(commandPublishingFacet);
    }

    void processExecutionPublishing(
            final ProcessMethodContext processMethodContext,
            final Optional<Action> actionIfAny) {

        val facetedMethod = processMethodContext.getFacetHolder();

        //
        // this rule inspired by a similar rule for auditing and publishing, see DomainObjectAnnotationFacetFactory
        // and for commands, see above
        //
        if(HasInteractionId.class.isAssignableFrom(processMethodContext.getCls())) {
            // do not install on any implementation of HasInteractionId
            // (ie commands, audit entries, published events).
            return;
        }

        // check for @Action(executionPublishing=...)
        val executionPublishingFacet = ExecutionPublishingActionFacetForActionAnnotation
                .create(actionIfAny, getConfiguration(), facetedMethod);
        super.addFacet(executionPublishingFacet);
    }

    void processTypeOf(final ProcessMethodContext processMethodContext, Optional<Action> actionIfAny) {

        val method = processMethodContext.getMethod();
        val facetedMethod = processMethodContext.getFacetHolder();

        val methodReturnType = method.getReturnType();
        if (!_Collections.isCollectionOrArrayType(methodReturnType)) {
            return;
        }

        // check for @Action(typeOf=...)
        TypeOfFacet typeOfFacet = actionIfAny
                .map(Action::typeOf)
                .filter(typeOf -> typeOf != null && typeOf != Object.class)
                .map(typeOf -> new TypeOfFacetForActionAnnotation(typeOf, facetedMethod))
                .orElse(null);

        // infer from generic return type
        if(typeOfFacet == null) {
            val cls = processMethodContext.getCls();
            typeOfFacet = TypeOfFacet.Util.inferFromMethodReturnType(facetedMethod, cls, method);
        }

        super.addFacet(typeOfFacet);
    }

    void processAssociateWith(final ProcessMethodContext processMethodContext, Optional<Action> actionIfAny) {

        val facetedMethod = processMethodContext.getFacetHolder();

        // check for @Action(associateWith=...)

        actionIfAny.ifPresent(action->{
            val associateWith = action.associateWith();
            if(!_Strings.isNullOrEmpty(associateWith)) {
                val associateWithSequence = action.associateWithSequence();
                super.addFacet(
                        new MemberOrderFacetForActionAnnotation(associateWith, associateWithSequence, facetedMethod));
                super.addFacet(
                        new AssociatedWithFacetForActionAnnotation(associateWith, facetedMethod));
            }
        });


    }

    void processFileAccept(final ProcessMethodContext processMethodContext, Optional<Action> actionIfAny) {

        val holder = processMethodContext.getFacetHolder();

        // check for @Action(fileAccept=...)
        val facet = FileAcceptFacetForActionAnnotation.create(actionIfAny, holder);
        super.addFacet(facet);

    }


}
