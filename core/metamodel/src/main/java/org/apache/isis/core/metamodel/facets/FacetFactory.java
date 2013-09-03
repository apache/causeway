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

package org.apache.isis.core.metamodel.facets;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.lang.PropertyUtil;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MethodRemover;
import org.apache.isis.core.metamodel.methodutils.MethodScope;

public interface FacetFactory {

    static class AbstractProcessContext<T extends FacetHolder> {
        private final T facetHolder;

        public AbstractProcessContext(final T facetHolder) {
            this.facetHolder = facetHolder;
        }

        public T getFacetHolder() {
            return facetHolder;
        }
    }

    /**
     * The {@link FeatureType feature type}s that this facet factory can create
     * {@link Facet}s for.
     * 
     * <p>
     * Used by the Java5 Reflector's <tt>ProgrammingModel</tt> to reduce the
     * number of {@link FacetFactory factory}s that are queried when building up
     * the meta-model.
     */
    List<FeatureType> getFeatureTypes();
    
    
    // //////////////////////////////////////
    // process class
    // //////////////////////////////////////

    public static class ProcessClassContext extends AbstractProcessContext<FacetHolder> implements MethodRemover {
        private final Class<?> cls;
        private final MethodRemover methodRemover;
        private final Properties metadataProperties;

        /**
         * For testing only.
         */
        public ProcessClassContext(final Class<?> cls, final MethodRemover methodRemover, final FacetHolder facetHolder) {
            this(cls, null, methodRemover, facetHolder);
        }

        public ProcessClassContext(
                final Class<?> cls, 
                final Properties metadataProperties, 
                final MethodRemover methodRemover, 
                final FacetHolder facetHolder) {
            super(facetHolder);
            this.cls = cls;
            this.methodRemover = methodRemover;
            this.metadataProperties = metadataProperties;
        }

        /**
         * The class being processed.
         */
        public Class<?> getCls() {
            return cls;
        }

        @Override
        public void removeMethod(final Method method) {
            methodRemover.removeMethod(method);
        }

        @Override
        public List<Method> removeMethods(final MethodScope methodScope, final String prefix, final Class<?> returnType, final boolean canBeVoid, final int paramCount) {
            return methodRemover.removeMethods(methodScope, prefix, returnType, canBeVoid, paramCount);
        }

        @Override
        public void removeMethod(final MethodScope methodScope, final String methodName, final Class<?> returnType, final Class<?>[] parameterTypes) {
            methodRemover.removeMethod(methodScope, methodName, returnType, parameterTypes);
        }

        @Override
        public void removeMethods(final List<Method> methods) {
            methodRemover.removeMethods(methods);
        }

        public Properties metadataProperties(String prefix) {
            if(metadataProperties == null) {
                return null;
            }
            final Properties subsetProperties = PropertyUtil.subset(this.metadataProperties, prefix);
            return !subsetProperties.isEmpty() ? subsetProperties : null;
        }
    }

    /**
     * Process the class, and return the correctly setup annotation if present.
     */
    void process(ProcessClassContext processClassContext);
    
    // //////////////////////////////////////
    // process method
    // //////////////////////////////////////


    public static class ProcessMethodContext extends AbstractProcessContext<FacetedMethod> implements MethodRemover {
        private final Class<?> cls;
        private final FeatureType featureType;
        private final Properties metadataProperties;
        private final Method method;
        private final MethodRemover methodRemover;

        public ProcessMethodContext(
                final Class<?> cls, 
                final FeatureType featureType, 
                final Properties metadataProperties, 
                final Method method, 
                final MethodRemover methodRemover, 
                final FacetedMethod facetedMethod) {
            super(facetedMethod);
            this.cls = cls;
            this.featureType = featureType;
            this.metadataProperties = metadataProperties;
            this.method = method;
            this.methodRemover = methodRemover;
        }

        public Class<?> getCls() {
            return cls;
        }

        public Method getMethod() {
            return method;
        }

        @Override
        public List<Method> removeMethods(final MethodScope methodScope, final String prefix, final Class<?> returnType, final boolean canBeVoid, final int paramCount) {
            return methodRemover.removeMethods(methodScope, prefix, returnType, canBeVoid, paramCount);
        }

        @Override
        public void removeMethod(final MethodScope methodScope, final String methodName, final Class<?> returnType, final Class<?>[] parameterTypes) {
            methodRemover.removeMethod(methodScope, methodName, returnType, parameterTypes);
        }

        @Override
        public void removeMethod(final Method method) {
            methodRemover.removeMethod(method);
        }

        @Override
        public void removeMethods(final List<Method> methods) {
            methodRemover.removeMethods(methods);
        }

        public Properties metadataProperties(String prefix) {
            
            if(metadataProperties == null) {
                return null;
            }
            Identifier identifier = featureType.identifierFor(getCls(), getMethod());
            final String id = identifier.getMemberName();
            
            // bit of a hack; to distinguish between actions and properties that have same identifier
            // eg getPaidBy() and paidBy()
            if(featureType.isAction()) {
                Properties subsetProperties = PropertyUtil.subset(this.metadataProperties, prefix+"."+id+"()");
                if (!subsetProperties.isEmpty()) {
                    return subsetProperties;
                } 
            }

            // otherwise, regular processing...
            Properties subsetProperties = PropertyUtil.subset(this.metadataProperties, prefix+"."+id);
            if (!subsetProperties.isEmpty()) {
                return subsetProperties;
            }
            
            return null;
        }
    }

    /**
     * Process the method, and return the correctly setup annotation if present.
     */
    void process(ProcessMethodContext processMethodContext);

    
    // //////////////////////////////////////
    // process param
    // //////////////////////////////////////

    public static class ProcessParameterContext extends AbstractProcessContext<FacetedMethodParameter> {
        private final Method method;
        private final int paramNum;

        public ProcessParameterContext(
                final Method method, 
                final int paramNum, 
                final FacetedMethodParameter facetedMethodParameter) {
            super(facetedMethodParameter);
            this.method = method;
            this.paramNum = paramNum;
        }

        public Method getMethod() {
            return method;
        }

        public int getParamNum() {
            return paramNum;
        }
    }

    /**
     * Process the parameters of the method, and return the correctly setup
     * annotation if present.
     */
    void processParams(ProcessParameterContext processParameterContext);
}
