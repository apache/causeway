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
package org.apache.causeway.core.metamodel.facets.object.domainobjectlayout.tabledec;

import java.util.Optional;

import org.apache.causeway.applib.layout.component.DomainObjectLayoutData;
import org.apache.causeway.applib.layout.component.TableDecoration;
import org.apache.causeway.core.config.metamodel.facets.DomainObjectLayoutConfigOptions;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

import lombok.val;

public class DomainObjectLayoutTableDecorationFacetForDomainObjectLayoutXml
extends DomainObjectLayoutTableDecorationFacetAbstract {

    public static final Class<DomainObjectLayoutTableDecorationFacet> type() {
        return DomainObjectLayoutTableDecorationFacet.class;
    }

    public static Optional<DomainObjectLayoutTableDecorationFacet> create(
            final DomainObjectLayoutData domainObjectLayout,
            final FacetHolder holder) {

        if(domainObjectLayout == null) {
            return Optional.empty();
        }

        val tableDecoration = domainObjectLayout.getTableDecoration();
        return Optional.ofNullable(
                tableDecoration == TableDecoration.DATATABLES_NET ?
                    new DomainObjectLayoutTableDecorationFacetForDomainObjectLayoutXml(holder)
                    : null);
    }

    private DomainObjectLayoutTableDecorationFacetForDomainObjectLayoutXml(final FacetHolder holder) {
        super(DomainObjectLayoutConfigOptions.TableDecoration.DATATABLES_NET, holder);
    }


}
