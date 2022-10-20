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

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.causeway.core.metamodel.facets.members.hidden.HiddenFacetAbstract;
import org.apache.causeway.core.metamodel.object.ManagedObject;

public class HiddenFacetForActionLayoutXml
extends HiddenFacetAbstract {

    public static Optional<HiddenFacet> create(
            final ActionLayoutData actionLayout,
            final FacetHolder holder) {
        if (actionLayout == null) {
            return Optional.empty();
        }
        final Where where = actionLayout.getHidden();
        return where != null
                && where != Where.NOT_SPECIFIED
                        ? Optional.of(new HiddenFacetForActionLayoutXml(where, holder))
                        : Optional.empty();
    }

    private HiddenFacetForActionLayoutXml(final Where where, final FacetHolder holder) {
        super(where, holder);
    }

    @Override
    public boolean isObjectTypeSpecific() {
        return true;
    }

    @Override
    public String hiddenReason(final ManagedObject targetAdapter, final Where whereContext) {
        if(!where().includes(whereContext)) {
            return null;
        }
        return "Hidden on " + where().getFriendlyName();
    }

}
