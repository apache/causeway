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

package org.apache.isis.core.metamodel.postprocessors.members.navigation;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.members.navigation.NavigationFacet;
import org.apache.isis.core.metamodel.facets.object.hidden.HiddenTypeFacet;
import org.apache.isis.core.metamodel.interactions.ObjectVisibilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.val;

public class NavigationFacetDerivedFromHiddenType
extends FacetAbstract
implements
    NavigationFacet {

    private final ObjectSpecification navigatedType;

    private static final Class<? extends Facet> type() {
        return NavigationFacet.class;
    }

    public NavigationFacetDerivedFromHiddenType(final FacetHolder holder, final ObjectSpecification navigatedType) {
        super(type(), holder);
        this.navigatedType = navigatedType;
    }

    @Override
    public String hides(final VisibilityContext ic) {
        val facet = navigatedType.getFacet(HiddenTypeFacet.class);
        if(facet == null) {
            // not expected to happen; this facet should only be installed for object members
            // that navigate to a class that has the HiddenTypeFacet
            return null;
        }
        val objVisibilityContext = new ObjectVisibilityContext(
                ic.getHead(),
                Identifier.classIdentifier(navigatedType.getLogicalType()),
                ic.getInitiatedBy(),
                ic.getWhere());
        final String hides = facet.hides(objVisibilityContext);
        return hides;
    }

}
