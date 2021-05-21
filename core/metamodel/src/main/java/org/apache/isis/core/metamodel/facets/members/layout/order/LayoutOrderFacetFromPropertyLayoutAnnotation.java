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

package org.apache.isis.core.metamodel.facets.members.layout.order;

import java.util.Optional;

import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

public class LayoutOrderFacetFromPropertyLayoutAnnotation
extends LayoutOrderFacetAbstract {

    public static LayoutOrderFacetFromPropertyLayoutAnnotation create(
            final Optional<PropertyLayout> actionLayoutIfAny,
            final FacetHolder holder) {

        return actionLayoutIfAny
                .map(PropertyLayout::sequence)
                .map(sequence -> new LayoutOrderFacetFromPropertyLayoutAnnotation(sequence, holder))
                .orElse(null);
    }

    public LayoutOrderFacetFromPropertyLayoutAnnotation(final String sequence, final FacetHolder holder) {
        super(sequence, holder);
    }

}
