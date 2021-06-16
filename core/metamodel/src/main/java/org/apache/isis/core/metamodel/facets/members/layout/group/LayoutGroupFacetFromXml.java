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
package org.apache.isis.core.metamodel.facets.members.layout.group;

import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.isis.applib.layout.component.FieldSet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

import lombok.NonNull;

public class LayoutGroupFacetFromXml
extends LayoutGroupFacetAbstract {

    // -- FACTORIES

    public static Optional<LayoutGroupFacetFromXml> create(
            final @Nullable GroupIdAndName groupIdAndName,
            final @NonNull  FacetHolder holder) {

        return Optional.ofNullable(groupIdAndName)
                .map(gIdAndName->new LayoutGroupFacetFromXml(gIdAndName, holder));
    }

    public static Optional<LayoutGroupFacetFromXml> create(
            final @NonNull FieldSet fieldSet,
            final @NonNull FacetHolder holder) {

        return GroupIdAndName
                .forFieldSet(fieldSet)
                .flatMap(groupIdAndName->create(groupIdAndName, holder));
    }

    // -- IMPLEMENTATION

    private LayoutGroupFacetFromXml(final GroupIdAndName groupIdAndName, final FacetHolder holder) {
        super(groupIdAndName, holder);
    }

    @Override
    public boolean isExplicitBinding() {
        return true;
    }


}
