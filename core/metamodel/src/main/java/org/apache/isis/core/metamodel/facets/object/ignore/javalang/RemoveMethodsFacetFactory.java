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

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.commons.internal._Constants;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.reflection._Annotations;
import org.apache.isis.core.metamodel.commons.ClassExtensions;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.val;

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

    private final List<MethodAndParameterTypes> javaLangObjectMethodsToIgnore = _Lists.newArrayList();

    @Inject
    public RemoveMethodsFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_ONLY);

        getMethodCache()
        .streamPublicMethods(Object.class)
        .forEach(method->{
            javaLangObjectMethodsToIgnore
            .add(new RemoveMethodsFacetFactory.MethodAndParameterTypes(method.getName(), method.getParameterTypes()));
        });

    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        super.process(processClassContext);

        val cls = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();
        val isConcreteMixin = facetHolder instanceof ObjectSpecification
                ? ((ObjectSpecification)facetHolder).getBeanSort().isMixin()
                : false;

        val config = processClassContext.getFacetHolder().getMetaModelContext().getConfiguration();
        val isExplicitAction = config.getApplib().getAnnotation().getAction().isExplicit();

        getMethodCache()
        .streamPublicMethods(cls)
        .forEach(method->{
            // remove synthetic methods (except when is a mixin)
            // (it seems that javac marks methods synthetic in the context of non-static inner classes)
            if (!isConcreteMixin
                    && method.isSynthetic()) {
                processClassContext.removeMethod(method);
            }

            // removeJavaLangComparable(processClassContext);
            if(method.getName().equals("compareTo")) {
                processClassContext.removeMethod(method);
            }

            // remove property setter, if has not explicitly an @Action annotation
            // this code block is not required, if @Action annotations are explicit per config
            if(!isExplicitAction
                    && method.getParameterCount() == 1
                    && method.getName().startsWith("set")
                    && method.getName().length() > 3) {

                if(!_Annotations.synthesize(method, Action.class).isPresent()) {
                    processClassContext.removeMethod(method);
                }
            }
        });

        removeSuperclassMethods(processClassContext.getCls(), processClassContext);

        // removeJavaLangObjectMethods(processClassContext);
        for (final MethodAndParameterTypes mapt : javaLangObjectMethodsToIgnore) {
            processClassContext.removeMethod(mapt.methodName, null, mapt.methodParameters);
        }

        // removeInitMethod(processClassContext);
        processClassContext.removeMethod("init", void.class, _Constants.emptyClasses);
    }

    private void removeSuperclassMethods(final Class<?> type, final ProcessClassContext processClassContext) {
        if (type == null) {
            return;
        }

        if (!ClassExtensions.isJavaClass(type)) {
            removeSuperclassMethods(type.getSuperclass(), processClassContext);
            return;
        }

        getMethodCache()
        .streamPublicMethods(type)
        .forEach(processClassContext::removeMethod);

    }

}
