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
import org.apache.isis.applib.annotation.PostsActionInvokedEvent;
import org.apache.isis.applib.annotation.PublishedAction;
import org.apache.isis.applib.annotation.TypeOf;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.applib.services.eventbus.ActionDomainEvent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationAware;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManagerAware;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacetInferredFromArray;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacetInferredFromGenerics;
import org.apache.isis.core.metamodel.facets.actions.bulk.BulkFacet;
import org.apache.isis.core.metamodel.facets.actions.bulk.annotation.BulkFacetAnnotation;
import org.apache.isis.core.metamodel.facets.actions.command.CommandFacet;
import org.apache.isis.core.metamodel.facets.actions.command.annotation.CommandFacetAnnotation;
import org.apache.isis.core.metamodel.facets.actions.command.configuration.ActionConfiguration;
import org.apache.isis.core.metamodel.facets.actions.command.configuration.CommandFacetFromConfiguration;
import org.apache.isis.core.metamodel.facets.actions.interaction.ActionInteractionFacetAbstract;
import org.apache.isis.core.metamodel.facets.actions.interaction.ActionInteractionFacetAnnotation;
import org.apache.isis.core.metamodel.facets.actions.interaction.ActionInteractionFacetDefault;
import org.apache.isis.core.metamodel.facets.actions.interaction.ActionInvocationFacetForActionInteractionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.interaction.ActionInvocationFacetForActionInteractionDefault;
import org.apache.isis.core.metamodel.facets.actions.interaction.ActionInvocationFacetForInteractionAbstract;
import org.apache.isis.core.metamodel.facets.actions.interaction.ActionInvocationFacetForPostsActionInvokedEventAnnotation;
import org.apache.isis.core.metamodel.facets.actions.publish.PublishedActionFacet;
import org.apache.isis.core.metamodel.facets.actions.publish.annotation.PublishedActionFacetAnnotation;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;
import org.apache.isis.core.metamodel.facets.actions.semantics.annotations.actionsemantics.ActionSemanticsFacetAnnotation;
import org.apache.isis.core.metamodel.facets.actions.typeof.annotation.TypeOfFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.typeof.annotation.TypeOfFacetOnActionAnnotation;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContextAware;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistry;

public class ActionAnnotationFacetFactory extends FacetFactoryAbstract implements ServicesInjectorAware, IsisConfigurationAware, AdapterManagerAware, RuntimeContextAware {

    private ServicesInjector servicesInjector;
    private IsisConfiguration configuration;
    private AdapterManager adapterManager;
    private RuntimeContext runtimeContext;

    private final CollectionTypeRegistry collectionTypeRegistry = new CollectionTypeRegistry();

