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

import java.lang.reflect.Method;
import java.util.List;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.services.HasUniqueId;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Collections;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.actions.action.associateWith.AssociatedWithFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.command.CommandFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.hidden.HiddenFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetAbstract;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetDefault;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventAbstract;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventFromActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventFromDefault;
import org.apache.isis.core.metamodel.facets.actions.action.prototype.PrototypeFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.publishing.PublishedActionFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.semantics.ActionSemanticsFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.typeof.TypeOfFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.command.CommandFacet;
import org.apache.isis.core.metamodel.facets.actions.prototype.PrototypeFacet;
import org.apache.isis.core.metamodel.facets.actions.publish.PublishedActionFacet;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.members.order.annotprop.MemberOrderFacetForActionAnnotation;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.util.EventUtil;

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

        final Method actionMethod = processMethodContext.getMethod();

        try {
            final Class<?> returnType = actionMethod.getReturnType();
            final ObjectSpecification returnSpec = getSpecificationLoader().loadSpecification(returnType);
            if (returnSpec == null) {
                return;
            }

            final Class<?> cls = processMethodContext.getCls();
            final ObjectSpecification typeSpec = getSpecificationLoader().loadSpecification(cls);
            final FacetHolder holder = processMethodContext.getFacetHolder();

            //
            // Set up ActionDomainEventFacet, which will act as the hiding/disabling/validating advisor
            //
            final List<Action> actions = Annotations.getAnnotations(actionMethod, Action.class);

            // search for @Action(domainEvent=...), else use the default event type
            final ActionDomainEventFacetAbstract actionDomainEventFacet =
                    actions.stream()
                    .map(Action::domainEvent)
                    .filter(domainEvent -> domainEvent != ActionDomainEvent.Default.class)
                    .findFirst()
                    .map(domainEvent ->
                    (ActionDomainEventFacetAbstract) new ActionDomainEventFacetForActionAnnotation(
                            domainEvent, servicesInjector, getSpecificationLoader(), holder))
                    .orElse(
                            new ActionDomainEventFacetDefault(
                                    ActionDomainEvent.Default.class, servicesInjector, getSpecificationLoader(), holder)
                            );

            if(EventUtil.eventTypeIsPostable(
                    actionDomainEventFacet.getEventType(),
                    ActionDomainEvent.Noop.class,
                    ActionDomainEvent.Default.class,
                    "isis.reflector.facet.actionAnnotation.domainEvent.postForDefault", getConfiguration())) {
                FacetUtil.addFacet(actionDomainEventFacet);
            }

            // replace the current actionInvocationFacet with one that will
            // emit the appropriate domain event and then delegate onto the underlying

            final ActionInvocationFacetForDomainEventAbstract actionInvocationFacet;
            if (actionDomainEventFacet instanceof ActionDomainEventFacetForActionAnnotation) {
                actionInvocationFacet = new ActionInvocationFacetForDomainEventFromActionAnnotation(
                        actionDomainEventFacet.getEventType(), actionMethod, typeSpec, returnSpec, holder,
                        servicesInjector
                        );
            } else
                // default
            {
                actionInvocationFacet = new ActionInvocationFacetForDomainEventFromDefault(
                        actionDomainEventFacet.getEventType(), actionMethod, typeSpec, returnSpec, holder,
                        servicesInjector
                        );
            }
            FacetUtil.addFacet(actionInvocationFacet);

        } finally {
            processMethodContext.removeMethod(actionMethod);
        }
    }

    void processHidden(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final FacetHolder holder = processMethodContext.getFacetHolder();

        // search for @Action(hidden=...)
        final List<Action> actions = Annotations.getAnnotations(method, Action.class);
        HiddenFacet facet = HiddenFacetForActionAnnotation.create(actions, holder);
        FacetUtil.addFacet(facet);
    }

    void processRestrictTo(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final FacetHolder holder = processMethodContext.getFacetHolder();

        // search for @Action(restrictTo=...)
        final List<Action> actions = Annotations.getAnnotations(method, Action.class);
        PrototypeFacet facet = PrototypeFacetForActionAnnotation.create(actions, holder,
                _Context.getEnvironment().getDeploymentType());

        FacetUtil.addFacet(facet);
    }

    void processSemantics(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final FacetHolder holder = processMethodContext.getFacetHolder();

        // check for @Action(semantics=...)
        final List<Action> actions = Annotations.getAnnotations(method, Action.class);
        ActionSemanticsFacet facet =
                ActionSemanticsFacetForActionAnnotation.create(actions, holder);

        FacetUtil.addFacet(facet);
    }

    void processCommand(final ProcessMethodContext processMethodContext) {

        //final Class<?> cls = processMethodContext.getCls();
        final Method method = processMethodContext.getMethod();
        final List<Action> actions = Annotations.getAnnotations(method, Action.class);
        final FacetedMethod facetHolder = processMethodContext.getFacetHolder();

        final FacetHolder holder = facetHolder;

        //
        // this rule inspired by a similar rule for auditing and publishing, see DomainObjectAnnotationFacetFactory
        //
        if(HasUniqueId.class.isAssignableFrom(processMethodContext.getCls())) {
            // do not install on any implementation of HasTransactionId
            // (ie commands, audit entries, published events).
            return;
        }

        // check for @Action(command=...)
        CommandFacet commandFacet = CommandFacetForActionAnnotation.create(actions, getConfiguration(), servicesInjector, holder);

        FacetUtil.addFacet(commandFacet);
    }

    void processPublishing(final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();
        final List<Action> actions = Annotations.getAnnotations(method, Action.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

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
        PublishedActionFacet facet = PublishedActionFacetForActionAnnotation.create(actions, getConfiguration(), holder);

        FacetUtil.addFacet(facet);
    }

    void processTypeOf(final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();
        final FacetedMethod holder = processMethodContext.getFacetHolder();

        final Class<?> methodReturnType = method.getReturnType();
        if (!_Collections.isCollectionOrArrayType(methodReturnType)) {
            return;
        }

        // check for @Action(typeOf=...)
        final List<Action> actions = Annotations.getAnnotations(method, Action.class);
        TypeOfFacet typeOfFacet = actions.stream()
                .map(Action::typeOf)
                .filter(typeOf -> typeOf != null && typeOf != Object.class)
                .findFirst()
                .map(typeOf -> new TypeOfFacetForActionAnnotation(typeOf, getSpecificationLoader(), holder))
                .orElse(null);

        // infer from return type
        if(typeOfFacet == null) {
            final Class<?> returnType = method.getReturnType();
            typeOfFacet = TypeOfFacet.Util.inferFromArrayType(holder, returnType, getSpecificationLoader());
        }

        // infer from generic return type
        if(typeOfFacet == null) {
            final Class<?> cls = processMethodContext.getCls();
            typeOfFacet = TypeOfFacet.Util.inferFromGenericReturnType(cls, method, holder,
                    getSpecificationLoader());
        }

        FacetUtil.addFacet(typeOfFacet);
    }

    void processAssociateWith(final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();
        final FacetedMethod holder = processMethodContext.getFacetHolder();

        // check for @Action(associateWith=...)

        final List<Action> actions = Annotations.getAnnotations(method, Action.class);
        final Action action = actions.isEmpty() ? null : actions.get(0);
        if (action != null) {
            final String associateWith = action.associateWith();
            if(!_Strings.isNullOrEmpty(associateWith)) {
                final String associateWithSequence = action.associateWithSequence();
                FacetUtil.addFacet(
                        new MemberOrderFacetForActionAnnotation(associateWith, associateWithSequence, holder));
                FacetUtil.addFacet(
                        new AssociatedWithFacetForActionAnnotation(associateWith, holder));
            }
        }

    }


}
