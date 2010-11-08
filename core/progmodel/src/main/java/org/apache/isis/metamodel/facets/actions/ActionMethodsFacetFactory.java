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


package org.apache.isis.metamodel.facets.actions;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.apache.isis.applib.security.UserMemento;
import org.apache.isis.core.metamodel.exceptions.ReflectionException;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.core.metamodel.util.InvokeUtils;
import org.apache.isis.core.metamodel.util.NameUtils;
import org.apache.isis.metamodel.facets.Facet;
import org.apache.isis.metamodel.facets.FacetHolder;
import org.apache.isis.metamodel.facets.FacetUtil;
import org.apache.isis.metamodel.facets.MethodRemover;
import org.apache.isis.metamodel.facets.actions.choices.ActionChoicesFacetViaMethod;
import org.apache.isis.metamodel.facets.actions.choices.ActionParameterChoicesFacetViaMethod;
import org.apache.isis.metamodel.facets.actions.defaults.ActionDefaultsFacetViaMethod;
import org.apache.isis.metamodel.facets.actions.defaults.ActionParameterDefaultsFacetViaMethod;
import org.apache.isis.metamodel.facets.actions.executed.ExecutedFacet.Where;
import org.apache.isis.metamodel.facets.actions.invoke.ActionInvocationFacetViaMethod;
import org.apache.isis.metamodel.facets.actions.validate.ActionValidationFacetViaMethod;
import org.apache.isis.metamodel.facets.naming.named.NamedFacetInferred;
import org.apache.isis.metamodel.java5.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.runtimecontext.RuntimeContextAware;
import org.apache.isis.metamodel.specloader.internal.peer.JavaObjectActionPeer;
import org.apache.isis.metamodel.specloader.internal.peer.ObjectActionParamPeer;


/**
 * Sets up all the {@link Facet}s for an action in a single shot.
 * 
 * <p>
 * TODO: should be more fine-grained?
 */
public class ActionMethodsFacetFactory extends MethodPrefixBasedFacetFactoryAbstract implements RuntimeContextAware {

    private static final String EXPLORATION_PREFIX = "Exploration";
    private static final String DEBUG_PREFIX = "Debug";
    private static final String REMOTE_PREFIX = "Remote";
    private static final String LOCAL_PREFIX = "Local";

    private static final String PARAMETER_NAMES_PREFIX = "names";
    private static final String PARAMETER_DESCRIPTIONS_PREFIX = "descriptions";
    private static final String PARAMETER_OPTIONAL_PREFIX = "optional";
    private static final String PARAMETER_DEFAULTS_PREFIX = "default";
    private static final String PARAMETER_CHOICES_PREFIX = "choices";

    private static final String[] PREFIXES = { EXPLORATION_PREFIX, DEBUG_PREFIX, REMOTE_PREFIX, LOCAL_PREFIX,
            PARAMETER_NAMES_PREFIX, PARAMETER_DESCRIPTIONS_PREFIX, PARAMETER_OPTIONAL_PREFIX, PARAMETER_DEFAULTS_PREFIX,
            PARAMETER_CHOICES_PREFIX, };
    
	private RuntimeContext runtimeContext;

    /**
     * Note that the {@link Facet}s registered are the generic ones from noa-architecture (where they exist)
     */
    public ActionMethodsFacetFactory() {
        super(PREFIXES, ObjectFeatureType.ACTIONS_ONLY);
    }

    // ///////////////////////////////////////////////////////
    // Actions
    // ///////////////////////////////////////////////////////