    public ActionAnnotationFacetFactory() {
        super(FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        processInteraction(processMethodContext);
        processHidden(processMethodContext);
        processRestrictTo(processMethodContext);
        processSemantics(processMethodContext);
        processInvokeOn(processMethodContext);

        // must come after processing semantics
        processCommand(processMethodContext);

        // must come after processing semantics
        processPublishing(processMethodContext);

        processTypeOf(processMethodContext);
    }

    private void processInteraction(final ProcessMethodContext processMethodContext) {

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
            // Set up ActionInteractionFacet, which will act as the hiding/disabling/validating advisor
            //
            final Action action = Annotations.getAnnotation(actionMethod, Action.class);
            final ActionInteraction actionInteraction =Annotations.getAnnotation(actionMethod, ActionInteraction.class);
            final Class<? extends ActionDomainEvent<?>> actionInteractionEventType;

            final ActionInteractionFacetAbstract actionInteractionFacet;
            if(action != null && action.domainEvent() != null) {
                actionInteractionEventType = action.domainEvent();
                actionInteractionFacet = new ActionInteractionFacetForActionAnnotation(
                        actionInteractionEventType, servicesInjector, getSpecificationLoader(), holder);
            } else if(actionInteraction != null) {
                actionInteractionEventType = actionInteraction.value();
                actionInteractionFacet = new ActionInteractionFacetAnnotation(
                        actionInteractionEventType, servicesInjector, getSpecificationLoader(), holder);
            } else {
                actionInteractionEventType = ActionDomainEvent.Default.class;
                actionInteractionFacet = new ActionInteractionFacetDefault(
                        actionInteractionEventType, servicesInjector, getSpecificationLoader(), holder);
            }
            FacetUtil.addFacet(actionInteractionFacet);


            final PostsActionInvokedEvent postsActionInvokedEvent = Annotations.getAnnotation(actionMethod, PostsActionInvokedEvent.class);

            final ActionInvocationFacetForInteractionAbstract actionInvocationFacet;
            if (actionInteraction != null) {
                actionInvocationFacet = new ActionInvocationFacetForActionInteractionAnnotation(
                        actionInteractionEventType, actionMethod, typeSpec, returnSpec, actionInteractionFacet, holder, getRuntimeContext(), getAdapterManager(), getServicesInjector());
            } else if (postsActionInvokedEvent != null) {
                actionInvocationFacet = new ActionInvocationFacetForPostsActionInvokedEventAnnotation(
                        postsActionInvokedEvent.value(), actionMethod, typeSpec, returnSpec, actionInteractionFacet, holder, getRuntimeContext(), getAdapterManager(), getServicesInjector());
            } else {
                actionInvocationFacet = new ActionInvocationFacetForActionInteractionDefault(
                        ActionDomainEvent.Default.class, actionMethod, typeSpec, returnSpec, actionInteractionFacet, holder, getRuntimeContext(), getAdapterManager(), getServicesInjector());
            }
            FacetUtil.addFacet(actionInvocationFacet);

        } finally {
            processMethodContext.removeMethod(actionMethod);
        }
    }

