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
package org.apache.isis.core.metamodel.services.grid;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.apache.isis.applib.services.grid.GridLoaderService;
import org.apache.isis.applib.services.layout.LayoutService;
import org.apache.isis.applib.services.layout.Style;
import org.apache.isis.core.metamodel.MetaModelTestAbstract;
import org.apache.isis.core.metamodel.facetapi.Facet.Precedence;
import org.apache.isis.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.isis.core.metamodel.facets.object.grid.GridFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import static org.junit.Assert.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import lombok.val;

class GridLoadingTest
extends MetaModelTestAbstract {

    private GridLoaderServiceDefault gridLoaderService;
    private LayoutService layoutService;

    @Override
    protected void afterSetUp() {
        layoutService = getServiceRegistry().lookupServiceElseFail(LayoutService.class);
        gridLoaderService = (GridLoaderServiceDefault)getServiceRegistry().lookupServiceElseFail(GridLoaderService.class);
    }

    @Test @Disabled("just a blueprint")
    void blueprint() {
        val domainClassAndLayout = new GridLoaderServiceDefault.DomainClassAndLayout(Bar.class, null);
        gridLoaderService.loadXml(domainClassAndLayout);

        val xml = layoutService.toXml(Bar.class, Style.NORMALIZED);
        System.out.println(xml);
    }

    @Test
    void customNamed() {

        val customNamed = "Hello";

        val barSpec = getSpecificationLoader().specForTypeElseFail(Bar.class);

        val gridFacet = barSpec.getFacet(GridFacet.class);
        assertNotNull(gridFacet);

        // triggers grid to be loaded (if initial or reloading supported)
        val grid = gridFacet.getGrid(ManagedObject.of(barSpec, new Bar()));
        assertNotNull(grid);

        // verify object-action's member named facet was installed when loading Grid from XML
        val objectAction = barSpec.getAction("createSimpleObject").orElse(null);
        assertNotNull(objectAction);
        assertEquals(customNamed, objectAction.getStaticFriendlyName().orElse(null));

        // trigger a layout.xml reload, which installs more facets while purging old ones
        // verify however, that the number of facets stays constant

        // triggers grid to be re-loaded
        val grid2 = gridFacet.getGrid(ManagedObject.of(barSpec, new Bar()));
        assertNotSame(grid, grid2); // verify that we actually got a new grid, indicative of a reload having taken place

        assertEquals(customNamed, objectAction.getStaticFriendlyName().orElse(null));

        val facetRanking = objectAction.getFacetRanking(MemberNamedFacet.class).orElse(null);
        assertNotNull(facetRanking);

        // XML layout facets are installed at precedence HIGH
        val xmlFacetRank = facetRanking.getRankLowerOrEqualTo(MemberNamedFacet.class, Precedence.HIGH);

        // verify rank did not grow with latest layout.xml reload
        assertEquals(1, xmlFacetRank.size());

        // verify winning facet is the same object as the last one added from latest layout.xml reload,
        // to make sure we are not feed the winner from an outdated cache
        assertSame(facetRanking.getWinnerNonEvent(MemberNamedFacet.class).get(), xmlFacetRank.getLastOrFail());

    }


}