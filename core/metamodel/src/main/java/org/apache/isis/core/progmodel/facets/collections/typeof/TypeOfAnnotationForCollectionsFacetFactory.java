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

package org.apache.isis.core.progmodel.facets.collections.typeof;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.apache.isis.applib.annotation.TypeOf;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacetInferredFromArray;
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacetInferredFromGenerics;
import org.apache.isis.core.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistry;
import org.apache.isis.core.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistryAware;

public class TypeOfAnnotationForCollectionsFacetFactory extends FacetFactoryAbstract implements CollectionTypeRegistryAware {
    private CollectionTypeRegistry collectionTypeRegistry;

    public TypeOfAnnotationForCollectionsFacetFactory() {
        super(FeatureType.COLLECTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final TypeOf annotation = Annotations.getAnnotation(processMethodContext.getMethod(), TypeOf.class);

        final Class<?> methodReturnType = processMethodContext.getMethod().getReturnType();
        if (!collectionTypeRegistry.isCollectionType(methodReturnType) && !collectionTypeRegistry.isArrayType(methodReturnType)) {
            return;
        }

        final Class<?> returnType = processMethodContext.getMethod().getReturnType();
        if (returnType.isArray()) {
            final Class<?> componentType = returnType.getComponentType();
            FacetUtil.addFacet(new TypeOfFacetInferredFromArray(componentType, processMethodContext.getFacetHolder(), getSpecificationLoader()));
            return;
        }

        if (annotation != null) {
            FacetUtil.addFacet(new TypeOfFacetAnnotationForCollection(annotation.value(), processMethodContext.getFacetHolder(), getSpecificationLoader()));
            return;
        }

        final Type type = processMethodContext.getMethod().getGenericReturnType();
        if (!(type instanceof ParameterizedType)) {
            return;
        }

        final ParameterizedType parameterizedType = (ParameterizedType) type;
        final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        if (actualTypeArguments.length == 0) {
            return;
        }

        final Object actualTypeArgument = actualTypeArguments[0];
        if (actualTypeArgument instanceof Class) {
            final Class<?> actualType = (Class<?>) actualTypeArgument;
            FacetUtil.addFacet(new TypeOfFacetInferredFromGenerics(actualType, processMethodContext.getFacetHolder(), getSpecificationLoader()));
            return;
        }

        if (actualTypeArgument instanceof TypeVariable) {

            // TODO: what to do here?
            return;
        }

    }

    @Override
    public void setCollectionTypeRegistry(final CollectionTypeRegistry collectionTypeRegistry) {
        this.collectionTypeRegistry = collectionTypeRegistry;
    }

}
