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
package org.apache.causeway.core.metamodel.facetapi;

import java.beans.Introspector;
import java.lang.reflect.Method;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.core.metamodel.commons.StringExtensions;
import org.apache.causeway.core.metamodel.facets.FacetFactory;

/**
 * Enumerates the features that a particular Facet can be applied to.
 *
 * <p>
 * The class-level feature processing is typically performed by {@link FacetFactory}s
 * pertaining to {@link #OBJECT}, performed before the processing of class members.
 *
 */
public enum FeatureType {

    OBJECT("Object") {
        /**
         * The supplied method can be null; at any rate it will be ignored.
         */
        @Override
        public Identifier identifierFor(final LogicalType typeIdentifier, final Method method) {
            return Identifier.classIdentifier(typeIdentifier);
        }
    },
    PROPERTY("Property") {
        @Override
        public Identifier identifierFor(final LogicalType typeIdentifier, final Method method) {
            return propertyIdentifierFor(typeIdentifier, method);
        }
    },
    COLLECTION("Collection") {
        @Override
        public Identifier identifierFor(final LogicalType typeIdentifier, final Method method) {
            return collectionIdentifierFor(typeIdentifier, method);
        }
    },
    ACTION("Action") {
        @Override
        public Identifier identifierFor(final LogicalType typeIdentifier, final Method method) {
            final String fullMethodName = method.getName();
            final Class<?>[] parameterTypes = method.getParameterTypes();
            return Identifier.actionIdentifier(typeIdentifier, fullMethodName, parameterTypes);
        }
    },
    ACTION_PARAMETER_SINGULAR("Singular Parameter") {
        /**
         * Always returns <tt>null</tt>.
         */
        @Override
        public Identifier identifierFor(final LogicalType typeIdentifier, final Method method) {
            return null;
        }
    },
    ACTION_PARAMETER_PLURAL("Plural Parameter") {
        /**
         * Always returns <tt>null</tt>.
         */
        @Override
        public Identifier identifierFor(final LogicalType typeIdentifier, final Method method) {
            return null;
        }
    };

    public static final ImmutableEnumSet<FeatureType> COLLECTIONS_ONLY = ImmutableEnumSet.of(COLLECTION);
    public static final ImmutableEnumSet<FeatureType> COLLECTIONS_AND_ACTIONS = ImmutableEnumSet.of(COLLECTION, ACTION);
    public static final ImmutableEnumSet<FeatureType> ACTIONS_ONLY = ImmutableEnumSet.of(ACTION);
    public static final ImmutableEnumSet<FeatureType> PARAMETERS_ONLY = ImmutableEnumSet.of(ACTION_PARAMETER_SINGULAR, ACTION_PARAMETER_PLURAL);
    public static final ImmutableEnumSet<FeatureType> PROPERTIES_ONLY = ImmutableEnumSet.of(PROPERTY);
    public static final ImmutableEnumSet<FeatureType> PROPERTIES_AND_ACTIONS = ImmutableEnumSet.of(PROPERTY, ACTION);
    public static final ImmutableEnumSet<FeatureType> OBJECTS_ONLY = ImmutableEnumSet.of(OBJECT);
    public static final ImmutableEnumSet<FeatureType> MEMBERS = ImmutableEnumSet.of(PROPERTY, COLLECTION, ACTION);
    public static final ImmutableEnumSet<FeatureType> OBJECTS_AND_PROPERTIES = ImmutableEnumSet.of(OBJECT, PROPERTY);
    public static final ImmutableEnumSet<FeatureType> PROPERTIES_AND_COLLECTIONS = ImmutableEnumSet.of(PROPERTY, COLLECTION);
    public static final ImmutableEnumSet<FeatureType> OBJECTS_AND_COLLECTIONS = ImmutableEnumSet.of(OBJECT, COLLECTION);
    public static final ImmutableEnumSet<FeatureType> OBJECTS_AND_ACTIONS = ImmutableEnumSet.of(OBJECT, ACTION);
    public static final ImmutableEnumSet<FeatureType> OBJECTS_PROPERTIES_AND_COLLECTIONS = ImmutableEnumSet.of(OBJECT, PROPERTY, COLLECTION);
    public static final ImmutableEnumSet<FeatureType> ACTIONS_AND_PARAMETERS = ImmutableEnumSet.of(ACTION, ACTION_PARAMETER_SINGULAR, ACTION_PARAMETER_PLURAL);

    /**
     * Use of this is discouraged; instead use multiple {@link FacetFactory}s
     * for different features.
     */
    public static final ImmutableEnumSet<FeatureType> EVERYTHING_BUT_PARAMETERS =
            ImmutableEnumSet.complementOf(
                    ImmutableEnumSet.of(ACTION_PARAMETER_SINGULAR, ACTION_PARAMETER_PLURAL));
    /**
     * Use of this is discouraged; instead use multiple {@link FacetFactory}s
     * for different features.
     */
    public static final ImmutableEnumSet<FeatureType> EVERYTHING = ImmutableEnumSet.allOf(FeatureType.class);

    private final String name;

    private FeatureType(final String name) {
        this.name = name;
    }

    private static Identifier propertyIdentifierFor(
            final LogicalType typeIdentifier,
            final Method method) {
        final String capitalizedName = StringExtensions.asJavaBaseName(method.getName());
        final String beanName = Introspector.decapitalize(capitalizedName);
        return Identifier.propertyIdentifier(typeIdentifier, beanName);
    }

    private static Identifier collectionIdentifierFor(
            final LogicalType typeIdentifier,
            final Method method) {
        final String capitalizedName = StringExtensions.asJavaBaseName(method.getName());
        final String beanName = Introspector.decapitalize(capitalizedName);
        return Identifier.collectionIdentifier(typeIdentifier, beanName);
    }

    public boolean isProperty() { return this == PROPERTY; }
    public boolean isCollection() { return this == COLLECTION; }
    public boolean isAction() { return this == ACTION; }
    public boolean isActionParameter() {
        return this == ACTION_PARAMETER_SINGULAR
                || this == ACTION_PARAMETER_PLURAL;
    }

    /**
     * Convenience.
     */
    public boolean isPropertyOrCollection() {
        return isProperty() || isCollection();
    }

    public abstract Identifier identifierFor(LogicalType typeIdentifier, Method method);

    @Override
    public String toString() {
        return name;
    }

}
