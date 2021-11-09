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
package org.apache.isis.core.runtimeservices.sitemap;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.applib.layout.menubars.bootstrap3.BS3MenuBars;
import org.apache.isis.applib.services.grid.GridService;
import org.apache.isis.applib.services.layout.Style;
import org.apache.isis.applib.services.menu.MenuBarsService;
import org.apache.isis.applib.services.sitemap.SitemapService;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.facets.object.grid.GridFacet;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@Named("isis.runtimeservices.SitemapServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
//@Log4j2
public class SitemapServiceDefault implements SitemapService {

    private final SpecificationLoader specificationLoader;
    private final GridService gridService;
    private final MenuBarsService menuBarsService;

    @Override
    public String toSitemapAdoc(final String title) {

        val adoc = new StringBuilder();
        adoc.append(String.format("= %s\n\n", title));
        adoc.append(":sectnums:\n\n");

        val menuBars = menuBarsService.menuBars(MenuBarsService.Type.DEFAULT);

        menuBars.visit(BS3MenuBars.VisitorAdapter.visitingMenus(menu->{
            val menuName = _Strings.isNotEmpty(menu.getNamed())
                ? menu.getNamed()
                : "Unnamed Menu";

            adoc.append(String.format("== %s\n\n", menuName));

            _NullSafe.stream(menu.getSections())
            .forEach(menuSection->{
                val sectionName = _Strings.isNotEmpty(menuSection.getNamed())
                        ? menuSection.getNamed()
                        : "Unnamed Section";
                adoc.append(String.format("=== %s\n\n", sectionName));
            });

        }));

        return adoc.toString();
    }

    // -- HELPER

    private Grid toGrid(final Class<?> domainClass, final Style style) {

        if (style == Style.CURRENT) {

            return specificationLoader.specForType(domainClass)
                    .flatMap(spec->spec.lookupFacet(GridFacet.class))
                    .map(gridFacet->gridFacet.getGrid(null))
                    .orElse(null);
        }

        // don't use the grid from the facet, because it will be modified subsequently.
        Grid grid = gridService.load(domainClass);
        if(grid == null) {
            grid = gridService.defaultGridFor(domainClass);
        }
        gridService.normalize(grid);
        if (style == Style.NORMALIZED) {
            return grid;
        }
        if (style == Style.COMPLETE) {
            return gridService.complete(grid);
        }
        if (style == Style.MINIMAL) {
            return gridService.minimal(grid);
        }
        throw new IllegalArgumentException("unsupported style");
    }

}
