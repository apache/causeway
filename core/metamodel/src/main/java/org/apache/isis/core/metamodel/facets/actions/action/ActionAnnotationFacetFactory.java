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

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionInteraction;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.Bulk;
import org.apache.isis.applib.annotation.Command;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Idempotent;
import org.apache.isis.applib.annotation.PostsActionInvokedEvent;
import org.apache.isis.applib.annotation.Prototype;
import org.apache.isis.applib.annotation.PublishedAction;
import org.apache.isis.applib.annotation.QueryOnly;
import org.apache.isis.applib.annotation.TypeOf;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.applib.services.eventbus.ActionDomainEvent;
import org.apache.isis.applib.services.eventbus.ActionInvokedEvent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacetInferredFromArray;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacetInferredFromGenerics;
import org.apache.isis.core.metamodel.facets.actions.action.bulk.BulkFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.bulk.BulkFacetForBulkAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.bulk.BulkFacetObjectOnly;
import org.apache.isis.core.metamodel.facets.actions.action.command.CommandFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.command.CommandFacetForCommandAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.disabled.DisabledFacetForDisabledAnnotationOnAction;
import org.apache.isis.core.metamodel.facets.actions.action.factorymethod.FactoryMethodFacet;
import org.apache.isis.core.metamodel.facets.actions.action.factorymethod.FactoryMethodFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.hidden.HiddenFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.hidden.HiddenFacetForHiddenAnnotationOnAction;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetAbstract;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetDefault;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetForActionInteractionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventAbstract;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventFromActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventFromActionInteractionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventFromDefault;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForPostsActionInvokedEventAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.prototype.PrototypeFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.prototype.PrototypeFacetForPrototypeAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.publishing.PublishedActionFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.publishing.PublishedActionFacetForPublishedActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.semantics.ActionSemanticsFacetFallbackToNonIdempotent;
import org.apache.isis.core.metamodel.facets.actions.action.semantics.ActionSemanticsFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.semantics.ActionSemanticsFacetForActionSemanticsAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.semantics.ActionSemanticsFacetFromIdempotentAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.semantics.ActionSemanticsFacetFromQueryOnlyAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.typeof.TypeOfFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.typeof.TypeOfFacetOnActionForTypeOfAnnotation;
import org.apache.isis.core.metamodel.facets.actions.bulk.BulkFacet;
import org.apache.isis.core.metamodel.facets.actions.command.CommandFacet;
import org.apache.isis.core.metamodel.facets.actions.prototype.PrototypeFacet;
import org.apache.isis.core.metamodel.facets.actions.publish.PublishedActionFacet;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.CollectionUtils;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForDeprecatedAnnotation;
import org.apache.isis.core.metamodel.util.EventUtil;

