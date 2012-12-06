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
package org.apache.isis.progmodel.groovy.metamodel;

import java.lang.reflect.Method;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationAware;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MethodRemover;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.methodutils.MethodScope;

public class RemoveGroovyMethodsFacetFactory extends FacetFactoryAbstract implements IsisConfigurationAware {

    private static final String DEPTH_KEY = "isis.groovy.depth";
    private static final int DEPTH_DEFAULT = 5;

    private IsisConfiguration configuration;

    public RemoveGroovyMethodsFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    static class MethodSpec {
        static class Builder {

            private final MethodSpec methodSpec = new MethodSpec();

            public Builder param(final Class<?>... paramTypes) {
                methodSpec.parameterTypes = paramTypes;
                return this;
            }

            public Builder ret(final Class<?> returnType) {
                methodSpec.returnType = returnType;
                return this;
            }

            public MethodSpec build() {
                return methodSpec;
            }

            public void remove(final MethodRemover remover) {
                build().removeMethod(remover);
            }
        }

        static Builder specFor(final String methodName) {
            final Builder builder = new Builder();
            builder.methodSpec.methodName = methodName;
            return builder;
        }

        static Builder specFor(final String formatStr, final Object... args) {
            return specFor(String.format(formatStr, args));
        }

        private String methodName;
        private Class<?> returnType = void.class;
        private Class<?>[] parameterTypes = new Class[0];

        void removeMethod(final MethodRemover methodRemover) {
            methodRemover.removeMethod(MethodScope.OBJECT, methodName, returnType, parameterTypes);
        }
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        MethodSpec.specFor("invokeMethod").param(String.class, Object.class).ret(Object.class).remove(processClassContext);
        MethodSpec.specFor("getMetaClass").ret(groovy.lang.MetaClass.class).remove(processClassContext);
        MethodSpec.specFor("setMetaClass").param(groovy.lang.MetaClass.class).remove(processClassContext);
        MethodSpec.specFor("getProperty").param(String.class).ret(Object.class).remove(processClassContext);

        final int depth = determineDepth();
        for (int i = 1; i < depth; i++) {
            MethodSpec.specFor("this$dist$invoke$%d", i).param(String.class, Object.class).ret(Object.class).remove(processClassContext);
            MethodSpec.specFor("this$dist$set$%d", i).param(String.class, Object.class).remove(processClassContext);
            MethodSpec.specFor("this$dist$get$%d", i).param(String.class).ret(Object.class).remove(processClassContext);
        }
        final Method[] methods = processClassContext.getCls().getMethods();
        for (final Method method : methods) {
            if (method.getName().startsWith("super$")) {
                processClassContext.removeMethod(method);
            }
        }
    }

    private int determineDepth() {
        final int depth = configuration.getInteger(DEPTH_KEY, DEPTH_DEFAULT);
        return depth;
    }

    @Override
    public void setConfiguration(final IsisConfiguration configuration) {
        this.configuration = configuration;
    }

}
