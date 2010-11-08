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


package org.apache.isis.core.progmodel.java5;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import org.apache.isis.applib.security.UserMemento;
import org.apache.isis.core.commons.lang.ListUtils;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.MethodRemover;
import org.apache.isis.core.metamodel.java5.MethodPrefixBasedFacetFactory;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.core.metamodel.util.InvokeUtils;
import org.apache.isis.core.progmodel.facets.actions.DescribedAsFacetViaMethod;
import org.apache.isis.core.progmodel.facets.actions.NamedFacetViaMethod;
import org.apache.isis.core.progmodel.facets.disable.DisableForContextFacetViaMethod;
import org.apache.isis.core.progmodel.facets.disable.DisableForSessionFacetViaMethod;
import org.apache.isis.core.progmodel.facets.disable.DisabledFacetAlways;
import org.apache.isis.core.progmodel.facets.hide.HiddenFacetAlways;
import org.apache.isis.core.progmodel.facets.hide.HideForContextFacetViaMethod;
import org.apache.isis.core.progmodel.facets.hide.HideForSessionFacetViaMethod;


public abstract class MethodPrefixBasedFacetFactoryAbstract extends FacetFactoryAbstract implements MethodPrefixBasedFacetFactory {

    protected static final boolean CLASS = true;
    protected static final Object[] NO_PARAMETERS = new Object[0];
    protected static final Class<?>[] NO_PARAMETERS_TYPES = new Class<?>[0];
    protected static final boolean OBJECT = false;

    private static final String DESCRIPTION_PREFIX = "description";
    private static final String NAME_PREFIX = "name";

    protected static final String VALIDATE_PREFIX = "validate";
    protected static final String DEFAULT_PREFIX = "default";
    protected static final String CHOICES_PREFIX = "choices";

    private static final String ALWAYS_HIDE_PREFIX = "alwaysHide";
    private static final String HIDE_FOR_SESSION_PREFIX = "hide";
    private static final String HIDE_PREFIX = "hide";
    private static final String PROTECT_PREFIX = "protect";
    private static final String DISABLE_FOR_SESSION_PREFIX = "disable";
    private static final String DISABLE_PREFIX = "disable";

    private static final String[] PREFIXES = { DESCRIPTION_PREFIX, NAME_PREFIX, VALIDATE_PREFIX, DEFAULT_PREFIX, CHOICES_PREFIX,
            ALWAYS_HIDE_PREFIX, HIDE_FOR_SESSION_PREFIX, HIDE_PREFIX, PROTECT_PREFIX, DISABLE_FOR_SESSION_PREFIX, DISABLE_PREFIX, };

    private final List<String> prefixes;

    public MethodPrefixBasedFacetFactoryAbstract(final String[] prefixes, final ObjectFeatureType[] featureTypes) {
        super(featureTypes);
        this.prefixes = ListUtils.combine(prefixes, PREFIXES);
    }

    public List<String> getPrefixes() {
        return prefixes;
    }

    protected Method findMethodWithOrWithoutParameters(
            final Class<?> type,
            final boolean classMethod,
            final String name,
            final Class<?> returnType,
            final Class<?>[] paramTypes) {
        Method method = findMethod(type, classMethod, name, returnType, paramTypes);
        if (method == null) {
            method = findMethod(type, classMethod, name, returnType, NO_PARAMETERS_TYPES);
        }
        return method;
    }

    /**
     * Returns a specific public methods that: have the specified prefix; have the specified return type, or
     * void, if canBeVoid is true; and has the specified number of parameters. If the returnType is specified
     * as null then the return type is ignored.
     * 
     * @param paramTypes
     *            the set of parameters the method should have, if null then is ignored
     */
    protected Method findMethod(
            final Class<?> type,
            final boolean forClass,
            final String name,
            final Class<?> returnType,
            final Class<?>[] paramTypes) {
        Method method;
        try {
            method = type.getMethod(name, paramTypes);
        } catch (final SecurityException e) {
            return null;
        } catch (final NoSuchMethodException e) {
            return null;
        }

        final int modifiers = method.getModifiers();

        // check for public modifier
        if (!Modifier.isPublic(modifiers)) {
            return null;
        }

        // check for static modifier
        if (Modifier.isStatic(modifiers) != forClass) {
            return null;
        }

        // check for name
        if (!method.getName().equals(name)) {
            return null;
        }

        // check for return type
        if (returnType != null && returnType != method.getReturnType()) {
            return null;
        }

        // check params (if required)
        if (paramTypes != null) {
            final Class<?>[] parameterTypes = method.getParameterTypes();
            if (paramTypes.length != parameterTypes.length) {
                return null;
            }

            for (int c = 0; c < paramTypes.length; c++) {
                if ((paramTypes[c] != null) && (paramTypes[c] != parameterTypes[c])) {
                    return null;
                }
            }
        }

        return method;
    }

