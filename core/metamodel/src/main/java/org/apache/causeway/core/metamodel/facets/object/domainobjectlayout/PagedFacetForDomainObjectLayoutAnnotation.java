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
package org.apache.causeway.core.metamodel.facets.object.domainobjectlayout;

import java.util.Optional;

import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.TableDecorator;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.domainobjectlayout.tabledec.DomainObjectLayoutTableDecoratorFacet;
import org.apache.causeway.core.metamodel.facets.object.paged.PagedFacet;
import org.apache.causeway.core.metamodel.facets.object.paged.PagedFacetAbstract;

import lombok.val;

public class PagedFacetForDomainObjectLayoutAnnotation extends PagedFacetAbstract {

    public static Optional<PagedFacet> create(
            final Optional<DomainObjectLayout> domainObjectLayoutIfAny,
            final FacetHolder holder) {


        val tableDecoratorFacet = holder.getFacet(DomainObjectLayoutTableDecoratorFacet.class);
        if (TableDecorator.DatatablesNet.class.equals(tableDecoratorFacet.value())) {
            return Optional.of(new PagedFacetOverriddenByDataTablesDecoration(holder));
        }

        return domainObjectLayoutIfAny
                .map(DomainObjectLayout::paged)
                .filter(paged -> paged > 1)
                .map(paged -> new PagedFacetForDomainObjectLayoutAnnotation(paged, holder));
    }

    private PagedFacetForDomainObjectLayoutAnnotation(final int value, final FacetHolder holder) {
        super(value, holder, Precedence.DEFAULT);
    }
}
