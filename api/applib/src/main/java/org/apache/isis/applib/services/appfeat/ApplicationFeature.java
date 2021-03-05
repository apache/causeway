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

import java.util.Optional;
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
     * Optionally the member sort, based on whether this feature is a member.
     */
    Optional<ApplicationMemberSort> getMemberSort();
    
    SortedSet<ApplicationFeatureId> membersOfSort(ApplicationMemberSort memberSort);
    
    SortedSet<ApplicationFeatureId> getContents();

    SortedSet<ApplicationFeatureId> getProperties();

    SortedSet<ApplicationFeatureId> getCollections();

    SortedSet<ApplicationFeatureId> getActions();

    // -- TODO probably non formal API, only used by secman ...

    String getReturnTypeName();

    SemanticsOf getActionSemantics();

    Boolean getDerived();

    Integer getPropertyTypicalLength();

    Integer getPropertyMaxLength();
    
}