    protected Method findMethod(
    		final Class<?> type, 
    		final boolean forClass, 
    		final String name, 
    		final Class<?> returnType) {
        try {
            final Method[] methods = type.getMethods();
            for (int i = 0; i < methods.length; i++) {
                final Method method = methods[i];
                final int modifiers = method.getModifiers();
                // check for public modifier
                if (!Modifier.isPublic(modifiers)) {
                    continue;
                }

                // check for static modifier
                if (Modifier.isStatic(modifiers) != forClass) {
                    continue;
                }

                // check for name
                if (!method.getName().equals(name)) {
                    continue;
                }

                // check for return type
                if (returnType != null && returnType != method.getReturnType()) {
                    continue;
                }
                return method;
            }
        } catch (final SecurityException e) {
            return null;
        }
        return null;
    }

    protected void removeMethod(final MethodRemover methodRemover, final Method method) {
        if (methodRemover != null && method != null) {
            methodRemover.removeMethod(method);
        }
    }

    protected Class<?>[] paramTypesOrNull(final Class<?> type) {
        return type == null ? null : new Class[] { type };
    }

    protected void findAndRemoveNameMethod(
            final List<Facet> facets,
            final MethodRemover methodRemover,
            final Class<?> type,
            final String capitalizedName,
            final FacetHolder facetHolder) {
        findAndRemoveNameMethod(facets, methodRemover, type, capitalizedName, (Class<?>) null, facetHolder);
    }

    protected void findAndRemoveNameMethod(
            final List<Facet> facets,
            final MethodRemover methodRemover,
            final Class<?> type,
            final String capitalizedName,
            final Class<?> paramType,
            final FacetHolder facetHolder) {
        findAndRemoveNameMethod(facets, methodRemover, type, capitalizedName, paramTypesOrNull(paramType), facetHolder);
    }

    protected void findAndRemoveNameMethod(
            final List<Facet> facets,
            final MethodRemover methodRemover,
            final Class<?> type,
            final String capitalizedName,
            final Class<?>[] paramTypes,
            final FacetHolder facetHolder) {
        final Method method = findMethod(type, CLASS, NAME_PREFIX + capitalizedName, String.class, paramTypes);

        if (method == null) {
            return;
        }

        methodRemover.removeMethod(method);
        final String name = (String) InvokeUtils.invokeStatic(method);
        facets.add(new NamedFacetViaMethod(name, method, facetHolder));
    }

    protected void findAndRemoveDescriptionMethod(
            final List<Facet> facets,
            final MethodRemover methodRemover,
            final Class<?> type,
            final String capitalizedName,
            final FacetHolder facetHolder) {
        findAndRemoveDescriptionMethod(facets, methodRemover, type, capitalizedName, (Class<?>) null, facetHolder);
    }

    protected void findAndRemoveDescriptionMethod(
            final List<Facet> facets,
            final MethodRemover methodRemover,
            final Class<?> type,
            final String capitalizedName,
            final Class<?> paramType,
            final FacetHolder facetHolder) {
        findAndRemoveDescriptionMethod(facets, methodRemover, type, capitalizedName, paramTypesOrNull(paramType), facetHolder);
    }

    protected void findAndRemoveDescriptionMethod(
            final List<Facet> facets,
            final MethodRemover methodRemover,
            final Class<?> type,
            final String capitalizedName,
            final Class<?>[] paramTypes,
            final FacetHolder facetHolder) {
        Method method;
        method = findMethod(type, CLASS, DESCRIPTION_PREFIX + capitalizedName, String.class, paramTypes);
        if (method == null) {
            return;
        }

        methodRemover.removeMethod(method);
        final String description = (String) InvokeUtils.invokeStatic(method);
        facets.add(new DescribedAsFacetViaMethod(description, method, facetHolder));
    }

