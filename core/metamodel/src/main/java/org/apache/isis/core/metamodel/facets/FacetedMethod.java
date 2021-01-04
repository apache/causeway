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
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.commons.internal.collections._Collections;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.commons.StringExtensions;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.collparam.semantics.CollectionSemanticsFacet;
import org.apache.isis.core.metamodel.facets.collparam.semantics.CollectionSemanticsFacetDefault;

/**
 * non-final only so it can be mocked if need be.
 */
public class FacetedMethod extends TypedHolderDefault implements IdentifiedHolder {

    // //////////////////////////////////////////////////
    // Factory methods
    // //////////////////////////////////////////////////

    /**
     * Principally for testing purposes.
     */
    public static FacetedMethod createForProperty(final Class<?> declaringType, final String propertyName) {
        try {
            final Method method = declaringType.getMethod("get" + StringExtensions.asPascal(propertyName));
            return FacetedMethod.createForProperty(declaringType, method);
        } catch (final SecurityException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Principally for testing purposes.
     */
    public static FacetedMethod createForCollection(final Class<?> declaringType, final String collectionName) {
        try {
            final Method method = declaringType.getMethod("get" + StringExtensions.asPascal(collectionName));
            return FacetedMethod.createForCollection(declaringType, method);
        } catch (final SecurityException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Principally for testing purposes.
     */
    public static FacetedMethod createForAction(
            final Class<?> declaringType,
            final String actionName,
            final Class<?>... parameterTypes) {

        try {
            final Method method = declaringType.getMethod(actionName, parameterTypes);
            return FacetedMethod.createForAction(declaringType, method);
        } catch (final SecurityException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


    public static FacetedMethod createForProperty(final Class<?> declaringType, final Method method) {
        return new FacetedMethod(FeatureType.PROPERTY, declaringType, method, method.getReturnType(), emptyParameterList());
    }

    public static FacetedMethod createForCollection(final Class<?> declaringType, final Method method) {
        return new FacetedMethod(FeatureType.COLLECTION, declaringType, method, null, emptyParameterList());
    }

    public static FacetedMethod createForAction(
            final Class<?> declaringType,
            final Method method) {
        return new FacetedMethod(FeatureType.ACTION, declaringType, method, method.getReturnType(), 
                getParameters(declaringType, method));
    }

    private static List<FacetedMethodParameter> getParameters(
            final Class<?> declaringType,
            final Method actionMethod) {

        final Class<?>[] parameterTypes = actionMethod.getParameterTypes();
        final Type[] genericParameterTypes = actionMethod.getGenericParameterTypes();
        final List<FacetedMethodParameter> actionParams = _Lists.newArrayList();

        for (int paramNum = 0; paramNum < parameterTypes.length; paramNum++) {

            final Class<?> parameterType = parameterTypes[paramNum];
            final Type genericParameterType = genericParameterTypes[paramNum];

            final FeatureType featureType =
                    _Collections.inferElementTypeFromArrayOrCollection(parameterType, genericParameterType).isPresent()
                        ? FeatureType.ACTION_PARAMETER_COLLECTION
                        : FeatureType.ACTION_PARAMETER_SCALAR;

            final FacetedMethodParameter fmp = new FacetedMethodParameter(featureType, declaringType, actionMethod, parameterType);
            actionParams.add(fmp);

            // this is based on similar logic to ActionAnnotationFacetFactory#processTypeOf
            if(featureType == FeatureType.ACTION_PARAMETER_COLLECTION) {

                final CollectionSemanticsFacet semanticsFacet =
                        CollectionSemanticsFacetDefault.forParamType(parameterType, fmp);
                FacetUtil.addFacet(semanticsFacet);

                TypeOfFacet typeOfFacet = TypeOfFacet.Util
                        .inferFromParameterType(fmp, parameterType, genericParameterType);
                

                // copy over (corresponds to similar code for OneToManyAssociation in FacetMethodsBuilder).
                if(typeOfFacet != null ) {
                    FacetUtil.addFacet(typeOfFacet);
                    fmp.setType(typeOfFacet.value());
                }
            }

        }
        return Collections.unmodifiableList(actionParams);
    }

    // //////////////////////////////////////////////////
    // Constructor
    // //////////////////////////////////////////////////

    private final Class<?> owningType;
    private final Method method;
    private final Identifier identifier;
    private final List<FacetedMethodParameter> parameters;

    public List<FacetedMethodParameter> getParameters() {
        return parameters;
    }

    public static List<FacetedMethodParameter> emptyParameterList() {
        final List<FacetedMethodParameter> emptyList = Collections.emptyList();
        return Collections.unmodifiableList(emptyList);
    }

    private FacetedMethod(final FeatureType featureType, final Class<?> declaringType, final Method method, final Class<?> type, final List<FacetedMethodParameter> parameters) {
        super(featureType, type);
        this.owningType = declaringType;
        this.method = method;
        this.identifier = featureType.identifierFor(declaringType, method);
        this.parameters = parameters;
    }

    /**
     * The {@link Class} that owns this {@link Method} (as per
     * {@link Class#getMethods()}, returning the {@link Method}s both of this
     * class and of its superclasses).
     *
     * <p>
     * Note: we don't call this the 'declaring type' because
     * {@link Class#getDeclaredMethods()} does not return methods from
     * superclasses.
     */
    public Class<?> getOwningType() {
        return owningType;
    }

    /**
     * A {@link Method} obtained from the {@link #getOwningType() owning type}
     * using {@link Class#getMethods()}.
     */
    public Method getMethod() {
        return method;
    }

    @Override
    public Identifier getIdentifier() {
        return identifier;
    }

    // ////////////////////////////////////////////////////////////////////
    // toString
    // ////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return getFeatureType().name() + " Peer [identifier=\"" + getIdentifier() + "\",type=" + getType().getName() + " ]";
    }


}
