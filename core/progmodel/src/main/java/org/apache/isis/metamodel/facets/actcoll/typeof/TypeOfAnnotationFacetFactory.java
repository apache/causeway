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


package org.apache.isis.metamodel.facets.actcoll.typeof;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.apache.isis.applib.annotation.TypeOf;
import org.apache.isis.core.metamodel.facets.FacetHolder;
import org.apache.isis.core.metamodel.facets.FacetUtil;
import org.apache.isis.core.metamodel.facets.MethodRemover;
import org.apache.isis.core.metamodel.java5.AnnotationBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeatureType;
import org.apache.isis.core.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistry;
import org.apache.isis.core.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistryAware;


public class TypeOfAnnotationFacetFactory extends AnnotationBasedFacetFactoryAbstract implements CollectionTypeRegistryAware {
    private CollectionTypeRegistry collectionTypeRegistry;

    public TypeOfAnnotationFacetFactory() {
        super(ObjectFeatureType.COLLECTIONS_AND_ACTIONS);
    }

    @Override
    public boolean process(Class<?> cls, final Method method, final MethodRemover methodRemover, final FacetHolder holder) {

        final TypeOf annotation = getAnnotation(method, TypeOf.class);

        final Class<?> methodReturnType = method.getReturnType();
        if (!collectionTypeRegistry.isCollectionType(methodReturnType) && !collectionTypeRegistry.isArrayType(methodReturnType)) {
            return false;
        }

        final Class<?> returnType = method.getReturnType();
        if (returnType.isArray()) {
            final Class<?> componentType = returnType.getComponentType();
            FacetUtil.addFacet(new TypeOfFacetInferredFromArray(componentType, holder, getSpecificationLoader()));
            return false;
        }

        if (annotation != null) {
            return FacetUtil.addFacet(new TypeOfFacetViaAnnotation(annotation.value(), holder, getSpecificationLoader()));
        }

        final Type type = method.getGenericReturnType();
        if (!(type instanceof ParameterizedType)) {
            return false;
        }

        final ParameterizedType parameterizedType = (ParameterizedType) type;
        final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (actualTypeArguments.length == 0) {
            return false;
        }

        final Object actualTypeArgument = actualTypeArguments[0];
        if (actualTypeArgument instanceof Class) {
            final Class<?> actualType = (Class<?>) actualTypeArgument;
            return FacetUtil.addFacet(new TypeOfFacetInferredFromGenerics(actualType, holder, getSpecificationLoader()));
        }

        if (actualTypeArgument instanceof TypeVariable) {

            // TODO: what to do here?
            return false;
        }

        return false;
    }

    public void setCollectionTypeRegistry(final CollectionTypeRegistry collectionTypeRegistry) {
        this.collectionTypeRegistry = collectionTypeRegistry;
    }

}
