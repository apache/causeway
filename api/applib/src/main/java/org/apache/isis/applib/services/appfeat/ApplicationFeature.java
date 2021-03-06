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
package org.apache.isis.applib.services.appfeat;

import java.util.Collections;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.SortedSet;

import org.apache.isis.applib.annotation.SemanticsOf;

/**
 * 
 * @since 1.x revised for 2.0 {@index}
 */
public interface ApplicationFeature {

    ApplicationFeatureId getFeatureId();
    
    default String getFullyQualifiedName() {
        return getFeatureId().getFullyQualifiedName();
    }

    /**
     * Returns optionally the member sort, based on whether this feature is of sort 
     * {@link ApplicationFeatureSort#MEMBER}.
     */
    Optional<ApplicationMemberSort> getMemberSort();
    
    default SortedSet<ApplicationFeatureId> getMembersOfSort(final ApplicationMemberSort memberSort) {
        switch (memberSort) {
        case PROPERTY:
            return getProperties();
        case COLLECTION:
            return getCollections();
        case ACTION:
            return getActions();
        default:
            return Collections.emptySortedSet();
        }
    }
    
    /**
     * Returns optionally the action's return type, based on
     * whether this feature is of sorts
     * {@link ApplicationFeatureSort#MEMBER member} and 
     * {@link ApplicationMemberSort#ACTION action}.
     */
    Optional<Class<?>> getActionReturnType();
    
    /**
     * Returns optionally the action's semantics, based on
     * whether this feature is of sorts
     * {@link ApplicationFeatureSort#MEMBER member} and 
     * {@link ApplicationMemberSort#ACTION action}.
     */
    Optional<SemanticsOf> getActionSemantics();

    /** 
     * Returns whether the property or collection feature is derived.  
     * @return always {@code false} when not a property or collection 
     */
    boolean isPropertyOrCollectionDerived();

    /**
     * Returns optionally the property's semantics, based on
     * whether this feature is of sorts
     * {@link ApplicationFeatureSort#MEMBER member} and 
     * {@link ApplicationMemberSort#PROPERTY property}.
     */
    OptionalInt getPropertyTypicalLength();

    /**
     * Returns optionally the property's max-length constraint, based on
     * whether this feature is of sorts
     * {@link ApplicationFeatureSort#MEMBER member} and 
     * {@link ApplicationMemberSort#PROPERTY property}.
     */
    OptionalInt getPropertyMaxLength();
    
    SortedSet<ApplicationFeatureId> getContents();

    SortedSet<ApplicationFeatureId> getProperties();

    SortedSet<ApplicationFeatureId> getCollections();

    SortedSet<ApplicationFeatureId> getActions();
    
}