    @Override
    public boolean process(Class<?> cls, final Method actionMethod, final MethodRemover methodRemover, final FacetHolder action) {

        final String capitalizedName = NameUtils.capitalizeName(actionMethod.getName());
        final Class<?> returnType = actionMethod.getReturnType();
        final Class<?>[] paramTypes = actionMethod.getParameterTypes();

        final List<Facet> facets = new ArrayList<Facet>();

        final ObjectSpecification typeSpec = getSpecificationLoader().loadSpecification(cls);
        final ObjectSpecification returnSpec = getSpecificationLoader().loadSpecification(returnType.getName());
        if (returnSpec != null) {
            facets.add(new ActionInvocationFacetViaMethod(actionMethod, typeSpec, returnSpec, action, getRuntimeContext()));
            checkForDebugPrefix(facets, capitalizedName, action);
            checkForExplorationPrefix(facets, capitalizedName, action);
            checkForExecutionLocationPrefix(facets, capitalizedName, action);
        }

        removeMethod(methodRemover, actionMethod);

        final boolean forClass = (actionMethod.getModifiers() & Modifier.STATIC) > 0;
        findAndRemoveValidMethod(facets, methodRemover, cls, forClass, capitalizedName, returnType, paramTypes, action);
        boolean oldChoicesOrDefaultsMethodsUsed = findAndRemoveParametersDefaultsMethod(facets, methodRemover, cls, forClass,
                capitalizedName, returnType, paramTypes, action);
        oldChoicesOrDefaultsMethodsUsed = findAndRemoveParametersChoicesMethod(facets, methodRemover, cls, forClass,
                capitalizedName, returnType, paramTypes, action)
                || oldChoicesOrDefaultsMethodsUsed;

        defaultNamedFacet(facets, methodRemover, capitalizedName, action); // must be called after the checkForXxxPrefix methods
        findAndRemoveNameMethod(facets, methodRemover, cls, capitalizedName, new Class[] {}, action);
        findAndRemoveDescriptionMethod(facets, methodRemover, cls, capitalizedName, new Class[] {}, action);

        findAndRemoveAlwaysHideMethod(facets, methodRemover, cls, capitalizedName, paramTypes, action);
        findAndRemoveProtectMethod(facets, methodRemover, cls, capitalizedName, paramTypes, action);

        findAndRemoveHideForSessionMethod(facets, methodRemover, cls, capitalizedName, UserMemento.class, action);
        findAndRemoveDisableForSessionMethod(facets, methodRemover, cls, capitalizedName, UserMemento.class, action);
        findAndRemoveHideMethod(facets, methodRemover, cls, forClass, capitalizedName, paramTypes, action);
        findAndRemoveDisableMethod(facets, methodRemover, cls, forClass, capitalizedName, paramTypes, action);

        if (action instanceof JavaObjectActionPeer) {
            final JavaObjectActionPeer javaObjectActionPeer = (JavaObjectActionPeer) action;
            // process the action's parameters names, descriptions and optional
            // an alternative design would be to have another facet factory processing just ACTION_PARAMETER,
            // and have it remove these
            // supporting methods. However, the FacetFactory API doesn't allow for methods of the class to be
            // removed while processing
            // action parameters, only while processing Methods (ie actions)
            final ObjectActionParamPeer[] actionParameters = javaObjectActionPeer.getParameters();

            findAndRemoveOptionalForActionParametersMethod(methodRemover, cls, capitalizedName, returnType, paramTypes,
                    actionParameters);
            findAndRemoveNamesForActionParametersMethod(methodRemover, cls, capitalizedName, returnType, paramTypes,
                    actionParameters);
            findAndRemoveDescriptionsforActionParametersMethod(methodRemover, cls, capitalizedName, returnType, paramTypes,
                    actionParameters);

            findAndRemoveChoicesForActionParametersMethod(oldChoicesOrDefaultsMethodsUsed, methodRemover, cls, capitalizedName,
                    paramTypes, actionParameters);
            findAndRemoveDefaultForActionParametersMethod(oldChoicesOrDefaultsMethodsUsed, methodRemover, cls, capitalizedName,
                    paramTypes, actionParameters);
        }
        return FacetUtil.addFacets(facets);
    }

    private void checkForExecutionLocationPrefix(final List<Facet> actionFacets, final String capitalizedName, final FacetHolder action) {
        if (capitalizedName.startsWith(LOCAL_PREFIX)) {
            actionFacets.add(new ExecutedFacetViaNamingConvention(Where.LOCALLY, action));
        } else if (capitalizedName.startsWith(REMOTE_PREFIX)) {
            actionFacets.add(new ExecutedFacetViaNamingConvention(Where.REMOTELY, action));
        }
    }

    private void checkForDebugPrefix(final List<Facet> actionFacets, final String capitalizedName, final FacetHolder action) {
        if (capitalizedName.startsWith(DEBUG_PREFIX)) {
            actionFacets.add(new DebugFacetViaNamingConvention(action));
        }
    }

    private void checkForExplorationPrefix(final List<Facet> facets, final String capitalizedName, final FacetHolder action) {
        if (capitalizedName.startsWith(EXPLORATION_PREFIX)) {
            facets.add(new ExplorationFacetViaNamingConvention(action));
        }
    }

    /**
     * Must be called after the checkForXxxPrefix methods.
     */
    private void defaultNamedFacet(
            final List<Facet> actionFacets,
            final MethodRemover methodRemover,
            final String capitalizedName,
            final FacetHolder action) {
        String name = removePrefix(capitalizedName, LOCAL_PREFIX);
        name = removePrefix(name, REMOTE_PREFIX);
        name = removePrefix(name, DEBUG_PREFIX);
        name = removePrefix(name, EXPLORATION_PREFIX);
        name = removePrefix(name, LOCAL_PREFIX);
        name = removePrefix(name, REMOTE_PREFIX);
        name = NameUtils.naturalName(name);
        actionFacets.add(new NamedFacetInferred(name, action));
    }

