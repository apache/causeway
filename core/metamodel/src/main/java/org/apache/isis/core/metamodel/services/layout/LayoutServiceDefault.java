/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.services.layout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.bind.Marshaller;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.FatalException;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.applib.layout.menubars.MenuBars;
import org.apache.isis.applib.services.grid.GridService;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.layout.LayoutService2;
import org.apache.isis.applib.services.menu.MenuBarsService;
import org.apache.isis.core.metamodel.facets.object.grid.GridFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;

@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
)
public class LayoutServiceDefault implements LayoutService2 {

    private static final Logger LOG = LoggerFactory.getLogger(LayoutServiceDefault.class);

    @Override
    public String toXml(final Class<?> domainClass, final Style style) {
        final Grid grid = toGrid(domainClass, style);
        return jaxbService.toXml(grid,
                ImmutableMap.<String,Object>of(
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


    @Programmatic
    public byte[] toZip(final Style style) {
        final Collection<ObjectSpecification> allSpecs = specificationLoader.allSpecifications();
        final Collection<ObjectSpecification> domainObjectSpecs = Collections2
                .filter(allSpecs, new Predicate<ObjectSpecification>(){
                    @Override
                    public boolean apply(final ObjectSpecification input) {
                        return  !input.isAbstract() &&
                                (input.containsDoOpFacet(JdoPersistenceCapableFacet.class) ||
                                        input.containsDoOpFacet(ViewModelFacet.class));
                    }});
        final byte[] bytes;
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ZipOutputStream zos = new ZipOutputStream(baos);
            final OutputStreamWriter writer = new OutputStreamWriter(zos);
            for (final ObjectSpecification objectSpec : domainObjectSpecs) {
                final Class<?> domainClass = objectSpec.getCorrespondingClass();
                final Grid grid = toGrid(domainClass, style);
                if(grid != null) {
                    zos.putNextEntry(new ZipEntry(zipEntryNameFor(objectSpec)));
                    final String xml = jaxbService.toXml(grid,
                            ImmutableMap.<String,Object>of(
                                    Marshaller.JAXB_SCHEMA_LOCATION,
                                    grid.getTnsAndSchemaLocation()
                            ));
                    writer.write(xml);
                    writer.flush();
                    zos.closeEntry();
                }
            }
            writer.close();
            bytes = baos.toByteArray();
        } catch (final IOException ex) {
            throw new FatalException("Unable to create zip of layouts", ex);
        }
        return bytes;
    }

    private static String zipEntryNameFor(final ObjectSpecification objectSpec) {
        final String fqn = objectSpec.getFullIdentifier();
        return fqn.replace(".", File.separator)+".layout.xml";
    }


    @Programmatic
    @Override
    public String toMenuBarsXml(final MenuBarsService.Type type) {
        final MenuBars menuBars = menuBarsService.menuBars(type);

        return jaxbService.toXml(menuBars,
                ImmutableMap.<String,Object>of(
                        Marshaller.JAXB_SCHEMA_LOCATION,
                        menuBars.getTnsAndSchemaLocation()
                ));
    }


    @javax.inject.Inject
    SpecificationLoader specificationLoader;

    @javax.inject.Inject
    JaxbService jaxbService;

    @javax.inject.Inject
    GridService gridService;

    @javax.inject.Inject
    MenuBarsService menuBarsService;

}
