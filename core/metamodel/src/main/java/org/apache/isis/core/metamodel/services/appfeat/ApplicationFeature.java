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

import java.util.SortedSet;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import org.apache.isis.applib.IsisApplibModule;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.isis.applib.services.appfeat.ApplicationMemberType;
import org.apache.isis.applib.util.ObjectContracts;

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

    public static abstract class PropertyDomainEvent<T> extends IsisApplibModule.PropertyDomainEvent<ApplicationFeature, T> {}

    public static abstract class CollectionDomainEvent<T> extends IsisApplibModule.CollectionDomainEvent<ApplicationFeature, T> {}

    public static abstract class ActionDomainEvent extends IsisApplibModule.ActionDomainEvent<ApplicationFeature> {}

    //region > constants

    // using same value for all to neaten up rendering
    public static final int TYPICAL_LENGTH_PKG_FQN = 50;
    public static final int TYPICAL_LENGTH_CLS_NAME = 50;
    public static final int TYPICAL_LENGTH_MEMBER_NAME = 50;
    //endregion

    //region > constructors
    public ApplicationFeature() {
        this(null);
    }
    public ApplicationFeature(final ApplicationFeatureId featureId) {
        setFeatureId(featureId);
    }
    //endregion

    //region > featureId
    private ApplicationFeatureId featureId;

    @Programmatic
    public ApplicationFeatureId getFeatureId() {
        return featureId;
    }

    public void setFeatureId(final ApplicationFeatureId applicationFeatureId) {
        this.featureId = applicationFeatureId;
    }
    //endregion

    //region > memberType
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
    //endregion

    //region > returnTypeName (for: properties, collections, actions)
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
    //endregion

    //region > contributed (for: properties, collections, actions)
    private boolean contributed;

    @Programmatic
    public boolean isContributed() {
        return contributed;
    }

    public void setContributed(final boolean contributed) {
        this.contributed = contributed;
    }
    //endregion

    //region > derived (properties and collections)
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
    //endregion

    //region > propertyMaxLength (properties only)
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
    //endregion

    //region > propertyTypicalLength (properties only)
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
    //endregion

    //region > actionSemantics (actions only)
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
    //endregion

    //region > packages: Contents
    private final SortedSet<ApplicationFeatureId> contents = Sets.newTreeSet();

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
    //endregion

    //region > classes: Properties, Collections, Actions
    private final SortedSet<ApplicationFeatureId> properties = Sets.newTreeSet();

    @Programmatic
    public SortedSet<ApplicationFeatureId> getProperties() {
        ApplicationFeatureType.ensureClass(this.getFeatureId());
        return properties;
    }


    private final SortedSet<ApplicationFeatureId> collections = Sets.newTreeSet();
    @Programmatic
    public SortedSet<ApplicationFeatureId> getCollections() {
        ApplicationFeatureType.ensureClass(this.getFeatureId());
        return collections;
    }


    private final SortedSet<ApplicationFeatureId> actions = Sets.newTreeSet();

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
    //endregion

    //region > Functions

    public static class Functions {
        private Functions(){}

        public static final Function<? super ApplicationFeature, ? extends String> GET_FQN = new Function<ApplicationFeature, String>() {
            @Override
            public String apply(final ApplicationFeature input) {
                return input.getFeatureId().getFullyQualifiedName();
            }
        };

        public static final Function<ApplicationFeature, ApplicationFeatureId> GET_ID =
                new Function<ApplicationFeature, ApplicationFeatureId>() {
            @Override
            public ApplicationFeatureId apply(final ApplicationFeature input) {
                return input.getFeatureId();
            }
        };
    }

    public static class Predicates {
        private Predicates(){}

        public static Predicate<ApplicationFeature> packageContainingClasses(
                final ApplicationMemberType memberType, final ApplicationFeatureRepositoryDefault applicationFeatures) {
            return new Predicate<ApplicationFeature>() {
                @Override
                public boolean apply(final ApplicationFeature input) {
                    // all the classes in this package
                    final Iterable<ApplicationFeatureId> classIds =
                            Iterables.filter(input.getContents(),
                                    ApplicationFeatureId.Predicates.isClassContaining(memberType, applicationFeatures));
                    return classIds.iterator().hasNext();
                }
            };
        }
    }

    //endregion

    //region > equals, hashCode, compareTo, toString

    private final static String propertyNames = "featureId";

    @Override
    public int compareTo(final ApplicationFeature other) {
        return ObjectContracts.compare(this, other, propertyNames);
    }

    @Override
    public boolean equals(final Object obj) {
        return ObjectContracts.equals(this, obj, propertyNames);
    }

    @Override
    public int hashCode() {
        return ObjectContracts.hashCode(this, propertyNames);
    }

    @Override
    public String toString() {
        return ObjectContracts.toString(this, propertyNames);
    }

    //endregion

}