    private void findAndRemoveValidMethod(
            final List<Facet> actionFacets,
            final MethodRemover methodRemover,
            final Class<?> cls,
            final boolean onClass,
            final String capitalizedName,
            final Class<?> returnType,
            final Class<?>[] params,
            final FacetHolder action) {
        final Method method = findMethod(cls, onClass, VALIDATE_PREFIX + capitalizedName, String.class, params);
        if (method == null) {
            return;
        }
        removeMethod(methodRemover, method);
        actionFacets.add(new ActionValidationFacetViaMethod(method, action));
    }

    private boolean findAndRemoveParametersDefaultsMethod(
            final List<Facet> actionFacets,
            final MethodRemover methodRemover,
            final Class<?> cls,
            final boolean onClass,
            final String capitalizedName,
            final Class<?> returnType,
            final Class<?>[] params,
            final FacetHolder action) {
        if (params.length == 0) {
            return false;
        }

        Method method = null;
        String name = PARAMETER_DEFAULTS_PREFIX + capitalizedName;
        if (allParametersOfSameType(params)) {
            final Object array = Array.newInstance(params[0], 0);
            final Class<?> classes = array.getClass();
            method = findMethodWithOrWithoutParameters(cls, onClass, name, classes, params);
            removeMethod(methodRemover, method);
        }
        if (method == null) {
            method = findMethodWithOrWithoutParameters(cls, onClass, name, Object[].class, params);
            removeMethod(methodRemover, method);
        }
        if (method == null) {
            method = findMethodWithOrWithoutParameters(cls, onClass, name, List.class,  params);
            removeMethod(methodRemover, method);
        }

        if (method == null) {
            return false;
        }
        actionFacets.add(new ActionDefaultsFacetViaMethod(method, action));
        return true;
    }

    private boolean findAndRemoveParametersChoicesMethod(
            final List<Facet> actionFacets,
            final MethodRemover methodRemover,
            final Class<?> cls,
            final boolean onClass,
            final String capitalizedName,
            final Class<?> returnType,
            final Class<?>[] params,
            final FacetHolder action) {
        if (params.length <= 0) {
            return false;
        }

        Method method = null;
        String name = PARAMETER_CHOICES_PREFIX + capitalizedName;
        if (allParametersOfSameType(params)) {
            final Object array = Array.newInstance(params[0], new int[] { 0, 0 });
            final Class<?> classes = array.getClass();
            method = findMethodWithOrWithoutParameters(cls, onClass, name, classes, params);
            removeMethod(methodRemover, method);
        }
        if (method == null) {
            method = findMethodWithOrWithoutParameters(cls, onClass, name, Object[].class, params);
            removeMethod(methodRemover, method);
        }
        if (method == null) {
            method = findMethodWithOrWithoutParameters(cls, onClass, name, List.class, params);
            removeMethod(methodRemover, method);
        }

        if (method == null) {
            return false;
        }

        actionFacets.add(new ActionChoicesFacetViaMethod(method, returnType, action, getSpecificationLoader(), getRuntimeContext()));
        return true;
    }

    private void findAndRemoveOptionalForActionParametersMethod(
            final MethodRemover methodRemover,
            final Class<?> cls,
            final String capitalizedName,
            final Class<?> returnType,
            final Class<?>[] params,
            final FacetHolder[] parameters) {
        if (params.length == 0) {
            return;
        }

        final Method method = findMethodWithOrWithoutParameters(cls, CLASS, PARAMETER_OPTIONAL_PREFIX + capitalizedName,
                boolean[].class, params);
        if (method == null) {
            return;
        }
        removeMethod(methodRemover, method);

        final Object[] parameterObjects = new Object[method.getParameterTypes().length];
        final boolean[] names = (boolean[]) InvokeUtils.invokeStatic(method, parameterObjects);
        for (int i = 0; i < names.length; i++) {
            if (names[i]) {
                // add facets directly to parameters, not to actions
                FacetUtil.addFacet(new MandatoryFacetOverriddenByMethod(parameters[i]));
            }
        }
    }

