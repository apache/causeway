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

package org.apache.isis.core.metamodel.facetapi;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.List;

import com.google.common.collect.ImmutableList;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.lang.NameUtils;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

/**
 * Enumerates the features that a particular Facet can be applied to.
 * 
 * <p>
 * TODO: should rationalize this and {@link ObjectSpecification#getResultType()}
 * . Note though that we don't distinguish value properties and reference
 * properties (and we probably shouldn't in {@link ObjectSpecification},
 * either).
 */
public enum FeatureType {

    OBJECT("Object") {
        /**
         * The supplied method can be null; at any rate it will be ignored.
         */
        @Override
        public Identifier identifierFor(final Class<?> type, final Method method) {
            return Identifier.classIdentifier(type);
        }
    },
    PROPERTY("Property") {
        @Override
        public Identifier identifierFor(final Class<?> type, final Method method) {
            return propertyOrCollectionIdentifierFor(type, method);
        }
    },
    COLLECTION("Collection") {
        @Override
        public Identifier identifierFor(final Class<?> type, final Method method) {
            return propertyOrCollectionIdentifierFor(type, method);
        }
    },
    ACTION("Action") {
        @Override
        public Identifier identifierFor(final Class<?> type, final Method method) {
            final String fullMethodName = method.getName();
            final Class<?>[] parameterTypes = method.getParameterTypes();
            return Identifier.actionIdentifier(type.getName(), fullMethodName, parameterTypes);
        }
    },
    ACTION_PARAMETER("Parameter") {
        /**
         * Always returns <tt>null</tt>.
         */
        @Override
        public Identifier identifierFor(final Class<?> type, final Method method) {
            return null;
        }
    };

    public final static List<FeatureType> COLLECTIONS_ONLY = ImmutableList.of(COLLECTION);
    public final static List<FeatureType> ACTIONS_ONLY = ImmutableList.of(ACTION);
    public final static List<FeatureType> PARAMETERS_ONLY = ImmutableList.of(ACTION_PARAMETER);
    public final static List<FeatureType> PROPERTIES_ONLY = ImmutableList.of(PROPERTY);
    public final static List<FeatureType> OBJECTS_ONLY = ImmutableList.of(OBJECT);
    public final static List<FeatureType> MEMBERS = ImmutableList.of(PROPERTY, COLLECTION, ACTION);
    public final static List<FeatureType> OBJECTS_PROPERTIES_AND_COLLECTIONS = ImmutableList.of(OBJECT, PROPERTY, COLLECTION);

    /**
     * Use of this is discouraged; instead use multiple {@link FacetFactory}s
     * for different features.
     */
    public final static List<FeatureType> EVERYTHING_BUT_PARAMETERS = ImmutableList.of(OBJECT, PROPERTY, COLLECTION, ACTION);
    /**
     * Use of this is discouraged; instead use multiple {@link FacetFactory}s
     * for different features.
     */
    public final static List<FeatureType> EVERYTHING = ImmutableList.of(OBJECT, PROPERTY, COLLECTION, ACTION, ACTION_PARAMETER);

    private final String name;

    private FeatureType(final String name) {
        this.name = name;
    }

    private static Identifier propertyOrCollectionIdentifierFor(final Class<?> type, final Method method) {
        final String capitalizedName = NameUtils.javaBaseName(method.getName());
        final String beanName = Introspector.decapitalize(capitalizedName);
        return Identifier.propertyOrCollectionIdentifier(type.getName(), beanName);
    }

    public boolean isProperty() {
        return this == PROPERTY;
    }

    public boolean isCollection() {
        return this == COLLECTION;
    }

    public boolean isAction() {
        return this == ACTION;
    }

    public boolean isActionParameter() {
        return this == ACTION_PARAMETER;
    }

    /**
     * Convenience.
     */
    public boolean isPropertyOrCollection() {
        return isProperty() || isCollection();
    }

    public abstract Identifier identifierFor(Class<?> type, Method method);

    @Override
    public String toString() {
        return name;
    }

}
