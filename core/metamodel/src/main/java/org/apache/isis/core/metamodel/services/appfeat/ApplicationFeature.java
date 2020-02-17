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
package org.apache.isis.core.metamodel.services.appfeat;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.enterprise.inject.Vetoed;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Value;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.isis.applib.services.appfeat.ApplicationMemberType;
import org.apache.isis.applib.util.Equality;
import org.apache.isis.applib.util.Hashing;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ToString;
import org.apache.isis.core.commons.internal.collections._Sets;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

/**
 * Canonical application feature, identified by {@link ApplicationFeatureId},
 * and wired together with other application features and cached by {@link ApplicationFeatureRepository}.
 *
 * <p>
 *     Note that this is NOT a view model; instead it can be converted to a string using methods of
 *     {@link ApplicationFeatureRepository}, eg {@link ApplicationFeatureRepository#classNamesContainedIn(String, ApplicationMemberType)}.
 * </p>
 */
@Value
public class ApplicationFeature implements Comparable<ApplicationFeature> {

    public static abstract class PropertyDomainEvent<T> 
    extends IsisModuleApplib.PropertyDomainEvent<ApplicationFeature, T> {}

    public static abstract class CollectionDomainEvent<T> 
    extends IsisModuleApplib.CollectionDomainEvent<ApplicationFeature, T> {}

    public static abstract class ActionDomainEvent 
    extends IsisModuleApplib.ActionDomainEvent<ApplicationFeature> {}

    // -- constants

    // using same value for all to neaten up rendering
    public static final int TYPICAL_LENGTH_PKG_FQN = 50;
    public static final int TYPICAL_LENGTH_CLS_NAME = 50;
    public static final int TYPICAL_LENGTH_MEMBER_NAME = 50;


    // -- constructors
    public ApplicationFeature() {
        this(null);
    }
    public ApplicationFeature(final ApplicationFeatureId featureId) {
        setFeatureId(featureId);
    }

    @Getter @Setter
    private ApplicationFeatureId featureId;

    /**
     * Only for {@link ApplicationFeatureType#MEMBER member}s.
     */
    @Getter @Setter
    private ApplicationMemberType memberType;

    /**
     * Only for {@link ApplicationMemberType#ACTION action}s.
     */
    @Getter @Setter
    private String returnTypeName;

    @Getter @Setter
    private boolean contributed;

    /**
     * Only for {@link ApplicationMemberType#PROPERTY} and {@link ApplicationMemberType#COLLECTION}
     */
    @Getter @Setter
    private Boolean derived;

    /**
     * Only for {@link ApplicationMemberType#ACTION action}s.
     */
    @Getter @Setter
    private Integer propertyMaxLength;
    
    /**
     * Only for {@link ApplicationMemberType#ACTION action}s.
     */
    @Getter @Setter
    private Integer propertyTypicalLength;

    /**
     * Only for {@link ApplicationMemberType#ACTION action}s.
     */
    @Getter @Setter
    private SemanticsOf actionSemantics;

    // -- packages: Contents
    private final SortedSet<ApplicationFeatureId> contents = _Sets.newTreeSet();

    public SortedSet<ApplicationFeatureId> getContents() {
        ApplicationFeatureType.ensurePackage(this.getFeatureId());
        return contents;
    }

    public void addToContents(final ApplicationFeatureId contentId) {
        ApplicationFeatureType.ensurePackage(this.getFeatureId());
        ApplicationFeatureType.ensurePackageOrClass(contentId);
        this.contents.add(contentId);
    }


    // -- classes: Properties, Collections, Actions
    private final SortedSet<ApplicationFeatureId> properties = _Sets.newTreeSet();

    public SortedSet<ApplicationFeatureId> getProperties() {
        ApplicationFeatureType.ensureClass(this.getFeatureId());
        return properties;
    }


    private final SortedSet<ApplicationFeatureId> collections = _Sets.newTreeSet();
    
    public SortedSet<ApplicationFeatureId> getCollections() {
        ApplicationFeatureType.ensureClass(this.getFeatureId());
        return collections;
    }


    private final SortedSet<ApplicationFeatureId> actions = _Sets.newTreeSet();
    
    public SortedSet<ApplicationFeatureId> getActions() {
        ApplicationFeatureType.ensureClass(this.getFeatureId());
        return actions;
    }
    
    public void addToMembers(final ApplicationFeatureId memberId, final ApplicationMemberType memberType) {
        ApplicationFeatureType.ensureClass(this.getFeatureId());
        ApplicationFeatureType.ensureMember(memberId);

        membersOf(memberType).add(memberId);
    }
    
    public SortedSet<ApplicationFeatureId> membersOf(final ApplicationMemberType memberType) {
        ApplicationFeatureType.ensureClass(this.getFeatureId());
        switch (memberType) {
        case PROPERTY:
            return properties;
        case COLLECTION:
            return collections;
        default: // case ACTION:
            return actions;
        }
    }

    // -- Functions

    @Vetoed @UtilityClass
    public static class Functions {

        public static final Function<ApplicationFeature, String> GET_FQN = 
                (ApplicationFeature input)->input.getFeatureId().getFullyQualifiedName();

    }

    @Vetoed @UtilityClass
    public static class Predicates {

        public static Predicate<ApplicationFeature> packageContainingClasses(
                final ApplicationMemberType memberType, 
                final ApplicationFeatureRepositoryDefault applicationFeatures) {

            return (final ApplicationFeature input) ->
            input.getContents().stream() // all the classes in this package
            .anyMatch(ApplicationFeatureId.Predicates.isClassContaining(memberType, applicationFeatures));
        }
    }

    // -- equals, hashCode, compareTo, toString

    private static final Comparator<ApplicationFeature> comparator =
            Comparator.comparing(ApplicationFeature::getFeatureId);

    private static final Equality<ApplicationFeature> equality =
            ObjectContracts.checkEquals(ApplicationFeature::getFeatureId);

    private static final Hashing<ApplicationFeature> hashing =
            ObjectContracts.hashing(ApplicationFeature::getFeatureId);

    private static final ToString<ApplicationFeature> toString =
            ObjectContracts.toString("featureId", ApplicationFeature::getFeatureId);

    @Override
    public int compareTo(final ApplicationFeature other) {
        return comparator.compare(this, other);
    }

    @Override
    public boolean equals(final Object obj) {
        return equality.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return hashing.hashCode(this);
    }

    @Override
    public String toString() {
        return toString.toString(this);
    }



}
