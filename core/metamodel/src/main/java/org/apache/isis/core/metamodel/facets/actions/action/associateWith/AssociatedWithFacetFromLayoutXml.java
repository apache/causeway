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

package org.apache.isis.core.metamodel.facets.actions.action.associateWith;

import javax.annotation.Nullable;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.members.layout.group.GroupIdAndName;

import lombok.NonNull;

public class AssociatedWithFacetFromLayoutXml extends AssociatedWithFacetAbstract {

    // -- FACTORIES

    public static @Nullable AssociatedWithFacetFromLayoutXml create(
            final @Nullable GroupIdAndName groupIdAndName,
            final @NonNull  FacetHolder holder) {

        return groupIdAndName!=null
                ? new AssociatedWithFacetFromLayoutXml(groupIdAndName.getId(), holder)
                : null;
    }

//    public static @Nullable AssociatedWithFacetFromLayoutXml create(
//            final @NonNull FieldSet fieldSet,
//            final @NonNull FacetHolder holder) {
//
//        return GroupIdAndName.forFieldSet(fieldSet)
//            .map(groupIdAndName->create(groupIdAndName, holder))
//            .orElse(null);
//    }

    // -- IMPLEMENTATION

    private AssociatedWithFacetFromLayoutXml(
            final String value,
            final FacetHolder holder) {
        super(value, holder);
    }

}