    protected void findAndRemoveAlwaysHideMethod(
            final List<Facet> facets,
            final MethodRemover methodRemover,
            final Class<?> type,
            final String name,
            final FacetHolder facetHolder) {
        findAndRemoveAlwaysHideMethod(facets, methodRemover, type, name, (Class<?>) null, facetHolder);
    }

    protected void findAndRemoveAlwaysHideMethod(
            final List<Facet> facets,
            final MethodRemover methodRemover,
            final Class<?> type,
            final String name,
            final Class<?> paramType,
            final FacetHolder facetHolder) {
        findAndRemoveAlwaysHideMethod(facets, methodRemover, type, name, paramTypesOrNull(paramType), facetHolder);
    }

    protected void findAndRemoveAlwaysHideMethod(
            final List<Facet> facets,
            final MethodRemover methodRemover,
            final Class<?> type,
            final String name,
            final Class<?>[] paramTypes,
            final FacetHolder facetHolder) {

        final Method method = findMethodWithOrWithoutParameters(type, CLASS, ALWAYS_HIDE_PREFIX + name, boolean.class, paramTypes);
        if (method == null) {
            return;
        }

        methodRemover.removeMethod(method);

        final Boolean alwaysHideMethodReturnValue = (Boolean) InvokeUtils.invokeStatic(method);
        if (!alwaysHideMethodReturnValue.booleanValue()) {
            return;
        }
        facets.add(new HiddenFacetAlways(facetHolder));
    }

    protected void findAndRemoveProtectMethod(
            final List<Facet> facets,
            final MethodRemover methodRemover,
            final Class<?> type,
            final String name,
            final FacetHolder facetHolder) {
        findAndRemoveProtectMethod(facets, methodRemover, type, name, (Class<?>) null, facetHolder);
    }

    protected void findAndRemoveProtectMethod(
            final List<Facet> facets,
            final MethodRemover methodRemover,
            final Class<?> type,
            final String name,
            final Class<?> paramType,
            final FacetHolder facetHolder) {
        findAndRemoveProtectMethod(facets, methodRemover, type, name, paramTypesOrNull(paramType), facetHolder);
    }

    protected void findAndRemoveProtectMethod(
            final List<Facet> facets,
            final MethodRemover methodRemover,
            final Class<?> type,
            final String name,
            final Class<?>[] paramTypes,
            final FacetHolder facetHolder) {

        final Method method = findMethodWithOrWithoutParameters(type, CLASS, PROTECT_PREFIX + name, boolean.class, paramTypes);
        if (method == null) {
            return;
        }

        methodRemover.removeMethod(method);

        final Boolean protectMethodReturnValue = (Boolean) InvokeUtils.invokeStatic(method);
        if (!protectMethodReturnValue.booleanValue()) {
            return;
        }
        facets.add(new DisabledFacetAlways(facetHolder));
    }

    protected void findAndRemoveDisableMethod(
            final List<Facet> facets,
            final MethodRemover methodRemover,
            final Class<?> type,
            final boolean onClass,
            final String capitalizedName,
            final FacetHolder facetHolder) {
        findAndRemoveDisableMethod(facets, methodRemover, type, onClass, capitalizedName, (Class<?>) null, facetHolder);
    }

    protected void findAndRemoveDisableMethod(
            final List<Facet> facets,
            final MethodRemover methodRemover,
            final Class<?> type,
            final boolean onClass,
            final String capitalizedName,
            final Class<?> paramType,
            final FacetHolder facetHolder) {
        findAndRemoveDisableMethod(facets, methodRemover, type, onClass, capitalizedName, paramTypesOrNull(paramType), facetHolder);
    }

    protected void findAndRemoveDisableMethod(
            final List<Facet> facets,
            final MethodRemover methodRemover,
            final Class<?> type,
            final boolean onClass,
            final String capitalizedName,
            final Class<?>[] paramTypes,
            final FacetHolder facetHolder) {

        Method method = findMethodWithOrWithoutParameters(type, onClass, DISABLE_PREFIX + capitalizedName, String.class,
                paramTypes);
        if (method == null) {
        	method = findMethodWithOrWithoutParameters(type, onClass, DISABLE_PREFIX + capitalizedName, String.class,
        			new Class[]{});
        }
        if (method == null) {
            return;
        }

        methodRemover.removeMethod(method);
        facets.add(new DisableForContextFacetViaMethod(method, facetHolder));
    }

