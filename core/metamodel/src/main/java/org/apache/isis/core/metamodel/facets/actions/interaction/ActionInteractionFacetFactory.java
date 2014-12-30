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

package org.apache.isis.core.metamodel.facets.actions.interaction;

import java.lang.reflect.Method;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionInteraction;
import org.apache.isis.applib.annotation.PostsActionInvokedEvent;
import org.apache.isis.applib.services.eventbus.ActionInteractionEvent;
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManagerAware;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.actions.action.ActionInteractionFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.debug.DebugFacet;
import org.apache.isis.core.metamodel.facets.actions.exploration.ExplorationFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacetInferred;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContextAware;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

/**
 * Sets up {@link ActionInteractionFacet} and also an {@link ActionInvocationFacet}, along with a number of supporting
 * facets that are based on the action's name.
 * 
 * <p>
 * The supporting methods are: {@link ExplorationFacet}
 * and {@link DebugFacet}. In addition a {@link NamedFacet} is inferred from the
 * name (taking into account the above well-known prefixes).
 */
public class ActionInteractionFacetFactory extends MethodPrefixBasedFacetFactoryAbstract implements AdapterManagerAware, ServicesInjectorAware, RuntimeContextAware {

    private static final String EXPLORATION_PREFIX = "Exploration";
    private static final String DEBUG_PREFIX = "Debug";

    private static final String[] PREFIXES = { EXPLORATION_PREFIX, DEBUG_PREFIX };

    private AdapterManager adapterManager;
    private ServicesInjector servicesInjector;
    private RuntimeContext runtimeContext;

    /**
     * Note that the {@link Facet}s registered are the generic ones from
     * noa-architecture (where they exist)
     */
    public ActionInteractionFacetFactory() {
        super(FeatureType.ACTIONS_ONLY, OrphanValidation.VALIDATE, PREFIXES);
    }

    // ///////////////////////////////////////////////////////
    // Actions
    // ///////////////////////////////////////////////////////

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        // InvocationFacet
        attachInvocationFacet(processMethodContext);

        // DebugFacet, ExplorationFacet
        attachDebugFacetIfActionMethodNamePrefixed(processMethodContext);
        attachExplorationFacetIfActionMethodNamePrefixed(processMethodContext);

        // inferred name
        // (must be called after the attachinvocationFacet methods)
        attachNamedFacetInferredFromMethodName(processMethodContext); 
    }

    private void attachInvocationFacet(final ProcessMethodContext processMethodContext) {

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
            final Class<? extends ActionInteractionEvent<?>> actionInteractionEventType;

            final ActionInteractionFacetAbstract actionInteractionFacet;
            if(action != null && action.interaction() != null) {
                actionInteractionEventType = action.interaction();
                actionInteractionFacet = new ActionInteractionFacetForActionAnnotation(
                        actionInteractionEventType, servicesInjector, getSpecificationLoader(), holder);
            } else if(actionInteraction != null) {
                actionInteractionEventType = actionInteraction.value();
                actionInteractionFacet = new ActionInteractionFacetAnnotation(
                        actionInteractionEventType, servicesInjector, getSpecificationLoader(), holder);
            } else {
                actionInteractionEventType = ActionInteractionEvent.Default.class;
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
                        ActionInteractionEvent.Default.class, actionMethod, typeSpec, returnSpec, actionInteractionFacet, holder, getRuntimeContext(), getAdapterManager(), getServicesInjector());
            }
            FacetUtil.addFacet(actionInvocationFacet);

        } finally {
            processMethodContext.removeMethod(actionMethod);
        }
    }

    private void attachDebugFacetIfActionMethodNamePrefixed(final ProcessMethodContext processMethodContext) {
        final Method actionMethod = processMethodContext.getMethod();
        final String capitalizedName = StringExtensions.asCapitalizedName(actionMethod.getName());
        if (!capitalizedName.startsWith(DEBUG_PREFIX)) {
            return;
        }
        final FacetHolder facetedMethod = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new DebugFacetViaNamingConvention(facetedMethod));
    }

    private void attachExplorationFacetIfActionMethodNamePrefixed(final ProcessMethodContext processMethodContext) {

        final Method actionMethod = processMethodContext.getMethod();
        final String capitalizedName = StringExtensions.asCapitalizedName(actionMethod.getName());
        if (!capitalizedName.startsWith(EXPLORATION_PREFIX)) {
            return;
        }
        final FacetHolder facetedMethod = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new ExplorationFacetViaNamingConvention(facetedMethod));
    }

    /**
     * Must be called after added the debug, exploration etc facets.
     * 
     * <p>
     * TODO: remove this hack
     */
    private void attachNamedFacetInferredFromMethodName(final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();
        final String capitalizedName = StringExtensions.asCapitalizedName(method.getName());

        // this is nasty...
        String name = capitalizedName;
        name = StringExtensions.removePrefix(name, DEBUG_PREFIX);
        name = StringExtensions.removePrefix(name, EXPLORATION_PREFIX);
        name = StringExtensions.asNaturalName2(name);

        final FacetHolder facetedMethod = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new NamedFacetInferred(name, facetedMethod));
    }

    // ///////////////////////////////////////////////////////////////
    // Dependencies
    // ///////////////////////////////////////////////////////////////

    @Override
    public void setAdapterManager(final AdapterManager adapterManager) {
        this.adapterManager = adapterManager;
    }

    private AdapterManager getAdapterManager() {
        return adapterManager;
    }

    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
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
