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
package org.apache.isis.metamodel.services.appfeat;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.enterprise.inject.Vetoed;

import org.apache.isis.applib.IsisApplibModule;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.isis.applib.services.appfeat.ApplicationMemberType;
import org.apache.isis.applib.util.Equality;
import org.apache.isis.applib.util.Hashing;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ToString;
import org.apache.isis.commons.internal.collections._Sets;

/**
 * Canonical application feature, identified by {@link ApplicationFeatureId},
 * and wired together with other application features and cached by {@link ApplicationFeatureRepository}.
 *
 * <p>
 *     Note that this is NOT a view model; instead it can be converted to a string using methods of
 *     {@link ApplicationFeatureRepository}, eg {@link ApplicationFeatureRepository#classNamesContainedIn(String, ApplicationMemberType)}.
 * </p>
 */
public class ApplicationFeature implements Comparable<ApplicationFeature> {

    public static abstract class PropertyDomainEvent<T> 
    extends IsisApplibModule.PropertyDomainEvent<ApplicationFeature, T> {
        private static final long serialVersionUID = 1L;
    }

    public static abstract class CollectionDomainEvent<T> 
    extends IsisApplibModule.CollectionDomainEvent<ApplicationFeature, T> {
        private static final long serialVersionUID = 1L;
    }

    public static abstract class ActionDomainEvent 
    extends IsisApplibModule.ActionDomainEvent<ApplicationFeature> {
        private static final long serialVersionUID = 1L;
    }

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


    // -- featureId
    private ApplicationFeatureId featureId;

    @Programmatic
    public ApplicationFeatureId getFeatureId() {
        return featureId;
    }

    public void setFeatureId(final ApplicationFeatureId applicationFeatureId) {
        this.featureId = applicationFeatureId;
    }


    // -- memberType
    private ApplicationMemberType memberType;

    /**
     * Only for {@link ApplicationFeatureType#MEMBER member}s.
     */
    @Programmatic
    public ApplicationMemberType getMemberType() {
        return memberType;
    }

    public void setMemberType(final ApplicationMemberType memberType) {
        this.memberType = memberType;
    }


    // -- returnTypeName (for: properties, collections, actions)
    private String returnTypeName;

    /**
     * Only for {@link ApplicationMemberType#ACTION action}s.
     */
    @Programmatic
    public String getReturnTypeName() {
        return returnTypeName;
    }

    public void setReturnTypeName(final String returnTypeName) {
        this.returnTypeName = returnTypeName;
    }


    // -- contributed (for: properties, collections, actions)
    private boolean contributed;

    @Programmatic
    public boolean isContributed() {
        return contributed;
    }

    public void setContributed(final boolean contributed) {
        this.contributed = contributed;
    }


    // -- derived (properties and collections)
    private Boolean derived;

    /**
     * Only for {@link ApplicationMemberType#PROPERTY} and {@link ApplicationMemberType#COLLECTION}
     */
    @Programmatic
    public Boolean isDerived() {
        return derived;
    }

    public void setDerived(final Boolean derived) {
        this.derived = derived;
    }


    // -- propertyMaxLength (properties only)
    private Integer propertyMaxLength;

    /**
     * Only for {@link ApplicationMemberType#ACTION action}s.
     */
    @Programmatic
    public Integer getPropertyMaxLength() {
        return propertyMaxLength;
    }

    public void setPropertyMaxLength(final Integer propertyMaxLength) {
        this.propertyMaxLength = propertyMaxLength;
    }


    // -- propertyTypicalLength (properties only)
    private Integer propertyTypicalLength;

    /**
     * Only for {@link ApplicationMemberType#ACTION action}s.
     */
    @Programmatic
    public Integer getPropertyTypicalLength() {
        return propertyTypicalLength;
    }

    public void setPropertyTypicalLength(final Integer propertyTypicalLength) {
        this.propertyTypicalLength = propertyTypicalLength;
    }


    // -- actionSemantics (actions only)
    private SemanticsOf actionSemantics;

    /**
     * Only for {@link ApplicationMemberType#ACTION action}s.
     */
    @Programmatic
    public SemanticsOf getActionSemantics() {
        return actionSemantics;
    }

    public void setActionSemantics(final SemanticsOf actionSemantics) {
        this.actionSemantics = actionSemantics;
    }


    // -- packages: Contents
    private final SortedSet<ApplicationFeatureId> contents = _Sets.newTreeSet();

    @Programmatic
    public SortedSet<ApplicationFeatureId> getContents() {
        ApplicationFeatureType.ensurePackage(this.getFeatureId());
        return contents;
    }

    @Programmatic
    public void addToContents(final ApplicationFeatureId contentId) {
        ApplicationFeatureType.ensurePackage(this.getFeatureId());
        ApplicationFeatureType.ensurePackageOrClass(contentId);
        this.contents.add(contentId);
    }


    // -- classes: Properties, Collections, Actions
    private final SortedSet<ApplicationFeatureId> properties = _Sets.newTreeSet();

    @Programmatic
    public SortedSet<ApplicationFeatureId> getProperties() {
        ApplicationFeatureType.ensureClass(this.getFeatureId());
        return properties;
    }


    private final SortedSet<ApplicationFeatureId> collections = _Sets.newTreeSet();
    @Programmatic
    public SortedSet<ApplicationFeatureId> getCollections() {
        ApplicationFeatureType.ensureClass(this.getFeatureId());
        return collections;
    }


    private final SortedSet<ApplicationFeatureId> actions = _Sets.newTreeSet();

    @Programmatic
    public SortedSet<ApplicationFeatureId> getActions() {
        ApplicationFeatureType.ensureClass(this.getFeatureId());
        return actions;
    }

    @Programmatic
    public void addToMembers(final ApplicationFeatureId memberId, final ApplicationMemberType memberType) {
        ApplicationFeatureType.ensureClass(this.getFeatureId());
        ApplicationFeatureType.ensureMember(memberId);

        membersOf(memberType).add(memberId);
    }

    @Programmatic
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

    @Vetoed
    public static class Functions {
        private Functions(){}

        public static final Function<ApplicationFeature, String> GET_FQN = 
                (ApplicationFeature input)->input.getFeatureId().getFullyQualifiedName();

    }

    @Vetoed
    public static class Predicates {
        private Predicates(){}

        public static Predicate<ApplicationFeature> packageContainingClasses(
                final ApplicationMemberType memberType, 
                final ApplicationFeatureRepositoryDefault applicationFeatures) {
            
            return (final ApplicationFeature input) ->
                    input.getContents().stream() // all the classes in this package
                    .anyMatch(ApplicationFeatureId.Predicates.isClassContaining(memberType, applicationFeatures));
        }
    }

    // -- equals, hashCode, compareTo, toString

    private final static Comparator<ApplicationFeature> comparator =
            Comparator.comparing(ApplicationFeature::getFeatureId);

    private final static Equality<ApplicationFeature> equality =
            ObjectContracts.checkEquals(ApplicationFeature::getFeatureId);

    private final static Hashing<ApplicationFeature> hashing =
            ObjectContracts.hashing(ApplicationFeature::getFeatureId);

    private final static ToString<ApplicationFeature> toString =
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
