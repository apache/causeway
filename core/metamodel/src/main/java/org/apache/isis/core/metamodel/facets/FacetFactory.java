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

import com.google.common.collect.Lists;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.lang.PropertiesExtensions;
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

    static class AbstractProcessWithClsContext<T extends FacetHolder> extends AbstractProcessContext<T>{

        private final Class<?> cls;

        AbstractProcessWithClsContext(final Class<?> cls, final T facetHolder) {
            super(facetHolder);
            this.cls = cls;
        }

        /**
         * The class being introspected upon.
         */
        public Class<?> getCls() {
            return cls;
        }
    }

    static class AbstractProcessWithMethodContext<T extends FacetHolder> extends AbstractProcessWithClsContext<T> implements MethodRemover{

        private final Method method;
        protected final MethodRemover methodRemover;

        AbstractProcessWithMethodContext(final Class<?> cls, final Method method, final MethodRemover methodRemover, final T facetHolder) {
            super(cls, facetHolder);
            this.method = method;
            this.methodRemover = methodRemover;
        }

        /**
         * The class being introspected upon.
         *
         * <p>
         *     This isn't necessarily the same as the {@link java.lang.reflect.Method#getDeclaringClass() declaring class} of the {@link #getMethod() method}; the method might have been inherited.
         * </p>
         */
        public Class<?> getCls() {
            return super.getCls();
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

    }

    public interface ProcessContextWithMetadataProperties<T extends FacetHolder> {
        public Properties metadataProperties(String subKey);
        public T getFacetHolder();
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

    public static class ProcessClassContext extends AbstractProcessWithClsContext<FacetHolder> implements MethodRemover, ProcessContextWithMetadataProperties<FacetHolder> {
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
            super(cls, facetHolder);
            this.methodRemover = methodRemover;
            this.metadataProperties = metadataProperties;
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

        public Properties metadataProperties(final String subKey) {
            if(metadataProperties == null) {
                return null;
            }
            final Properties subsetProperties = PropertiesExtensions.subset(this.metadataProperties, "class." + subKey);
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


    public static class ProcessMethodContext extends AbstractProcessWithMethodContext<FacetedMethod> implements  ProcessContextWithMetadataProperties<FacetedMethod> {
        private final FeatureType featureType;
        private final Properties metadataProperties;


        public ProcessMethodContext(
                final Class<?> cls, 
                final FeatureType featureType, 
                final Properties metadataProperties, 
                final Method method, 
                final MethodRemover methodRemover, 
                final FacetedMethod facetedMethod) {
            super(cls, method, methodRemover, facetedMethod);
            this.featureType = featureType;
            this.metadataProperties = metadataProperties;
        }

        public FeatureType getFeatureType() {
            return featureType;
        }

        public Properties metadataProperties(final String subKey) {
            
            if(metadataProperties == null) {
                return null;
            }
            final Identifier identifier = featureType.identifierFor(getCls(), getMethod());
            final String id = identifier.getMemberName();
            
            // build list of keys to search for... 
            final List<String> keys = Lists.newArrayList();
            if(featureType == FeatureType.ACTION) {
                // ... either "action.actionId" or "member.actionId()" 
                keys.add("action." + id+"."+subKey);
                keys.add("member." + id+"()"+"."+subKey);
            } else if(featureType == FeatureType.PROPERTY) {
                // ... either "property.propertyId" or "member.propertyId" 
                keys.add("property." + id+"."+subKey);
                keys.add("member." + id+"."+subKey);
            } else if(featureType == FeatureType.COLLECTION) {
                // ... either "collection.collectionId" or "member.collectionId" 
                keys.add("collection." + id+"."+subKey);
                keys.add("member." + id+"."+subKey);
            }

            for (final String key : keys) {
                final Properties subsetProperties = PropertiesExtensions.subset(this.metadataProperties, key);
                if (!subsetProperties.isEmpty()) {
                    return subsetProperties;
                } 
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

    public static class ProcessParameterContext extends AbstractProcessWithMethodContext<FacetedMethodParameter> {
        private final int paramNum;

        public ProcessParameterContext(
                final Class<?> cls,
                final Method method,
                final int paramNum,
                final MethodRemover methodRemover,
                final FacetedMethodParameter facetedMethodParameter) {
            super(cls, method, methodRemover, facetedMethodParameter);
            this.paramNum = paramNum;
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