public class ActionAnnotationFacetFactory extends FacetFactoryAbstract
            implements MetaModelValidatorRefiner {

    private final MetaModelValidatorForDeprecatedAnnotation actionSemanticsValidator = new MetaModelValidatorForDeprecatedAnnotation(ActionSemantics.class);
    private final MetaModelValidatorForDeprecatedAnnotation actionInteractionValidator = new MetaModelValidatorForDeprecatedAnnotation(ActionInteraction.class);
    private final MetaModelValidatorForDeprecatedAnnotation postsActionInvokedEventValidator = new MetaModelValidatorForDeprecatedAnnotation(PostsActionInvokedEvent.class);
    private final MetaModelValidatorForDeprecatedAnnotation bulkValidator = new MetaModelValidatorForDeprecatedAnnotation(Bulk.class);
    private final MetaModelValidatorForDeprecatedAnnotation commandValidator = new MetaModelValidatorForDeprecatedAnnotation(Command.class);
    private final MetaModelValidatorForDeprecatedAnnotation queryOnlyValidator = new MetaModelValidatorForDeprecatedAnnotation(QueryOnly.class);
    private final MetaModelValidatorForDeprecatedAnnotation idempotentValidator = new MetaModelValidatorForDeprecatedAnnotation(Idempotent.class);
    private final MetaModelValidatorForDeprecatedAnnotation publishedActionValidator = new MetaModelValidatorForDeprecatedAnnotation(PublishedAction.class);
    private final MetaModelValidatorForDeprecatedAnnotation typeOfValidator = new MetaModelValidatorForDeprecatedAnnotation(TypeOf.class);
    private final MetaModelValidatorForDeprecatedAnnotation hiddenValidator = new MetaModelValidatorForDeprecatedAnnotation(Hidden.class);
    private final MetaModelValidatorForDeprecatedAnnotation disabledValidator = new MetaModelValidatorForDeprecatedAnnotation(Disabled.class);
    private final MetaModelValidatorForDeprecatedAnnotation prototypeValidator = new MetaModelValidatorForDeprecatedAnnotation(Prototype.class);



    public ActionAnnotationFacetFactory() {
        super(FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        processInvocation(processMethodContext);
        processHidden(processMethodContext);
        processDisabled(processMethodContext);
        processRestrictTo(processMethodContext);
        processSemantics(processMethodContext);
        processBulk(processMethodContext);

        // must come after processing semantics
        processCommand(processMethodContext);

        // must come after processing semantics
        processPublishing(processMethodContext);

        processTypeOf(processMethodContext);
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
            final PostsActionInvokedEvent postsActionInvokedEvent = Annotations.getAnnotation(actionMethod, PostsActionInvokedEvent.class);
            final ActionInteraction actionInteraction =Annotations.getAnnotation(actionMethod, ActionInteraction.class);
            final Action action = Annotations.getAnnotation(actionMethod, Action.class);
            final Class<? extends ActionDomainEvent<?>> actionDomainEventType;

            final ActionDomainEventFacetAbstract actionDomainEventFacet;


            // can't really do this, because would result in the event being fired for the
            // hidden/disable/validate phases, most likely breaking existing code.

//            // search for @PostsActionInvoked(value=...)
//            if(postsActionInvokedEvent != null) {
//                actionDomainEventType = postsActionInvokedEvent.value();
//                actionDomainEventFacet = new ActionDomainEventFacetForPostsActionInvokedEventAnnotation(
//                        actionDomainEventType, servicesInjector, getSpecificationLoader(), holder);
//            } else

            // search for @ActionInteraction(value=...)
            if(actionInteraction != null) {
                actionDomainEventType = actionInteraction.value();
                actionDomainEventFacet = new ActionDomainEventFacetForActionInteractionAnnotation(
                        actionDomainEventType, servicesInjector, getSpecificationLoader(), holder);
            } else
            // search for @Action(domainEvent=...)
            if(action != null && action.domainEvent() != null) {
                actionDomainEventType = action.domainEvent();
                actionDomainEventFacet = new ActionDomainEventFacetForActionAnnotation(
                        actionDomainEventType, servicesInjector, getSpecificationLoader(), holder);
            } else
            // else use default event type
            {
                actionDomainEventType = ActionDomainEvent.Default.class;
                actionDomainEventFacet = new ActionDomainEventFacetDefault(
                        actionDomainEventType, servicesInjector, getSpecificationLoader(), holder);
            }
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
            // deprecated
            if (postsActionInvokedEvent != null) {
                final Class<? extends ActionInvokedEvent<?>> actionInvokedEventType = postsActionInvokedEvent.value();
                actionInvocationFacet = actionInteractionValidator.flagIfPresent(
                        new ActionInvocationFacetForPostsActionInvokedEventAnnotation(
                                actionInvokedEventType, actionMethod, typeSpec, returnSpec, holder,
                                servicesInjector
                        ), processMethodContext);
            } else
            // deprecated (but more recently)
            if (actionInteraction != null) {
                actionInvocationFacet = actionInteractionValidator.flagIfPresent(
                        new ActionInvocationFacetForDomainEventFromActionInteractionAnnotation(
                                actionDomainEventType, actionMethod, typeSpec, returnSpec, holder,
                                servicesInjector
                        ), processMethodContext);
            } else
            // current
            if (action != null) {
                actionInvocationFacet = new ActionInvocationFacetForDomainEventFromActionAnnotation(
                        actionDomainEventType, actionMethod, typeSpec, returnSpec, holder,
                        servicesInjector
                );
            } else
            // default
            {
                actionInvocationFacet = new ActionInvocationFacetForDomainEventFromDefault(
                        actionDomainEventType, actionMethod, typeSpec, returnSpec, holder,
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

        // check for deprecated @Hidden
        final Hidden hiddenAnnotation = Annotations.getAnnotation(processMethodContext.getMethod(), Hidden.class);
        HiddenFacet facet = hiddenValidator
                .flagIfPresent(HiddenFacetForHiddenAnnotationOnAction.create(hiddenAnnotation, holder),
                        processMethodContext);

        // else search for @Action(hidden=...)
        final Action action = Annotations.getAnnotation(method, Action.class);
        if(facet == null) {
            facet = HiddenFacetForActionAnnotation.create(action, holder);
        }
        FacetUtil.addFacet(facet);
    }

    void processDisabled(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final FacetHolder holder = processMethodContext.getFacetHolder();

        // check for deprecated @Disabled
        final Disabled annotation = Annotations.getAnnotation(method, Disabled.class);
        DisabledFacet facet = disabledValidator
                .flagIfPresent(DisabledFacetForDisabledAnnotationOnAction.create(annotation, holder),
                        processMethodContext);

        // there is no equivalent in @Action(...)

        FacetUtil.addFacet(facet);
    }

    void processRestrictTo(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final FacetHolder holder = processMethodContext.getFacetHolder();

        // check for deprecated @Prototype
        final Prototype annotation = Annotations.getAnnotation(method, Prototype.class);
        final PrototypeFacet facet1 = PrototypeFacetForPrototypeAnnotation.create(annotation, holder,
                getDeploymentCategory());
        FacetUtil.addFacet(prototypeValidator.flagIfPresent(facet1, processMethodContext));
        PrototypeFacet facet = facet1;

        // else search for @Action(restrictTo=...)
        final Action action = Annotations.getAnnotation(method, Action.class);
        if(facet == null) {
            facet = PrototypeFacetForActionAnnotation.create(action, holder,
                    getDeploymentCategory());
        }
        FacetUtil.addFacet(facet);
    }

    void processSemantics(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final FacetHolder holder = processMethodContext.getFacetHolder();

        ActionSemanticsFacet facet;

        // check for the deprecated @QueryOnly...
        final QueryOnly queryOnly = Annotations.getAnnotation(processMethodContext.getMethod(), QueryOnly.class);
        facet = queryOnlyValidator.flagIfPresent(ActionSemanticsFacetFromQueryOnlyAnnotation.create(queryOnly, holder), processMethodContext);

        // else check for the deprecated @Idempotent...
        if(facet == null) {
            final Idempotent idempotent = Annotations.getAnnotation(processMethodContext.getMethod(), Idempotent.class);
            facet = idempotentValidator.flagIfPresent(ActionSemanticsFacetFromIdempotentAnnotation.create(idempotent, holder), processMethodContext);
        }

        // else check for the deprecated @ActionSemantics ...
        if(facet == null) {
            final ActionSemantics actionSemantics = Annotations.getAnnotation(method, ActionSemantics.class);
            facet = actionSemanticsValidator.flagIfPresent(ActionSemanticsFacetForActionSemanticsAnnotation.create(actionSemantics, holder), processMethodContext);
        }

        // else check for @Action(semantics=...)
        if(facet == null) {
            final Action action = Annotations.getAnnotation(method, Action.class);
            facet = ActionSemanticsFacetForActionAnnotation.create(action, holder);
        }

        // else fallback
        if(facet == null) {
            facet = new ActionSemanticsFacetFallbackToNonIdempotent(holder);
        }

        FacetUtil.addFacet(facet);
    }

    void processBulk(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Action action = Annotations.getAnnotation(method, Action.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        BulkFacet facet;

        // check for the deprecated @Bulk annotation first
        final Bulk annotation = Annotations.getAnnotation(method, Bulk.class);
        facet = bulkValidator.flagIfPresent(BulkFacetForBulkAnnotation.create(annotation, holder), processMethodContext);

        // else check for @Action(invokeOn=...)
        if(facet == null) {
            facet = BulkFacetForActionAnnotation.create(action, holder);
        }
        if(facet == null) {
            facet = new BulkFacetObjectOnly(holder);
        }

        FacetUtil.addFacet(facet);
    }

    void processCommand(final ProcessMethodContext processMethodContext) {

        final Class<?> cls = processMethodContext.getCls();
        final Method method = processMethodContext.getMethod();
        final Action action = Annotations.getAnnotation(method, Action.class);
        final FacetedMethod facetHolder = processMethodContext.getFacetHolder();

        final FacetHolder holder = facetHolder;

        //
        // this rule inspired by a similar rule for auditing and publishing, see DomainObjectAnnotationFacetFactory
        //
        if(HasTransactionId.class.isAssignableFrom(processMethodContext.getCls())) {
            // do not install on any implementation of HasTransactionId
            // (ie commands, audit entries, published events).
            return;
        }

        CommandFacet commandFacet;

        // check for deprecated @Command annotation first
        final Command annotation = Annotations.getAnnotation(method, Command.class);
        commandFacet = commandValidator.flagIfPresent(
                CommandFacetForCommandAnnotation.create(annotation, processMethodContext.getFacetHolder()), processMethodContext);

        // else check for @Action(command=...)
        if(commandFacet == null) {
            commandFacet = CommandFacetForActionAnnotation.create(action, getConfiguration(), holder);
        }

        FacetUtil.addFacet(commandFacet);
    }

    void processPublishing(final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();
        final Action action = Annotations.getAnnotation(method, Action.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        //
        // this rule inspired by a similar rule for auditing and publishing, see DomainObjectAnnotationFacetFactory
        // and for commands, see above
        //
        if(HasTransactionId.class.isAssignableFrom(processMethodContext.getCls())) {
            // do not install on any implementation of HasTransactionId
            // (ie commands, audit entries, published events).
            return;
        }

        PublishedActionFacet facet;

        // check for deprecated @PublishedAction annotation first
        final PublishedAction annotation = Annotations.getAnnotation(processMethodContext.getMethod(), PublishedAction.class);
        facet = publishedActionValidator.flagIfPresent(
                PublishedActionFacetForPublishedActionAnnotation.create(annotation, holder), processMethodContext);

        // else check for @Action(publishing=...)
        if(facet == null) {
            facet = PublishedActionFacetForActionAnnotation.create(action, getConfiguration(), holder);
        }

        FacetUtil.addFacet(facet);
    }

    void processFactoryMethod(final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();
        final FacetHolder holder = processMethodContext.getFacetHolder();

        // search for @Action(factoryMethod=...)
        final Action action = Annotations.getAnnotation(method, Action.class);
        FactoryMethodFacet facet = FactoryMethodFacetForActionAnnotation.create(action, method, holder);

        FacetUtil.addFacet(facet);
    }

    void processTypeOf(final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();
        final FacetedMethod holder = processMethodContext.getFacetHolder();

        final Class<?> methodReturnType = method.getReturnType();
        if (!CollectionUtils.isCollectionType(methodReturnType) && !CollectionUtils.isArrayType(methodReturnType)) {
            return;
        }

        TypeOfFacet facet;

        // check for deprecated @TypeOf
        final TypeOf annotation = Annotations.getAnnotation(method, TypeOf.class);
        facet = typeOfValidator.flagIfPresent(
                TypeOfFacetOnActionForTypeOfAnnotation.create(annotation, getSpecificationLoader(), holder), processMethodContext);

        // check for @Action(typeOf=...)
        if(facet == null) {
            final Action action = Annotations.getAnnotation(method, Action.class);
            if (action != null) {
                final Class<?> typeOf = action.typeOf();
                if(typeOf != null && typeOf != Object.class) {
                    facet = new TypeOfFacetForActionAnnotation(typeOf, getSpecificationLoader(), holder);
                }
            }
        }

        // infer from return type
        if(facet == null) {
            final Class<?> returnType = method.getReturnType();
            if (returnType.isArray()) {
                final Class<?> componentType = returnType.getComponentType();
                facet = new TypeOfFacetInferredFromArray(componentType, holder, getSpecificationLoader());
            }
        }

        // infer from generic return type
        if(facet == null) {
            facet = inferFromGenericReturnType(processMethodContext);
        }

        FacetUtil.addFacet(facet);
    }

    private TypeOfFacet inferFromGenericReturnType(
            final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();
        final FacetedMethod holder = processMethodContext.getFacetHolder();

        final Type type = method.getGenericReturnType();
        if (!(type instanceof ParameterizedType)) {
            return null;
        }

        final ParameterizedType methodParameterizedType = (ParameterizedType) type;
        final Type[] methodActualTypeArguments = methodParameterizedType.getActualTypeArguments();

        if (methodActualTypeArguments.length == 0) {
            return null;
        }

        final Object methodActualTypeArgument = methodActualTypeArguments[0];
        if (methodActualTypeArgument instanceof Class) {
            final Class<?> actualType = (Class<?>) methodActualTypeArgument;
            return new TypeOfFacetInferredFromGenerics(actualType, holder, getSpecificationLoader());
        }

        if (methodActualTypeArgument instanceof TypeVariable) {

            final TypeVariable<?> methodTypeVariable = (TypeVariable<?>) methodActualTypeArgument;
            final GenericDeclaration methodGenericClassDeclaration = methodTypeVariable.getGenericDeclaration();

            // try to match up with the actual type argument of the generic superclass.
            final Type genericSuperclass = processMethodContext.getCls().getGenericSuperclass();
            if(genericSuperclass instanceof ParameterizedType) {
                final ParameterizedType parameterizedTypeOfSuperclass = (ParameterizedType)genericSuperclass;
                if(parameterizedTypeOfSuperclass.getRawType() == methodGenericClassDeclaration) {
                    final Type[] genericSuperClassActualTypeArguments = parameterizedTypeOfSuperclass.getActualTypeArguments();
                    // simplification: if there's just one, then use it.
                    if(methodActualTypeArguments.length == 1) {
                        final Type actualType = genericSuperClassActualTypeArguments[0];
                        if(actualType instanceof Class) {
                            // just being safe
                            final Class<?> actualCls = (Class<?>) actualType;
                            return new TypeOfFacetInferredFromGenerics(actualCls, holder, getSpecificationLoader());
                        }
                    }
                }
            }
            // otherwise, what to do?
        }
        return null;
    }

    // ///////////////////////////////////////////////////////////////

    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator, final IsisConfiguration configuration) {
        metaModelValidator.add(actionSemanticsValidator);
        metaModelValidator.add(actionInteractionValidator);
        metaModelValidator.add(postsActionInvokedEventValidator);
        metaModelValidator.add(bulkValidator);
        metaModelValidator.add(commandValidator);
        metaModelValidator.add(queryOnlyValidator);
        metaModelValidator.add(idempotentValidator);
        metaModelValidator.add(publishedActionValidator);
        metaModelValidator.add(typeOfValidator);
        metaModelValidator.add(hiddenValidator);
        metaModelValidator.add(disabledValidator);
        metaModelValidator.add(prototypeValidator);
    }

    // ///////////////////////////////////////////////////////////////


    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        super.setServicesInjector(servicesInjector);
        final IsisConfiguration configuration = getConfiguration();

        actionSemanticsValidator.setConfiguration(configuration);
        actionInteractionValidator.setConfiguration(configuration);
        postsActionInvokedEventValidator.setConfiguration(configuration);
        bulkValidator.setConfiguration(configuration);
        commandValidator.setConfiguration(configuration);
        queryOnlyValidator.setConfiguration(configuration);
        idempotentValidator.setConfiguration(configuration);
        publishedActionValidator.setConfiguration(configuration);
        typeOfValidator.setConfiguration(configuration);
        hiddenValidator.setConfiguration(configuration);
        disabledValidator.setConfiguration(configuration);
        prototypeValidator.setConfiguration(configuration);
    }


}
