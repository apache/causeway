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
package org.apache.isis.core.metamodel.services.grid;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.FatalException;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.component.Grid;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.layout.GridImplementationService;
import org.apache.isis.applib.services.layout.LayoutService;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.deployment.DeploymentCategoryAware;
import org.apache.isis.core.metamodel.facets.object.grid.GridFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderAware;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpiAware;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;

@DomainService(
        nature = NatureOfService.DOMAIN
)
public class LayoutServiceDefault
        implements LayoutService, SpecificationLoaderAware, SpecificationLoaderSpiAware, DeploymentCategoryAware {

    private static final Logger LOG = LoggerFactory.getLogger(LayoutServiceDefault.class);

    public static final String COMMON_TNS = "http://isis.apache.org/applib/layout/component";
    public static final String COMMON_SCHEMA_LOCATION = "http://isis.apache.org/applib/layout/component/component.xsd";

    // for better logging messages (used only in prototyping mode)
    private final Map<Class<?>, String> badXmlByClass = Maps.newHashMap();

    // cache (used only in prototyping mode)
    private final Map<String, Grid> pageByXml = Maps.newHashMap();

    private List<Class<? extends Grid>> pageImplementations;

    private final MimeType mimeTypeApplicationZip;


    public LayoutServiceDefault() {
        try {
            mimeTypeApplicationZip = new MimeType("application", "zip");
        } catch (final MimeTypeParseException ex) {
            throw new RuntimeException(ex);
        }
    }


    @PostConstruct
    public void init(){
        pageImplementations = FluentIterable.from(gridImplementationServices)
                .transform(new Function<GridImplementationService, Class<? extends Grid>>() {
                    @Override
                    public Class<? extends Grid> apply(final GridImplementationService gridImplementationService) {
                        return gridImplementationService.gridImplementation();
                    }
                })
                .toList();
    }


    @Override
    @Programmatic
    public boolean xmlExistsFor(final Class<?> domainClass) {
        final URL resource = Resources.getResource(domainClass, resourceNameFor(domainClass));
        return resource != null;
    }

    @Override
    public Grid normalizedFromXmlElseDefault(final Class<?> domainClass) {
        Grid grid = fromXml(domainClass);
        if(grid == null) {
            grid = defaultGrid(domainClass);
        }
        normalize(grid);
        return grid;
    }

    protected Grid fromXml(Class<?> domainClass) {

        final String resourceName = resourceNameFor(domainClass);
        final String xml;
        try {
            xml = resourceContentOf(domainClass, resourceName);
        } catch (IOException | IllegalArgumentException ex) {

            final String message = String .format(
                    "Failed to locate file %s (relative to %s.class); ex: %s)",
                    resourceName, domainClass.getName(), ex.getMessage());

            LOG.debug(message);
            return null;
        }

        if(!deploymentCategory.isProduction()) {
            final Grid grid = pageByXml.get(xml);
            if(grid != null) {
                return grid;
            }

            final String badXml = badXmlByClass.get(domainClass);
            if(badXml != null) {
                if(Objects.equals(xml, badXml)) {
                    // seen this before and already logged; just quit
                    return null;
                } else {
                    // this different XML might be good
                    badXmlByClass.remove(domainClass);
                }
            }
        }

        try {
            // all known implementations of Page
            final JAXBContext context = JAXBContext.newInstance(pageImplementations.toArray(new Class[0]));

            final Grid grid = (Grid) jaxbService.fromXml(context, xml);
            grid.setDomainClass(domainClass);
            if(!deploymentCategory.isProduction()) {
                pageByXml.put(xml, grid);
            }
            return grid;
        } catch(Exception ex) {

            if(!deploymentCategory.isProduction()) {
                // save fact that this was bad XML, so that we don't log again if called next time
                badXmlByClass.put(domainClass, xml);
            }

            // note that we don't blacklist if the file exists but couldn't be parsed;
            // the developer might fix so we will want to retry.
            final String message = "Failed to parse " + resourceName + " file (" + ex.getMessage() + ")";
            LOG.warn(message);

            return null;
        }
    }

    protected Grid defaultGrid(Class<?> domainClass) {

        for (GridImplementationService gridImplementationService : gridNormalizerServices()) {
            Grid grid = gridImplementationService.defaultGrid(domainClass);
            if(grid != null) {
                return grid;
            }
        }
        throw new IllegalStateException("No GridNormalizerService available to create grid for '" + domainClass.getName() + "'");
    }

    protected Grid normalize(final Grid grid) {

        // if have .layout.json and then add a .layout.xml without restarting, then note that the .layout.xml won't
        // be picked up.  To do so would require normalizing repeatedly in order to trample over the .layout.json's
        // original facets
        if(grid.isNormalized()) {
            return grid;
        }

        final Class<?> domainClass = grid.getDomainClass();

        for (GridImplementationService gridImplementationService : gridNormalizerServices()) {
            gridImplementationService.normalize(grid, domainClass);
        }

        final String tnsAndSchemaLocation = tnsAndSchemaLocation(grid);
        grid.setTnsAndSchemaLocation(tnsAndSchemaLocation);

        grid.setNormalized(true);

        return grid;
    }

    /**
     * Not public API, exposed only for testing.
     */
    @Programmatic
    public String tnsAndSchemaLocation(final Grid grid) {
        final List<String> parts = Lists.newArrayList();
        parts.add(COMMON_TNS);
        parts.add(COMMON_SCHEMA_LOCATION);
        FluentIterable.from(gridImplementationServices)
                .filter(new Predicate<GridImplementationService>() {
                    @Override
                    public boolean apply(final GridImplementationService gridImplementationService) {
                        final Class<? extends Grid> gridImpl = gridImplementationService.gridImplementation();
                        return gridImpl.isAssignableFrom(grid.getClass());
                    }
                })
                .transform(new Function<GridImplementationService, Void>() {
                    @Nullable @Override
                    public Void apply(final GridImplementationService gridImplementationService) {
                        parts.add(gridImplementationService.tns());
                        parts.add(gridImplementationService.schemaLocation());
                        return null;
                    }
                });
        return Joiner.on(" ").join(parts);
    }

    @Override
    @Programmatic
    public Grid complete(final Grid grid) {

        final Class<?> domainClass = grid.getDomainClass();
        for (GridImplementationService gridImplementationService : gridNormalizerServices()) {
            gridImplementationService.complete(grid, domainClass);
        }

        return grid;
    }

    @Override
    @Programmatic
    public Grid minimal(final Grid grid) {

        final Class<?> domainClass = grid.getDomainClass();
        for (GridImplementationService gridImplementationService : gridNormalizerServices()) {
            gridImplementationService.minimal(grid, domainClass);
        }

        return grid;
    }


    private static String resourceContentOf(final Class<?> cls, final String resourceName) throws IOException {
        final URL url = Resources.getResource(cls, resourceName);
        return Resources.toString(url, Charset.defaultCharset());
    }

    private String resourceNameFor(final Class<?> domainClass) {
        return domainClass.getSimpleName() + ".layout.xml";
    }


    @Override
    public Grid toGrid(final Class<?> domainClass, final Style style) {
        final Grid normalizedGrid = normalized(domainClass);
        switch (style) {
        case NORMALIZED:
            return normalizedGrid;
        case COMPLETE:
            return complete(normalizedGrid);
        case MINIMAL:
            return minimal(normalizedGrid);
        default:
            throw new IllegalArgumentException("unsupported style");
        }
    }

    protected Grid normalized(final Class<?> domainClass) {
        final ObjectSpecification objectSpec = specificationLookup.loadSpecification(domainClass);
        final GridFacet facet = objectSpec.getFacet(GridFacet.class);
        return facet != null? facet.getGrid(): null;
    }


    ////////////////////////////////////////////////////////

    private List<GridImplementationService<?>> filteredGridImplementationServices;

    /**
     * For all of the available {@link GridImplementationService}s available, return only the first one for any that
     * are for the same grid implementation.
     *
     * <p>
     *   This allows default implementations (eg for bootstrap3) to be overridden while also allowing for the more
     *   general idea of multiple implementations.
     * </p>
     */
    @Programmatic
    protected List<GridImplementationService<?>> gridNormalizerServices() {

        if (filteredGridImplementationServices == null) {
            List<GridImplementationService<?>> services = Lists.newArrayList();

            for (GridImplementationService gridImplementationService : this.gridImplementationServices) {
                final Class gridImplementation = gridImplementationService.gridImplementation();
                final boolean notSeenBefore = FluentIterable.from(services).filter(new Predicate<GridImplementationService<?>>() {
                    @Override public boolean apply(@Nullable final GridImplementationService<?> gridNormalizerService) {
                        return gridNormalizerService.gridImplementation() == gridImplementation;
                    }
                }).isEmpty();
                if(notSeenBefore) {
                    services.add(gridImplementationService);
                }
            }

            filteredGridImplementationServices = services;

        }
        return filteredGridImplementationServices;
    }

    ////////////////////////////////////////////////////////


    //region > injected dependencies

    private SpecificationLoader specificationLookup;

    @Override
    public void setSpecificationLoader(final SpecificationLoader specificationLookup) {
        this.specificationLookup = specificationLookup;
    }

    private DeploymentCategory deploymentCategory;

    @Override
    public void setDeploymentCategory(final DeploymentCategory deploymentCategory) {
        this.deploymentCategory = deploymentCategory;
    }

    @Inject
    JaxbService jaxbService;

    @Inject
    List<GridImplementationService> gridImplementationServices;
    //endregion




    @Programmatic
    public Blob downloadLayouts(final LayoutService.Style style) {
        final Collection<ObjectSpecification> allSpecs = specificationLoader.allSpecifications();
        final Collection<ObjectSpecification> domainObjectSpecs = Collections2
                .filter(allSpecs, new Predicate<ObjectSpecification>(){
                    @Override
                    public boolean apply(final ObjectSpecification input) {
                        return  !input.isAbstract() &&
                                (input.containsDoOpFacet(JdoPersistenceCapableFacet.class) ||
                                        input.containsDoOpFacet(ViewModelFacet.class));
                    }});
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ZipOutputStream zos = new ZipOutputStream(baos);
            final OutputStreamWriter writer = new OutputStreamWriter(zos);
            for (final ObjectSpecification objectSpec : domainObjectSpecs) {
                final Class<?> domainClass = objectSpec.getCorrespondingClass();
                final Grid grid = layoutService.toGrid(domainClass, style);
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
            final String fileName = "layouts." + style.name().toLowerCase() + ".zip";
            return new Blob(fileName, mimeTypeApplicationZip, baos.toByteArray());
        } catch (final IOException ex) {
            throw new FatalException("Unable to create zip of layouts", ex);
        }
    }

    private static String zipEntryNameFor(final ObjectSpecification objectSpec) {
        final String fqn = objectSpec.getFullIdentifier();
        return fqn.replace(".", File.separator)+".layout.xml";
    }


    // //////////////////////////////////////

    @Inject
    LayoutService layoutService;


    private SpecificationLoaderSpi specificationLoader;

    @Programmatic
    @Override
    public void setSpecificationLoaderSpi(final SpecificationLoaderSpi specificationLoader) {
        this.specificationLoader = specificationLoader;
    }


}
