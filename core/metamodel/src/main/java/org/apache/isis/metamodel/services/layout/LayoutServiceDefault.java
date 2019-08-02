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
package org.apache.isis.metamodel.services.layout;

import java.io.File;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.Marshaller;

import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.applib.layout.menubars.MenuBars;
import org.apache.isis.applib.services.grid.GridService;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.layout.LayoutService;
import org.apache.isis.applib.services.menu.MenuBarsService;
import org.apache.isis.commons.ZipWriter;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.metamodel.facets.object.grid.GridFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.specloader.SpecificationLoader;

import lombok.val;

@Singleton
public class LayoutServiceDefault implements LayoutService {

    @Override
    public String toXml(final Class<?> domainClass, final Style style) {
        final Grid grid = toGrid(domainClass, style);
        return jaxbService.toXml(grid,
                _Maps.unmodifiable(
                        Marshaller.JAXB_SCHEMA_LOCATION,
                        grid.getTnsAndSchemaLocation()
                        ));
    }

    protected Grid toGrid(final Class<?> domainClass, final Style style) {

        if (style == Style.CURRENT) {
            final ObjectSpecification objectSpec = specificationLoader.loadSpecification(domainClass);
            final GridFacet facet = objectSpec.getFacet(GridFacet.class);
            return facet != null? facet.getGrid(null): null;
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


    @Override
    public byte[] toZip(final Style style) {
        final Collection<ObjectSpecification> allSpecs = specificationLoader.currentSpecifications();
        final List<ObjectSpecification> domainObjectSpecs = _Lists
                .filter(allSpecs, spec ->
                !spec.isAbstract() &&
                (spec.isEntity() || spec.isViewModel())
                        );


        val zipWriter = ZipWriter.ofFailureMessage("Unable to create zip of layouts");

        for (final ObjectSpecification objectSpec : domainObjectSpecs) {
            val domainClass = objectSpec.getCorrespondingClass();
            val grid = toGrid(domainClass, style);
            if(grid != null) {
                zipWriter.nextEntry(zipEntryNameFor(objectSpec), writer->{

                    val xmlString = jaxbService.toXml(grid,
                            _Maps.unmodifiable(
                                    Marshaller.JAXB_SCHEMA_LOCATION,
                                    grid.getTnsAndSchemaLocation()
                                    ));
                    writer.write(xmlString);
                });
            }
        }

        return zipWriter.toBytes();
    }

    private static String zipEntryNameFor(final ObjectSpecification objectSpec) {
        final String fqn = objectSpec.getFullIdentifier();
        return fqn.replace(".", File.separator)+".layout.xml";
    }

    @Override
    public String toMenuBarsXml(final MenuBarsService.Type type) {
        final MenuBars menuBars = menuBarsService.menuBars(type);

        return jaxbService.toXml(menuBars, _Maps.unmodifiable(
                Marshaller.JAXB_SCHEMA_LOCATION,
                menuBars.getTnsAndSchemaLocation()
                ));
    }

    @Inject SpecificationLoader specificationLoader;
    @Inject JaxbService jaxbService;
    @Inject GridService gridService;
    @Inject MenuBarsService menuBarsService;

}
