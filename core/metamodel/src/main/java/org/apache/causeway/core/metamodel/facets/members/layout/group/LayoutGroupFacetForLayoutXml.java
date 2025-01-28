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
package org.apache.causeway.core.metamodel.facets.members.layout.group;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.layout.component.FieldSet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

import org.jspecify.annotations.NonNull;

public class LayoutGroupFacetForLayoutXml
extends LayoutGroupFacetAbstract {

    // -- FACTORIES

    public static Optional<LayoutGroupFacetForLayoutXml> create(
            final @Nullable GroupIdAndName groupIdAndName,
            final @NonNull  FacetHolder holder,
            final Precedence precedence) {

        return Optional.ofNullable(groupIdAndName)
                .map(gIdAndName->new LayoutGroupFacetForLayoutXml(gIdAndName, holder, precedence));
    }

    public static Optional<LayoutGroupFacetForLayoutXml> create(
            final @NonNull FieldSet fieldSet,
            final @NonNull FacetHolder holder,
            final Precedence precedence) {

        return GroupIdAndName
                .forFieldSet(fieldSet)
                .flatMap(groupIdAndName->create(groupIdAndName, holder, precedence));
    }

    // -- IMPLEMENTATION

    private LayoutGroupFacetForLayoutXml(
            final GroupIdAndName groupIdAndName,
            final FacetHolder holder,
            final Precedence precedence) {
        super(groupIdAndName, holder, precedence);
    }

    @Override
    public boolean isObjectTypeSpecific() {
        return true;
    }

    @Override
    public boolean isExplicitBinding() {
        return true;
    }

}
