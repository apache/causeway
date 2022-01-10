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

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.mixins.system.HasInteractionId;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Collections;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.actions.action.associateWith.ChoicesFromFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.explicit.ActionExplicitFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.hidden.HiddenFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetAbstract;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetDefault;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventFromActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventFromDefault;
import org.apache.isis.core.metamodel.facets.actions.action.prototype.PrototypeFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.semantics.ActionSemanticsFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.typeof.TypeOfFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.fileaccept.FileAcceptFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.members.layout.group.LayoutGroupFacetFromActionAnnotation;
import org.apache.isis.core.metamodel.facets.members.publish.command.CommandPublishingFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.members.publish.execution.ExecutionPublishingActionFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.domainevents.ActionDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForAmbiguousMixinAnnotations;
import org.apache.isis.core.metamodel.util.EventUtil;

import lombok.val;

public class ActionAnnotationFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public ActionAnnotationFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        val actionIfAny = processMethodContext
                .synthesizeOnMethodOrMixinType(
                        Action.class,
                        () -> MetaModelValidatorForAmbiguousMixinAnnotations
                        .addValidationFailure(processMethodContext.getFacetHolder(), Action.class));

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
        processChoicesFrom(processMethodContext, actionIfAny);

        processFileAccept(processMethodContext, actionIfAny);
    }

    void processExplicit(final ProcessMethodContext processMethodContext, final Optional<Action> actionIfAny) {
        val holder = processMethodContext.getFacetHolder();

        // check for @Action at all.
        addFacetIfPresent(
                ActionExplicitFacetForActionAnnotation
                .create(actionIfAny, holder));
    }


    void processInvocation(final ProcessMethodContext processMethodContext, final Optional<Action> actionIfAny) {

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
                addFacet(actionDomainEventFacet);
            }

            // replace the current actionInvocationFacet with one that will
            // emit the appropriate domain event and then delegate onto the underlying

            addFacet(actionDomainEventFacet instanceof ActionDomainEventFacetForActionAnnotation
                    ? new ActionInvocationFacetForDomainEventFromActionAnnotation(
                            actionDomainEventFacet.getEventType(), actionMethod, typeSpec, returnSpec, holder)
                    : new ActionInvocationFacetForDomainEventFromDefault(
                            actionDomainEventFacet.getEventType(), actionMethod, typeSpec, returnSpec, holder));

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

    void processHidden(final ProcessMethodContext processMethodContext, final Optional<Action> actionIfAny) {
        val facetedMethod = processMethodContext.getFacetHolder();

        // search for @Action(hidden=...)
        addFacetIfPresent(
                HiddenFacetForActionAnnotation
                .create(actionIfAny, facetedMethod));
    }

    void processRestrictTo(final ProcessMethodContext processMethodContext, final Optional<Action> actionIfAny) {
        val facetedMethod = processMethodContext.getFacetHolder();

        // search for @Action(restrictTo=...)
        addFacetIfPresent(
                PrototypeFacetForActionAnnotation
                .create(
                        actionIfAny, facetedMethod,
                        ()->super.getSystemEnvironment().getDeploymentType()));
    }

    void processSemantics(final ProcessMethodContext processMethodContext, final Optional<Action> actionIfAny) {
        val facetedMethod = processMethodContext.getFacetHolder();

        // check for @Action(semantics=...)
        addFacet(
                ActionSemanticsFacetForActionAnnotation
                .create(actionIfAny, facetedMethod));
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
        addFacetIfPresent(CommandPublishingFacetForActionAnnotation
                .create(actionIfAny, getConfiguration(), getServiceInjector(), facetedMethod));
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
        addFacetIfPresent(
                ExecutionPublishingActionFacetForActionAnnotation
                .create(actionIfAny, getConfiguration(), facetedMethod));
    }

    void processTypeOf(final ProcessMethodContext processMethodContext, final Optional<Action> actionIfAny) {

        val method = processMethodContext.getMethod();
        val facetedMethod = processMethodContext.getFacetHolder();

        val methodReturnType = method.getReturnType();
        if (!_Collections.isCollectionOrArrayOrCanType(methodReturnType)) {
            return;
        }

        addFacetIfPresent(
                TypeOfFacetForActionAnnotation.create(actionIfAny, facetedMethod)
                .or(
                    // else infer from generic type arg if any
                    ()->TypeOfFacet.inferFromMethodReturnType(method, facetedMethod)
                    ));
    }

    void processChoicesFrom(final ProcessMethodContext processMethodContext, final Optional<Action> actionIfAny) {

        val facetedMethod = processMethodContext.getFacetHolder();

        // check for @Action(choicesFrom=...)
        actionIfAny.ifPresent(action->{
            val choicesFrom = action.choicesFrom();
            if(_Strings.isNotEmpty(choicesFrom)) {
                addFacet(new ChoicesFromFacetForActionAnnotation(choicesFrom, facetedMethod));
            }
        });

        addFacetIfPresent(
                LayoutGroupFacetFromActionAnnotation
                .create(actionIfAny, facetedMethod));

    }

    void processFileAccept(final ProcessMethodContext processMethodContext, final Optional<Action> actionIfAny) {

        val holder = processMethodContext.getFacetHolder();

        // check for @Action(fileAccept=...)
        addFacetIfPresent(
                FileAcceptFacetForActionAnnotation
                .create(actionIfAny, holder));
    }


}
