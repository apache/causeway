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

import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.annotation.Value;
import org.apache.isis.applib.services.appfeat.ApplicationFeature;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureId;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository;
import org.apache.isis.applib.services.appfeat.ApplicationFeatureSort;
import org.apache.isis.applib.services.appfeat.ApplicationMemberSort;
import org.apache.isis.applib.util.Equality;
import org.apache.isis.applib.util.Hashing;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ToString;
import org.apache.isis.commons.internal.collections._Sets;

import lombok.Getter;
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
    
    public ApplicationFeatureDefault(final ApplicationFeatureId featureId) {
        this.featureId = featureId;
    }

    @Getter
    private final ApplicationFeatureId featureId;

    /**
     * Only for {@link ApplicationFeatureSort#MEMBER member}s.
     */
    @Getter @Setter
    private ApplicationMemberSort memberSort;

    /**
     * Only for {@link ApplicationMemberSort#ACTION action}s.
     */
    @Getter @Setter
    private String returnTypeName;

    /**
     * Only for {@link ApplicationMemberSort#PROPERTY} and {@link ApplicationMemberSort#COLLECTION}
     */
    @Getter @Setter
    private Boolean derived;

    /**
     * Only for {@link ApplicationMemberSort#ACTION action}s.
     */
    @Getter @Setter
    private Integer propertyMaxLength;
    
    /**
     * Only for {@link ApplicationMemberSort#ACTION action}s.
     */
    @Getter @Setter
    private Integer propertyTypicalLength;

    /**
     * Only for {@link ApplicationMemberSort#ACTION action}s.
     */
    @Getter @Setter
    private SemanticsOf actionSemantics;

    // -- packages: Contents
    private final SortedSet<ApplicationFeatureId> contents = _Sets.newTreeSet();

    @Override
    public SortedSet<ApplicationFeatureId> getContents() {
        _Asserts.ensureNamespace(this.getFeatureId());
        return contents;
    }

    public void addToContents(final ApplicationFeatureId contentId) {
        _Asserts.ensureNamespace(this.getFeatureId());
        _Asserts.ensureNamespaceOrType(contentId);
        this.contents.add(contentId);
    }


    // -- classes: Properties, Collections, Actions
    private final SortedSet<ApplicationFeatureId> properties = _Sets.newTreeSet();

    public SortedSet<ApplicationFeatureId> getProperties() {
        _Asserts.ensureType(this.getFeatureId());
        return properties;
    }


    private final SortedSet<ApplicationFeatureId> collections = _Sets.newTreeSet();
    
    public SortedSet<ApplicationFeatureId> getCollections() {
        _Asserts.ensureType(this.getFeatureId());
        return collections;
    }


    private final SortedSet<ApplicationFeatureId> actions = _Sets.newTreeSet();
    
    public SortedSet<ApplicationFeatureId> getActions() {
        _Asserts.ensureType(this.getFeatureId());
        return actions;
    }
    
    public void addToMembers(final ApplicationFeatureId memberId, final ApplicationMemberSort memberSort) {
        _Asserts.ensureType(this.getFeatureId());
        _Asserts.ensureMember(memberId);

        membersOfSort(memberSort).add(memberId);
    }
    
    @Override
    public SortedSet<ApplicationFeatureId> membersOfSort(final ApplicationMemberSort memberSort) {
        _Asserts.ensureType(this.getFeatureId());
        switch (memberSort) {
        case PROPERTY:
            return properties;
        case COLLECTION:
            return collections;
        default: // case ACTION:
            return actions;
        }
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
