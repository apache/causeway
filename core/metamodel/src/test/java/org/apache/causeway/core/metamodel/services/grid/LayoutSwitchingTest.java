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
package org.apache.causeway.core.metamodel.services.grid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.function.Predicate;

import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.layout.grid.bootstrap.BSUtil;
import org.apache.causeway.applib.services.grid.GridService;
import org.apache.causeway.applib.services.grid.GridService.LayoutKey;
import org.apache.causeway.commons.internal._Java17Ex;
import org.apache.causeway.commons.io.TextUtils;
import org.apache.causeway.core.metamodel.MetaModelTestAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FacetRanking;
import org.apache.causeway.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.causeway.core.metamodel.facets.all.hide.HiddenFacet.Semantics;
import org.apache.causeway.core.metamodel.facets.object.grid.GridFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.junit.jupiter.api.Test;

import lombok.experimental.ExtensionMethod;

/**
 * Switching between Layout Variants may result in Members staying hidden.
 * https://issues.apache.org/jira/browse/CAUSEWAY-3971
 */
@ExtensionMethod(_Java17Ex.class)
class LayoutSwitchingTest extends MetaModelTestAbstract {

    private GridServiceDefault gridService;
    
    @Override
    protected void afterSetUp() {
        this.gridService = ((GridServiceDefault) getServiceRegistry()
                .lookupServiceElseFail(GridService.class));
        assertFalse(gridService.supportsReloading(), ()->"unmet test precondition 'supportsReloading'");
    }

    @Test
    void switchLayout_viaFacet() {

        var barSpec = getSpecificationLoader().specForTypeElseFail(Bar.class);

        var gridFacet = barSpec.lookupFacet(GridFacet.class).orElse(null);
        assertNotNull(gridFacet);

        // triggers grid to be loaded initially
        gridFacet.getGrid(ManagedObject.adaptSingular(barSpec, new Bar()));

        var bsGridSimple = (BSGrid) gridFacet.getGrid(ManagedObject.adaptSingular(barSpec, new Bar("simple")));
        assertLineCount(bsGridSimple, 3, line->line.contains("hidden: EVERYWHERE")); // 3 hidden members
        assertHasLayoutKey(bsGridSimple, new LayoutKey(Bar.class, "simple"));

        var bsGridDefault = (BSGrid) gridFacet.getGrid(ManagedObject.adaptSingular(barSpec, new Bar()));
        assertLineCount(bsGridDefault, 3, line->line.contains("hidden: null")); // 3 non-hidden members
        assertHasLayoutKey(bsGridDefault, new LayoutKey(Bar.class));

        assertCanSwitchQualifierOnAndOff(barSpec, new LayoutKey(Bar.class, "simple"));

        // REPEATED loading should not change the total number of facets stored within rankings
        assertTotalFacetCountIsInvariant(List.of(
                barSpec.getActionElseFail("createSimpleObject"),
                barSpec.getPropertyElseFail("name"),
                barSpec.getCollectionElseFail("sampleCollection")
            ),
            ()->gridFacet.getGrid(ManagedObject.adaptSingular(barSpec, new Bar("simple"))));
    }

    @Test
    void switchLayout_viaService() {

        // triggers grid to be loaded initially
        gridService.load(new LayoutKey(Bar.class));

        var bsGridSimple = (BSGrid) gridService.load(new LayoutKey(Bar.class, "simple"));
        assertLineCount(bsGridSimple, 3, line->line.contains("hidden: EVERYWHERE")); // 3 hidden members
        assertHasLayoutKey(bsGridSimple, new LayoutKey(Bar.class, "simple"));

        var bsGridDefault = (BSGrid) gridService.load(new LayoutKey(Bar.class));
        assertLineCount(bsGridDefault, 3, line->line.contains("hidden: null")); // 3 non-hidden members
        assertHasLayoutKey(bsGridDefault, new LayoutKey(Bar.class));

        var barSpec = getSpecificationLoader().specForTypeElseFail(Bar.class);
        assertCanSwitchQualifierOnAndOff(barSpec, new LayoutKey(Bar.class, "simple"));
    }

    // -- HELPER

    private void assertTotalFacetCountIsInvariant(final List<FacetHolder> facetHolders, final Runnable runnable) {
        final var totalFacetCountsBefore = facetHolders.stream()
                .map(facetHolder->facetHolder.getFacetRanking(HiddenFacet.class).orElseThrow())
                .map(FacetRanking::totalFacetCount)
                .toList();
        runnable.run();
        final var totalFacetCountsAfter = facetHolders.stream()
                .map(facetHolder->facetHolder.getFacetRanking(HiddenFacet.class).orElseThrow())
                .map(FacetRanking::totalFacetCount)
                .toList();
        assertEquals(totalFacetCountsBefore, totalFacetCountsAfter);
    }

    private void assertCanSwitchQualifierOnAndOff(final ObjectSpecification spec, final LayoutKey layoutKey) {
        assertHiddenActionCount(spec, 0);
        assertHiddenPropertyCount(spec, 0);
        assertHiddenCollectionCount(spec, 0);

        FacetRanking.setQualifier(layoutKey);
        assertHiddenActionCount(spec, 1);
        assertHiddenPropertyCount(spec, 1);
        assertHiddenCollectionCount(spec, 1);

        FacetRanking.removeQualifier();
        assertHiddenActionCount(spec, 0);
        assertHiddenPropertyCount(spec, 0);
        assertHiddenCollectionCount(spec, 0);
    }

    private void assertHiddenActionCount(final ObjectSpecification spec, final long n) {
        assertEquals(n,
            spec.streamActions(ActionScope.ANY, MixedIn.EXCLUDED)
                .flatMap(act->act.lookupFacet(HiddenFacet.class).stream())
                .map(HiddenFacet::getSemantics)
                .filter(Semantics::isHidden)
                .count());
    }
    private void assertHiddenPropertyCount(final ObjectSpecification spec, final long n) {
        assertEquals(n,
            spec.streamProperties(MixedIn.EXCLUDED)
                .flatMap(prop->prop.lookupFacet(HiddenFacet.class).stream())
                .map(HiddenFacet::getSemantics)
                .filter(Semantics::isHidden)
                .count());
    }
    private void assertHiddenCollectionCount(final ObjectSpecification spec, final long n) {
        assertEquals(n,
            spec.streamCollections(MixedIn.EXCLUDED)
                .flatMap(coll->coll.lookupFacet(HiddenFacet.class).stream())
                .map(HiddenFacet::getSemantics)
                .filter(Semantics::isHidden)
                .count());
    }

    private void assertHasLayoutKey(final BSGrid bsGrid, final LayoutKey layoutKey) {
        assertEquals(layoutKey.domainClass(), bsGrid.domainClass());
        assertEquals(layoutKey, bsGrid.layoutKey());
    }

    private void assertLineCount(final BSGrid bsGrid, final long n, final Predicate<String> matcher) {
        assertEquals(n, TextUtils.readLines(BSUtil.toYaml(bsGrid))
                .stream()
                .filter(matcher)
                .count());
    }

}
