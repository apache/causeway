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
package org.apache.causeway.core.metamodel.facets.actions.action;

import java.util.Optional;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.mixins.system.HasInteractionId;
import org.apache.causeway.commons.semantics.CollectionSemantics;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.causeway.core.metamodel.facets.actions.action.choicesfrom.ChoicesFromFacetForActionAnnotation;
import org.apache.causeway.core.metamodel.facets.actions.action.explicit.ActionExplicitFacetForActionAnnotation;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacet;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForAction;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForMixedInPropertyOrCollection;
import org.apache.causeway.core.metamodel.facets.actions.action.prototype.PrototypeFacetForActionAnnotation;
import org.apache.causeway.core.metamodel.facets.actions.action.semantics.ActionSemanticsFacetForActionAnnotation;
import org.apache.causeway.core.metamodel.facets.actions.action.typeof.TypeOfFacetForActionAnnotation;
import org.apache.causeway.core.metamodel.facets.actions.fileaccept.FileAcceptFacetForActionAnnotation;
import org.apache.causeway.core.metamodel.facets.members.layout.group.LayoutGroupFacetForActionAnnotation;
import org.apache.causeway.core.metamodel.facets.members.publish.command.CommandPublishingFacetForActionAnnotation;
import org.apache.causeway.core.metamodel.facets.members.publish.execution.ExecutionPublishingFacetForActionAnnotation;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailureUtils;

public class ActionAnnotationFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public ActionAnnotationFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        var actionIfAny = actionIfAny(processMethodContext);

        processExplicit(processMethodContext, actionIfAny);
        processDomainEvent(processMethodContext, actionIfAny);
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

    Optional<Action> actionIfAny(final ProcessMethodContext processMethodContext) {
        return processMethodContext
                .synthesizeOnMethodOrMixinType(
                        Action.class,
                        () -> ValidationFailureUtils
                        .raiseAmbiguousMixinAnnotations(processMethodContext.getFacetHolder(), Action.class));
    }

    void processExplicit(final ProcessMethodContext processMethodContext, final Optional<Action> actionIfAny) {
        var holder = processMethodContext.getFacetHolder();

        // check for @Action at all.
        addFacetIfPresent(
                ActionExplicitFacetForActionAnnotation
                .create(actionIfAny, holder));
    }

    void processDomainEvent(final ProcessMethodContext processMethodContext, final Optional<Action> actionIfAny) {

        var actionMethod = processMethodContext.getMethod();

        final boolean isAction = !processMethodContext.isMixinMain()
                || actionIfAny.isPresent();

        try {

            var typeSpec = getSpecificationLoader().loadSpecification(processMethodContext.getCls());
            if(typeSpec==null) {
                return;
            }

            var returnType = actionMethod.getReturnType();
            var returnSpec = getSpecificationLoader().loadSpecification(returnType);
            if (returnSpec == null) {
                return;
            }

            var holder = processMethodContext.getFacetHolder();

            //
            // Set up ActionDomainEventFacet, which will act as the hiding/disabling/validating advisor
            //

            // search for @Action(domainEvent=...), else use the default event type
            var actionDomainEventFacet = ActionDomainEventFacet.create(actionIfAny, typeSpec, holder);
            addFacet(actionDomainEventFacet);

            // replace the current actionInvocationFacet with one that will
            // emit the appropriate domain event and then delegate onto the underlying
            addFacet(
                /* lazily binds the event-type to the actionDomainEventFacet,
                 * such that any changes to the latter during post processing
                 * are reflected here as well
                 */
                isAction
                    ? new ActionInvocationFacetForAction(
                            actionDomainEventFacet,
                            actionMethod, typeSpec, returnSpec, holder)
                    // when in a mixed-in prop/coll situation, the prop/coll event-type must be used instead
                    : new ActionInvocationFacetForMixedInPropertyOrCollection(
                            actionMethod, typeSpec, returnSpec, holder));
        } finally {
            processMethodContext.removeMethod(actionMethod.asMethodForIntrospection());
        }
    }

    void processRestrictTo(final ProcessMethodContext processMethodContext, final Optional<Action> actionIfAny) {
        var facetedMethod = processMethodContext.getFacetHolder();

        // search for @Action(restrictTo=...)
        addFacetIfPresent(
                PrototypeFacetForActionAnnotation
                .create(
                        actionIfAny, facetedMethod,
                        ()->super.getSystemEnvironment().getDeploymentType()));
    }

    void processSemantics(final ProcessMethodContext processMethodContext, final Optional<Action> actionIfAny) {
        var facetedMethod = processMethodContext.getFacetHolder();

        // check for @Action(semantics=...)
        addFacet(
                ActionSemanticsFacetForActionAnnotation
                .create(actionIfAny, facetedMethod));
    }

    void processCommandPublishing(
            final ProcessMethodContext processMethodContext,
            final Optional<Action> actionIfAny) {

        var facetedMethod = processMethodContext.getFacetHolder();

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

        var facetedMethod = processMethodContext.getFacetHolder();

        //
        // this rule inspired by a similar rule for auditing and publishing,
        // see DomainObjectAnnotationFacetFactory
        // and for commands, see above
        //
        if(HasInteractionId.class.isAssignableFrom(processMethodContext.getCls())) {
            // do not install on any implementation of HasInteractionId
            // (ie commands, audit entries, published events).
            return;
        }

        // check for @Action(executionPublishing=...)
        addFacetIfPresent(
                ExecutionPublishingFacetForActionAnnotation
                .create(actionIfAny, getConfiguration(), facetedMethod));

    }

    void processTypeOf(final ProcessMethodContext processMethodContext, final Optional<Action> actionIfAny) {

        var method = processMethodContext.getMethod();
        var facetedMethod = processMethodContext.getFacetHolder();

        var methodReturnType = method.getReturnType();

        CollectionSemantics.valueOf(methodReturnType)
        .ifPresent(collectionType->{
            addFacetIfPresent(
                    TypeOfFacetForActionAnnotation.create(actionIfAny, collectionType, facetedMethod)
                    .or(
                        // else infer from generic type arg if any
                        ()->TypeOfFacet.inferFromMethodReturnType(method, facetedMethod)
                        ));

        });
    }

    void processChoicesFrom(final ProcessMethodContext processMethodContext, final Optional<Action> actionIfAny) {

        var holder = processMethodContext.getFacetHolder();

        // check for @Action(choicesFrom=...)
        addFacetIfPresent(
                ChoicesFromFacetForActionAnnotation
                .create(actionIfAny, holder));

        addFacetIfPresent(
                LayoutGroupFacetForActionAnnotation
                .create(actionIfAny, holder));

    }

    void processFileAccept(final ProcessMethodContext processMethodContext, final Optional<Action> actionIfAny) {

        var holder = processMethodContext.getFacetHolder();

        // check for @Action(fileAccept=...)
        addFacetIfPresent(
                FileAcceptFacetForActionAnnotation
                .create(actionIfAny, holder));
    }

}