    protected void findAndRemoveHideMethod(
            final List<Facet> facets,
            final MethodRemover methodRemover,
            final Class<?> type,
            final boolean onClass,
            final String capitalizedName,
            final FacetHolder facetHolder) {
        findAndRemoveHideMethod(facets, methodRemover, type, onClass, capitalizedName, (Class<?>) null, facetHolder);
    }

    protected void findAndRemoveHideMethod(
            final List<Facet> facets,
            final MethodRemover methodRemover,
            final Class<?> type,
            final boolean onClass,
            final String capitalizedName,
            final Class<?> collectionType,
            final FacetHolder facetHolder) {
        findAndRemoveHideMethod(facets, methodRemover, type, onClass, capitalizedName, paramTypesOrNull(collectionType),
                facetHolder);
    }

    protected void findAndRemoveHideMethod(
            final List<Facet> facets,
            final MethodRemover methodRemover,
            final Class<?> type,
            final boolean onClass,
            final String capitalizedName,
            final Class<?>[] paramTypes,
            final FacetHolder facetHolder) {

        Method method = findMethod(type, onClass, HIDE_PREFIX + capitalizedName, boolean.class, paramTypes);
        if (method == null) {
        	method = findMethod(type, onClass, HIDE_PREFIX + capitalizedName, boolean.class, new Class[]{});
        }
        if (method == null) {
            return;
        }

        methodRemover.removeMethod(method);
        facets.add(new HideForContextFacetViaMethod(method, facetHolder));
    }

    protected void findAndRemoveHideForSessionMethod(
            final List<Facet> facets,
            final MethodRemover methodRemover,
            final Class<?> type,
            final String capitalizedName,
            final Class<?> paramType,
            final FacetHolder facetHolder) {
        findAndRemoveHideForSessionMethod(facets, methodRemover, type, capitalizedName, paramTypesOrNull(paramType), facetHolder);
    }

    protected void findAndRemoveHideForSessionMethod(
            final List<Facet> facets,
            final MethodRemover methodRemover,
            final Class<?> type,
            final String capitalizedName,
            final Class<?>[] paramTypes,
            final FacetHolder facetHolder) {

        final Class<?>[] sessionParams = new Class[paramTypes.length + 1];
        sessionParams[0] = UserMemento.class;
        System.arraycopy(paramTypes, 0, sessionParams, 1, paramTypes.length);
        Method method = findMethod(type, CLASS, HIDE_FOR_SESSION_PREFIX + capitalizedName, boolean.class, sessionParams);
        if (method == null && paramTypes.length > 0) {
            method = findMethod(type, CLASS, HIDE_FOR_SESSION_PREFIX + capitalizedName, boolean.class,
                    new Class[] { UserMemento.class });
        }

        if (method == null) {
            return;
        }

        methodRemover.removeMethod(method);
        facets.add(new HideForSessionFacetViaMethod(method, facetHolder));
    }

    protected void findAndRemoveDisableForSessionMethod(
            final List<Facet> facets,
            final MethodRemover methodRemover,
            final Class<?> type,
            final String capitalizedName,
            final Class<?> paramType,
            final FacetHolder facetHolder) {
        findAndRemoveDisableForSessionMethod(facets, methodRemover, type, capitalizedName, paramTypesOrNull(paramType),
                facetHolder);
    }

    protected void findAndRemoveDisableForSessionMethod(
            final List<Facet> facets,
            final MethodRemover methodRemover,
            final Class<?> type,
            final String capitalizedName,
            final Class<?>[] paramTypes,
            final FacetHolder facetHolder) {

        final Class<?>[] sessionParams = new Class[paramTypes.length + 1];
        sessionParams[0] = UserMemento.class;
        System.arraycopy(paramTypes, 0, sessionParams, 1, paramTypes.length);

        Method method = findMethod(type, CLASS, DISABLE_FOR_SESSION_PREFIX + capitalizedName, String.class, sessionParams);
        if (method == null && paramTypes.length > 0) {
            method = findMethod(type, CLASS, DISABLE_FOR_SESSION_PREFIX + capitalizedName, String.class,
                    new Class[] { UserMemento.class });
        }

        if (method == null) {
            return;
        }

        methodRemover.removeMethod(method);
        facets.add(new DisableForSessionFacetViaMethod(method, facetHolder));
    }

}
