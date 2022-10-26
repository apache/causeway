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
package org.apache.causeway.core.metamodel.facets.actions.layout;

import java.util.Optional;

import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.members.hidden.HiddenFacetAbstract;
import org.apache.causeway.core.metamodel.object.ManagedObject;

public class HiddenFacetForActionLayoutAnnotation extends HiddenFacetAbstract {

    public static Optional<HiddenFacetForActionLayoutAnnotation> create(
            final Optional<ActionLayout> actionLayoutIfAny,
            final FacetHolder holder) {

        return actionLayoutIfAny
                .map(ActionLayout::hidden)
                .filter(where -> where != null && where != Where.NOT_SPECIFIED)
                .map(where -> new HiddenFacetForActionLayoutAnnotation(where, holder));
    }

    private HiddenFacetForActionLayoutAnnotation(final Where where, final FacetHolder holder) {
        super(where, holder);
    }

    @Override
    public String hiddenReason(final ManagedObject targetAdapter, final Where whereContext) {
        if(!where().includes(whereContext)) {
            return null;
        }
        return "Hidden on " + where().getFriendlyName();
    }

}
