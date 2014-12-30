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

package org.apache.isis.core.metamodel.facets.actions.typeof.annotation;

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.TypeOf;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacetInferredFromArray;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacetInferredFromGenerics;
import org.apache.isis.core.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistry;

public class TypeOfFacetOnActionAnnotationFactory extends FacetFactoryAbstract {

    private final CollectionTypeRegistry collectionTypeRegistry = new CollectionTypeRegistry();

    public TypeOfFacetOnActionAnnotationFactory() {
        super(FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();
        final Class<?> methodReturnType = method.getReturnType();
        if (!collectionTypeRegistry.isCollectionType(methodReturnType) && !collectionTypeRegistry.isArrayType(methodReturnType)) {
            return;
        }

        final Class<?> returnType = method.getReturnType();
        if (returnType.isArray()) {
            final Class<?> componentType = returnType.getComponentType();
            FacetUtil.addFacet(new TypeOfFacetInferredFromArray(componentType, processMethodContext.getFacetHolder(), getSpecificationLoader()));
            return;
        }

        final Action action = Annotations.getAnnotation(method, Action.class);
        if (action != null) {
            final Class<?> typeOf = action.typeOf();
            if(typeOf != null && typeOf != Object.class) {
                FacetUtil.addFacet(new TypeOfFacetForActionAnnotation(typeOf, getSpecificationLoader(), processMethodContext.getFacetHolder()));
                return;
            }
        }

        final TypeOf annotation = Annotations.getAnnotation(method, TypeOf.class);
        if (annotation != null) {
            FacetUtil.addFacet(new TypeOfFacetOnActionAnnotation(annotation.value(), getSpecificationLoader(), processMethodContext.getFacetHolder()));
            return;
        }

        final Type type = method.getGenericReturnType();
        if (!(type instanceof ParameterizedType)) {
            return;
        }

        final ParameterizedType methodParameterizedType = (ParameterizedType) type;
        final Type[] methodActualTypeArguments = methodParameterizedType.getActualTypeArguments();
        if (methodActualTypeArguments.length == 0) {
            return;
        }

        final Object methodActualTypeArgument = methodActualTypeArguments[0];
        if (methodActualTypeArgument instanceof Class) {
            final Class<?> actualType = (Class<?>) methodActualTypeArgument;
            FacetUtil.addFacet(new TypeOfFacetInferredFromGenerics(actualType, processMethodContext.getFacetHolder(), getSpecificationLoader()));
            return;
        }

        if (methodActualTypeArgument instanceof TypeVariable) {

            TypeVariable<?> methodTypeVariable = (TypeVariable<?>) methodActualTypeArgument;
            final GenericDeclaration methodGenericClassDeclaration = methodTypeVariable.getGenericDeclaration();
            
            // try to match up with the actual type argument of the generic superclass.
            final Type genericSuperclass = processMethodContext.getCls().getGenericSuperclass();
            if(genericSuperclass instanceof ParameterizedType) {
                final ParameterizedType parameterizedTypeOfSuperclass = (ParameterizedType)genericSuperclass;
                if(parameterizedTypeOfSuperclass.getRawType() == methodGenericClassDeclaration) {
                    final Type[] genericSuperClassActualTypeArguments = parameterizedTypeOfSuperclass.getActualTypeArguments();
                    // simplification: if there's just one, then use it.
                    if(methodActualTypeArguments.length == 1) {
                        final Type actualType = genericSuperClassActualTypeArguments[0];
                        if(actualType instanceof Class) {
                            // just being safe
                            Class<?> actualCls = (Class<?>) actualType;
                            FacetUtil.addFacet(new TypeOfFacetInferredFromGenerics(actualCls, processMethodContext.getFacetHolder(), getSpecificationLoader()));
                            return;
                        }
                    }
                }
            }
            
            // TODO: otherwise, what to do?
            return;
        }

    }

}
