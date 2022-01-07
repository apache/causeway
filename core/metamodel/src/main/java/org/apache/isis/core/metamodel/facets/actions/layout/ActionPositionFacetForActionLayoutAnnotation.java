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
package org.apache.isis.core.metamodel.facets.actions.layout;

import java.util.Optional;

import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.actions.position.ActionPositionFacet;
import org.apache.isis.core.metamodel.facets.actions.position.ActionPositionFacetAbstract;

public class ActionPositionFacetForActionLayoutAnnotation extends ActionPositionFacetAbstract {

    public static Optional<ActionPositionFacet> create(
            final Optional<ActionLayout> actionLayoutIfAny,
            final FacetHolder holder) {

        return actionLayoutIfAny
                .map(ActionLayout::position)
                .filter(position -> position != ActionLayout.Position.NOT_SPECIFIED)
                .map(position -> new ActionPositionFacetForActionLayoutAnnotation(position, holder));
    }

    private ActionPositionFacetForActionLayoutAnnotation(final ActionLayout.Position position, final FacetHolder holder) {
        super(position, holder);
    }

}
