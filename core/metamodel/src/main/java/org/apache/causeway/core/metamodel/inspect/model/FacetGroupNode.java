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
package org.apache.causeway.core.metamodel.inspect.model;

import java.util.stream.Stream;

import jakarta.inject.Named;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.facetapi.Facet;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Named(CausewayModuleApplib.NAMESPACE + ".FacetGroupNode")
@DomainObject(
        nature=Nature.VIEW_MODEL,
        introspection = Introspection.ANNOTATION_REQUIRED
)
@RequiredArgsConstructor
public final class FacetGroupNode implements MMNode {

    @Programmatic
    private final Can<Facet> facets;

    @Override
    public String title() {
        return "Facets";
    }

    @Override
    public String iconName() {
        return "";
    }

    // -- TREE NODE STUFF

    @Getter @Setter
    private MMNode parentNode;

    @Override
    public Stream<MMNode> streamChildNodes() {

        return facets.stream()
                .map(facet->MMNodeFactory.facet(facet, this))
                .sorted((a, b)->a.title().compareToIgnoreCase(b.title()));
    }

}
