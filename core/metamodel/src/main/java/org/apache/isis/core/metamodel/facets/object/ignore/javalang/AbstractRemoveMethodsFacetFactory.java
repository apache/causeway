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
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.methodutils.MethodScope;

/**
 * Removes all methods inherited specified class.
 */
public abstract class AbstractRemoveMethodsFacetFactory extends FacetFactoryAbstract {

    private static class MethodAndParameterTypes {
        private final String methodName;
        private final Class<?>[] methodParameters;

        public MethodAndParameterTypes(final String methodName, final Class<?>[] methodParameters) {
            this.methodName = methodName;
            this.methodParameters = methodParameters;
        }
    }

    private final List<MethodAndParameterTypes> methodsToIgnore = Lists.newArrayList();

    public AbstractRemoveMethodsFacetFactory(final Class<?> typeToIgnore) {
        super(FeatureType.OBJECTS_ONLY);
        final Method[] methods = typeToIgnore.getMethods();
        for (final Method method : methods) {
            methodsToIgnore.add(new MethodAndParameterTypes(method.getName(), method.getParameterTypes()));
        }
    }

    public AbstractRemoveMethodsFacetFactory(final String typeToIgnoreIfOnClasspath) {
        super(FeatureType.OBJECTS_ONLY);
        try {
            Class<?> typeToIgnore = InstanceUtil.loadClass(typeToIgnoreIfOnClasspath);
            addMethodsToBeIgnored(typeToIgnore);
        } catch(Exception ex) {
            // ignore
        }
    }

    private void addMethodsToBeIgnored(Class<?> typeToIgnore) {
        final Method[] methods = typeToIgnore.getMethods();
        for (final Method method : methods) {
            methodsToIgnore.add(new MethodAndParameterTypes(method.getName(), method.getParameterTypes()));
        }
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        for (final MethodAndParameterTypes mapt : methodsToIgnore) {
            processClassContext.removeMethod(MethodScope.OBJECT, mapt.methodName, null, mapt.methodParameters);
        }
    }

}
