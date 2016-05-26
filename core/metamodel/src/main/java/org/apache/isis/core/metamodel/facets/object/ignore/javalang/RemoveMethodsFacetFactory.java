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

package org.apache.isis.core.metamodel.facets.object.ignore.javalang;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.core.commons.lang.ClassExtensions;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.methodutils.MethodScope;
import org.apache.isis.core.metamodel.spec.InjectorMethodEvaluator;
import org.apache.isis.core.metamodel.specloader.InjectorMethodEvaluatorDefault;

/**
 * Designed to simply filter out any synthetic methods.
 * 
 * <p>
 * Does not add any {@link Facet}s.
 */
public class RemoveMethodsFacetFactory extends FacetFactoryAbstract {

    @SuppressWarnings("unused")
    private static final String JAVA_CLASS_PREFIX = "java.";

    public static class MethodAndParameterTypes {
        public final String methodName;
        public final Class<?>[] methodParameters;

        public MethodAndParameterTypes(final String methodName, final Class<?>[] methodParameters) {
            this.methodName = methodName;
            this.methodParameters = methodParameters;
        }
    }


    private final InjectorMethodEvaluator injectorMethodEvaluator = new InjectorMethodEvaluatorDefault();

    private final List<MethodAndParameterTypes> javaLangObjectMethodsToIgnore = Lists.newArrayList();


    public RemoveMethodsFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);

        final Class<?> typeToIgnore = Object.class;

        final Method[] methods = typeToIgnore.getMethods();
        for (final Method method : methods) {
            javaLangObjectMethodsToIgnore
                    .add(new RemoveMethodsFacetFactory.MethodAndParameterTypes(method.getName(), method.getParameterTypes()));
        }

    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        super.process(processClassContext);

        removeSyntheticOrAbstractMethods(processClassContext);
        removeSuperclassMethods(processClassContext.getCls(), processClassContext);

        removeJavaLangObjectMethods(processClassContext);
        removeJavaLangComparable(processClassContext);

        removeSetDomainObjectContainerMethod(processClassContext);
        removeInitMethod(processClassContext);

        removeInjectMethods(processClassContext);

        removeGetClass(processClassContext);
    }

    private void removeSyntheticOrAbstractMethods(final ProcessClassContext processClassContext) {
        Class<?> cls = processClassContext.getCls();
        Method[] methods = cls.getMethods();
        for (Method method : methods) {
            if (method.isSynthetic() || Modifier.isAbstract(method.getModifiers())) {
                processClassContext.removeMethod(method);
            }
        }
    }

    private void removeSuperclassMethods(Class<?> type, final ProcessClassContext processClassContext) {
        if (type == null) {
            return;
        }

        if (!ClassExtensions.isJavaClass(type)) {
            removeSuperclassMethods(type.getSuperclass(), processClassContext);
            return;
        }

        final Method[] methods = type.getMethods();
        for (final Method method : methods) {
            processClassContext.removeMethod(method);
        }

    }

    private void removeJavaLangObjectMethods(final ProcessClassContext processClassContext) {
        for (final RemoveMethodsFacetFactory.MethodAndParameterTypes mapt : javaLangObjectMethodsToIgnore) {
            processClassContext.removeMethod(MethodScope.OBJECT, mapt.methodName, null, mapt.methodParameters);
        }
    }


    private void removeJavaLangComparable(final ProcessClassContext processClassContext) {
        Class<?> cls = processClassContext.getCls();
        Method[] methods = cls.getMethods(); // not getDeclaredMethods !!!
        for (Method method : methods) {
            if(method.getName().equals("compareTo")) {
                processClassContext.removeMethod(method);
            }

        }
    }

    private void removeSetDomainObjectContainerMethod(final ProcessClassContext processClassContext) {
        processClassContext.removeMethod(
                MethodScope.OBJECT, "setContainer", void.class, new Class[] { DomainObjectContainer.class });
        processClassContext.removeMethod(MethodScope.OBJECT, "set_Container", void.class, new Class[] { DomainObjectContainer.class });
    }


    private void removeInitMethod(final ProcessClassContext processClassContext) {
        processClassContext.removeMethod(MethodScope.OBJECT, "init", void.class, new Class[0]);
    }


    private void removeInjectMethods(final ProcessClassContext processClassContext) {
        final List<Class<?>> serviceClasses = getSpecificationLoader().allServiceClasses();
        for (Class<? extends Object> serviceClass : serviceClasses) {
            Method[] methods = processClassContext.getCls().getMethods();
            for (Method method : methods) {
                if(injectorMethodEvaluator.isInjectorMethodFor(method, serviceClass)) {
                    processClassContext.removeMethod(method);
                }
            }
        }
    }

    private void removeGetClass(final ProcessClassContext processClassContext) {
        processClassContext.removeMethod(MethodScope.OBJECT, "getClass", Class.class, null);
    }

}