    private void findAndRemoveNamesForActionParametersMethod(
            final MethodRemover methodRemover,
            final Class<?> cls,
            final String capitalizedName,
            final Class<?> returnType,
            final Class<?>[] params,
            final FacetHolder[] parameters) {
        Method method = findMethodWithOrWithoutParameters(cls, CLASS, PARAMETER_NAMES_PREFIX + capitalizedName,
                String[].class, params);
        if (method == null) {
            return;
        }
        removeMethod(methodRemover, method);

        final Object[] parameterObjects = new Object[method.getParameterTypes().length];
        final String[] names = (String[]) InvokeUtils.invokeStatic(method, parameterObjects);
        if (names.length != parameters.length) {
            throw new ReflectionException("Invalid number of parameter names, expected " + parameters.length + ", but got "
                    + names.length + ", on " + method);
        }
        for (int i = 0; i < names.length; i++) {
            // add facets directly to parameters, not to actions
            FacetUtil.addFacet(new NamedFacetViaMethod(names[i], method, parameters[i]));
        }
    }

    private void findAndRemoveChoicesForActionParametersMethod(
            final boolean oldChoicesOrDefaultsMethodsUsed,
            final MethodRemover methodRemover,
            final Class<?> cls,
            final String capitalizedName,
            final Class<?>[] params,
            final FacetHolder[] parameters) {

        for (int i = 0; i < params.length; i++) {
            final Class<?> returnType = (Array.newInstance(params[i], 0)).getClass();

            String name = PARAMETER_CHOICES_PREFIX + i + capitalizedName;
            Method method = findMethodWithOrWithoutParameters(cls, OBJECT, name, returnType, params);

            if (method == null) {
                method = findMethodWithOrWithoutParameters(cls, OBJECT, name, List.class, params);
            }

            if (method != null) {
                if (oldChoicesOrDefaultsMethodsUsed) {
                    throw new ReflectionException(cls + " uses both old and new choices/default syntax - must use one or other");
                }

                removeMethod(methodRemover, method);

                // add facets directly to parameters, not to actions
                FacetUtil.addFacet(new ActionParameterChoicesFacetViaMethod(method, returnType, parameters[i], getSpecificationLoader(), getRuntimeContext()));
            }
        }
    }

    private void findAndRemoveDefaultForActionParametersMethod(
            final boolean oldChoicesOrDefaultsMethodsUsed,
            final MethodRemover methodRemover,
            final Class<?> cls,
            final String capitalizedName,
            final Class<?>[] params,
            final FacetHolder[] parameters) {

        for (int i = 0; i < params.length; i++) {

            final Method method = findMethodWithOrWithoutParameters(cls, OBJECT, PARAMETER_DEFAULTS_PREFIX + i + capitalizedName,
                    params[i], params);

            if (method != null) {
                if (oldChoicesOrDefaultsMethodsUsed) {
                    throw new ReflectionException(cls + " uses both old and new choices/default syntax - must use one or other");
                }

                removeMethod(methodRemover, method);
                // add facets directly to parameters, not to actions
                FacetUtil.addFacet(new ActionParameterDefaultsFacetViaMethod(method, parameters[i]));
            }
        }
    }

    private void findAndRemoveDescriptionsforActionParametersMethod(
            final MethodRemover methodRemover,
            final Class<?> cls,
            final String capitalizedName,
            final Class<?> returnType,
            final Class<?>[] params,
            final FacetHolder[] parameters) {
        final Method method = findMethodWithOrWithoutParameters(cls, CLASS, PARAMETER_DESCRIPTIONS_PREFIX + capitalizedName,
                String[].class, params);
        if (method == null) {
            return;
        }
        removeMethod(methodRemover, method);

        final Object[] parameterObjects = new Object[method.getParameterTypes().length];
        final String[] names = (String[]) InvokeUtils.invokeStatic(method, parameterObjects);
        for (int i = 0; i < names.length; i++) {
            // add facets directly to parameters, not to actions
            FacetUtil.addFacet(new DescribedAsFacetViaMethod(names[i], method, parameters[i]));
        }
        methodRemover.removeMethod(method);
    }

    private String removePrefix(final String name, final String prefix) {
        if (name.startsWith(prefix)) {
            return name.substring(prefix.length());
        } else {
            return name;
        }
    }

    private boolean allParametersOfSameType(final Class<?>[] params) {
        final Class<?> firstParam = params[0];
        for (int i = 1; i < params.length; i++) {
            if (params[i] != firstParam) {
                return false;
            }
        }
        return true;
    }

    
    /////////////////////////////////////////////////////////////////
    // Dependencies
    /////////////////////////////////////////////////////////////////
    
    /**
     * @see #setRuntimeContext(RuntimeContext)
     * @return
     */
    public RuntimeContext getRuntimeContext() {
		return runtimeContext;
	}
    
    /**
     * Injected because {@link RuntimeContextAware}
     */
	public void setRuntimeContext(final RuntimeContext runtimeContext) {
		this.runtimeContext = runtimeContext;
	}

}
