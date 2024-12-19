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

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.facetapi.Facet;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
final class FacetGroupNode implements MMNode {
    
    private final Can<Facet> facets;

    @Override
    public String title() {
        return "Facets";
    }

    @Override
    public String iconName() {
        return "";
    }
    
    @Override
    public void putDetails(Details details) {
        facets.stream()
            .sorted(FacetGroupNode::facetCompare)
            .forEach(facet->details.put(facet.facetType().getSimpleName(), facet.getClass().getName()));
    }

    // -- TREE NODE STUFF

    @Getter @Setter
    private MMNode parentNode;

    @Override
    public Stream<MMNode> streamChildNodes() {
        return facets.stream()
            .sorted(FacetGroupNode::facetCompare)
            .map(facet->MMNodeFactory.facet(facet, this));
    }
    
    // -- HELPER
    
    private static int facetCompare(Facet a, Facet b) {
        int c = a.facetType().getSimpleName().compareToIgnoreCase(b.facetType().getSimpleName());
        return c==0 
            ? a.getClass().getName().compareToIgnoreCase(b.getClass().getName())
            : c;
    }

}
