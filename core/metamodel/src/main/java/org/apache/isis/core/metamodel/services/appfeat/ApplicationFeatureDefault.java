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
import java.util.Optional;
import java.util.OptionalInt;
import java.util.SortedSet;

import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Value;
import org.apache.isis.applib.services.appfeat.ApplicationFeature;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.isis.applib.services.appfeat.ApplicationMemberSort;
import org.apache.isis.applib.util.Equality;
import org.apache.isis.applib.util.Hashing;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ToString;
import org.apache.isis.commons.internal.collections._Sets;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Canonical application feature, identified by {@link ApplicationFeatureId},
 * and wired together with other application features and cached by {@link ApplicationFeatureRepository}.
 *
 * <p>
 *     Note that this is NOT a view model; instead it can be converted to a string using methods of
 *     {@link ApplicationFeatureRepository}.
 * </p>
 */
@Value
public class ApplicationFeatureDefault
implements
ApplicationFeature,
Comparable<ApplicationFeature> {

    // -- CONSTRUCTORS

    public ApplicationFeatureDefault(final @NonNull ApplicationFeatureId featureId) {
        this.featureId = featureId;
    }

    // -- FIELDS

    @Getter(onMethod_ = {@Override})
    private final @NonNull ApplicationFeatureId featureId;

    @Getter(onMethod_ = {@Override})
    @Setter(AccessLevel.PACKAGE)
    private @NonNull Optional<ApplicationMemberSort> memberSort = Optional.empty();

    @Getter(onMethod_ = {@Override})
    @Setter(AccessLevel.PACKAGE)
    private @NonNull Optional<Class<?>> actionReturnType = Optional.empty();

    @Getter(onMethod_ = {@Override})
    @Setter(AccessLevel.PACKAGE)
    private @NonNull Optional<SemanticsOf> actionSemantics = Optional.empty();

    @Getter(onMethod_ = {@Override})
    @Setter(AccessLevel.PACKAGE)
    private boolean propertyOrCollectionDerived = false;

    @Getter(onMethod_ = {@Override})
    @Setter(AccessLevel.PACKAGE)
    private @NonNull OptionalInt propertyMaxLength = OptionalInt.empty();

    @Getter(onMethod_ = {@Override})
    @Setter(AccessLevel.PACKAGE)
    private @NonNull OptionalInt propertyTypicalLength = OptionalInt.empty();

    @Getter(onMethod_ = {@Override})
    private final SortedSet<ApplicationFeatureId> contents = _Sets.newTreeSet();

    @Getter(onMethod_ = {@Override})
    private final SortedSet<ApplicationFeatureId> properties = _Sets.newTreeSet();

    @Getter(onMethod_ = {@Override})
    private final SortedSet<ApplicationFeatureId> collections = _Sets.newTreeSet();

    @Getter(onMethod_ = {@Override})
    private final SortedSet<ApplicationFeatureId> actions = _Sets.newTreeSet();

    // -- PACKAGE PRIVATE ACCESS

    void addToContents(final ApplicationFeatureId contentId) {
        _Asserts.ensureNamespace(this.getFeatureId());
        _Asserts.ensureNamespaceOrType(contentId);
        this.contents.add(contentId);
    }

    void addToMembers(final ApplicationFeatureId memberId, final ApplicationMemberSort memberSort) {
        _Asserts.ensureType(this.getFeatureId());
        _Asserts.ensureMember(memberId);

        getMembersOfSort(memberSort).add(memberId);
    }

    // -- OBJECT CONTRACT

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
