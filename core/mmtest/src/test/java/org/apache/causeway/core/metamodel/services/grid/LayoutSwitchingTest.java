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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.causeway.applib.layout.grid.bootstrap.BSUtil;
import org.apache.causeway.applib.services.grid.GridService;
import org.apache.causeway.applib.services.grid.GridService.LayoutKey;
import org.apache.causeway.commons.io.TextUtils;
import org.apache.causeway.core.metamodel.MetaModelTestAbstract;
import org.apache.causeway.core.metamodel.facets.object.grid.GridFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;

/**
 * Switching between Layout Variants may result in Members staying hidden.
 * https://issues.apache.org/jira/browse/CAUSEWAY-3971
 */
class LayoutSwitchingTest extends MetaModelTestAbstract {

    private GridServiceDefault gridService;

    @Override
    protected void afterSetUp() {
        this.gridService = ((GridServiceDefault) getServiceRegistry()
                .lookupServiceElseFail(GridService.class));
        assertFalse(gridService.supportsReloading());
    }

    @Test
    void switchLayout_highLevel() {

        var barSpec = getSpecificationLoader().specForTypeElseFail(Bar.class);

        var gridFacet = barSpec.getFacet(GridFacet.class);
        assertNotNull(gridFacet);

        // triggers grid to be loaded initially
        gridFacet.getGrid(ManagedObject.adaptSingular(barSpec, new Bar()));

        var bsGridSimple = gridFacet.getGrid(ManagedObject.adaptSingular(barSpec, new Bar("simple")));
        assertNotNull(bsGridSimple);
        assertEquals(3L, TextUtils.readLines(BSUtil.toYaml(bsGridSimple))
                .stream()
                .filter(line->line.contains("hidden: EVERYWHERE")) // 3 hidden members
                .count());

        var bsGridDefault = gridFacet.getGrid(ManagedObject.adaptSingular(barSpec, new Bar()));
        assertEquals(3L, TextUtils.readLines(BSUtil.toYaml(bsGridDefault))
                .stream()
                .filter(line->line.contains("hidden: null")) // 3 non-hidden members
                .count());
    }

    @Test
    void switchLayout_lowLevel() {

        // triggers grid to be loaded initially
        gridService.load(new LayoutKey(Bar.class, null));

        var bsGridSimple = gridService.load(new LayoutKey(Bar.class, "simple"));
        assertEquals(3L, TextUtils.readLines(BSUtil.toYaml(bsGridSimple))
                .stream()
                .filter(line->line.contains("hidden: EVERYWHERE")) // 3 hidden members
                .count());

        var bsGridDefault = gridService.load(new LayoutKey(Bar.class, null));
        assertEquals(3L, TextUtils.readLines(BSUtil.toYaml(bsGridDefault))
                .stream()
                .filter(line->line.contains("hidden: null")) // 3 non-hidden members
                .count());
    }

}