    private void processHidden(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Action action = Annotations.getAnnotation(method, Action.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        FacetUtil.addFacet(
                HiddenFacetForActionAnnotation.create(action, holder));
    }

    private void processRestrictTo(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Action action = Annotations.getAnnotation(method, Action.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        FacetUtil.addFacet(
                PrototypeFacetForActionAnnotation.create(action, holder));
    }

    private void processSemantics(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Action action = Annotations.getAnnotation(method, Action.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        ActionSemanticsFacet facet;

        //
        // check for the deprecated @ActionSemantics first, because the
        // @Action(semantics=...) has a default of NON_IDEMPOTENT that would otherwise be used
        //
        final ActionSemantics actionSemantics = Annotations.getAnnotation(method, ActionSemantics.class);
        facet = ActionSemanticsFacetAnnotation.create(actionSemantics, holder);

        // else check for @Action(semantics=...)
        if(facet == null) {
            facet = ActionSemanticsFacetForActionAnnotation.create(action, holder);
        }
        FacetUtil.addFacet(facet);
    }

    private void processInvokeOn(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final Action action = Annotations.getAnnotation(method, Action.class);
        final FacetHolder holder = processMethodContext.getFacetHolder();

        BulkFacet bulkFacet;

        // check for the deprecated @Bulk annotation first
        final Bulk annotation = Annotations.getAnnotation(method, Bulk.class);
        bulkFacet = BulkFacetAnnotation.create(annotation, holder);

        // else check for @Action(invokeOn=...)
        if(bulkFacet == null) {
            bulkFacet = BulkFacetForActionAnnotation.create(action, holder);
        }

        FacetUtil.addFacet(bulkFacet);
    }

    private void processCommand(final ProcessMethodContext processMethodContext) {

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

        final ActionSemanticsFacet actionSemanticsFacet = facetHolder.getFacet(ActionSemanticsFacet.class);
        if(actionSemanticsFacet == null) {
            throw new IllegalStateException("Require ActionSemanticsFacet in order to process");
        }
        if(facetHolder.containsDoOpFacet(CommandFacet.class)) {
            // do not replace
            return;
        }

        CommandFacet commandFacet;

        // check for deprecated @Command annotation first
        final Command annotation = Annotations.getAnnotation(method, Command.class);
        commandFacet = CommandFacetAnnotation.create(annotation, processMethodContext.getFacetHolder());

        // else check for @Action(command=...)
        if(commandFacet == null) {
            commandFacet = CommandFacetForActionAnnotation.create(action, configuration, holder);
        }

        // else check from configuration
        if(commandFacet == null) {
            final ActionConfiguration setting = ActionConfiguration.parse(configuration);
            if(setting == ActionConfiguration.NONE) {
                return;
            }
            if(actionSemanticsFacet.value() == ActionSemantics.Of.SAFE && setting == ActionConfiguration.IGNORE_SAFE) {
                return;
            }

            commandFacet = CommandFacetFromConfiguration.create(facetHolder);
        }

        FacetUtil.addFacet(commandFacet);
    }

    private void processPublishing(final ProcessMethodContext processMethodContext) {

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

        PublishedActionFacet publishedActionFacet;

        // check for deprecated @PublishedAction annotation first
        final PublishedAction annotation = Annotations.getAnnotation(processMethodContext.getMethod(), PublishedAction.class);
        publishedActionFacet = PublishedActionFacetAnnotation.create(annotation, holder);

        // else check for @Action(publishing=...)
        if(publishedActionFacet == null) {
            publishedActionFacet = PublishedActionFacetForActionAnnotation.create(action, configuration, holder);
        }

        FacetUtil.addFacet(publishedActionFacet);
    }


    private void processTypeOf(final ProcessMethodContext processMethodContext) {


        final Method method = processMethodContext.getMethod();
        final Action action = Annotations.getAnnotation(method, Action.class);
        final FacetedMethod holder = processMethodContext.getFacetHolder();

        final Class<?> methodReturnType = method.getReturnType();
        if (!collectionTypeRegistry.isCollectionType(methodReturnType) && !collectionTypeRegistry.isArrayType(methodReturnType)) {
            return;
        }

        final Class<?> returnType = method.getReturnType();
        if (returnType.isArray()) {
            final Class<?> componentType = returnType.getComponentType();
            FacetUtil.addFacet(new TypeOfFacetInferredFromArray(componentType, holder, getSpecificationLoader()));
            return;
        }

        if (action != null) {
            final Class<?> typeOf = action.typeOf();
            if(typeOf != null && typeOf != Object.class) {
                FacetUtil.addFacet(new TypeOfFacetForActionAnnotation(typeOf, getSpecificationLoader(), holder));
                return;
            }
        }

        final TypeOf annotation = Annotations.getAnnotation(method, TypeOf.class);
        if (annotation != null) {
            FacetUtil.addFacet(new TypeOfFacetOnActionAnnotation(annotation.value(), getSpecificationLoader(), holder));
            return;
        }

        final Type type = method.getGenericReturnType();
        if (!(type instanceof ParameterizedType)) {
            return;
        }

        final ParameterizedType methodParameterizedType = (ParameterizedType) type;
        final Type[] methodActualTypeArguments = methodParameterizedType.getActualTypeArguments();
        if (methodActualTypeArguments.length == 0) {
            return;
        }

        final Object methodActualTypeArgument = methodActualTypeArguments[0];
        if (methodActualTypeArgument instanceof Class) {
            final Class<?> actualType = (Class<?>) methodActualTypeArgument;
            FacetUtil.addFacet(new TypeOfFacetInferredFromGenerics(actualType, holder, getSpecificationLoader()));
            return;
        }

        if (methodActualTypeArgument instanceof TypeVariable) {

            TypeVariable<?> methodTypeVariable = (TypeVariable<?>) methodActualTypeArgument;
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
                            Class<?> actualCls = (Class<?>) actualType;
                            FacetUtil.addFacet(new TypeOfFacetInferredFromGenerics(actualCls, holder, getSpecificationLoader()));
                            return;
                        }
                    }
                }
            }

            // TODO: otherwise, what to do?
            return;
        }

    }

    // ///////////////////////////////////////////////////////////////
    // Dependencies
    // ///////////////////////////////////////////////////////////////

    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }

    @Override
    public void setConfiguration(final IsisConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void setAdapterManager(final AdapterManager adapterManager) {
        this.adapterManager = adapterManager;
    }

    private AdapterManager getAdapterManager() {
        return adapterManager;
    }

    private ServicesInjector getServicesInjector() {
        return servicesInjector;
    }

    private RuntimeContext getRuntimeContext() {
        return runtimeContext;
    }

    @Override
    public void setRuntimeContext(RuntimeContext runtimeContext) {
        this.runtimeContext = runtimeContext;
    }

}
