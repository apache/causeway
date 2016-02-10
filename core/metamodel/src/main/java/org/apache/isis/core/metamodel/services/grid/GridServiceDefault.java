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

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.component.Grid;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.layout.GridNormalizerService;
import org.apache.isis.applib.services.layout.GridService;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.deployment.DeploymentCategoryAware;
import org.apache.isis.core.metamodel.facets.object.grid.GridFacet;
import org.apache.isis.core.metamodel.layoutmetadata.json.LayoutMetadataReaderFromJson;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderAware;

@DomainService(
        nature = NatureOfService.DOMAIN
)
public class GridServiceDefault
        implements GridService, SpecificationLoaderAware, DeploymentCategoryAware {

    private static final Logger LOG = LoggerFactory.getLogger(GridServiceDefault.class);

    public static final String COMMON_TNS = "http://isis.apache.org/applib/layout/component";
    public static final String COMMON_SCHEMA_LOCATION = "http://isis.apache.org/applib/layout/component/component.xsd";

    // for better logging messages (used only in prototyping mode)
    private final Map<Class<?>, String> badXmlByClass = Maps.newHashMap();

    // cache (used only in prototyping mode)
    private final Map<String, Grid> pageByXml = Maps.newHashMap();

    private LayoutMetadataReaderFromJson layoutMetadataReaderFromJson = new LayoutMetadataReaderFromJson();

    @Override
    @Programmatic
    public boolean exists(final Class<?> domainClass) {
        final URL resource = Resources.getResource(domainClass, resourceNameFor(domainClass));
        return resource != null;
    }

    @Override
    @Programmatic
    public Grid fromXml(Class<?> domainClass) {

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
            List<Class<? extends Grid>> pageImplementations =
                    FluentIterable.from(gridNormalizerServices)
                    .transform(new Function<GridNormalizerService, Class<? extends Grid>>() {
                        @Override
                        public Class<? extends Grid> apply(final GridNormalizerService gridNormalizer) {
                            return gridNormalizer.gridImplementation();
                        }
                    })
                    .toList();
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

    @Override
    @Programmatic
    public Grid normalize(final Grid grid) {

        if(grid == null) {
            return null;
        }

        // if have .layout.json and then add a .layout.xml without restarting, then note that
        // the changes won't be picked up.  Normalizing would be required
        // in order to trample over the .layout.json's original facets
        if(grid.isNormalized()) {
            return grid;
        }

        final Class<?> domainClass = grid.getDomainClass();

        for (GridNormalizerService gridNormalizerService : gridNormalizerServices()) {
            gridNormalizerService.normalize(grid, domainClass);
        }

        grid.setNormalized(true);

        return grid;
    }

    @Override
    @Programmatic
    public Grid complete(final Grid grid) {

        if(grid == null) {
            return null;
        }

        final Class<?> domainClass = grid.getDomainClass();
        for (GridNormalizerService gridNormalizerService : gridNormalizerServices()) {
            gridNormalizerService.complete(grid, domainClass);
        }

        return grid;
    }

    @Override
    @Programmatic
    public Grid minimal(final Grid grid) {

        if(grid == null) {
            return null;
        }

        final Class<?> domainClass = grid.getDomainClass();
        for (GridNormalizerService gridNormalizerService : gridNormalizerServices()) {
            gridNormalizerService.minimal(grid, domainClass);
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
    @Programmatic
    public Grid toGrid(final Object domainObject, final Style style) {
        return toGrid(domainObject.getClass(), style);
    }

    @Override
    public Grid toGrid(final Class<?> domainClass, final Style style) {
        switch (style) {
        case NORMALIZED:
            // obtain the already normalized grid, if available.
            // (if there is none, then the facet will delegate back to this service to do the normalization,
            // but then will cache it for any subsequent requests).
            final ObjectSpecification objectSpec = specificationLookup.loadSpecification(domainClass);
            final GridFacet facet = objectSpec.getFacet(GridFacet.class);
            return facet != null? facet.getGrid(): null;
        case COMPLETE:
            return complete(fromXml(domainClass));
        case MINIMAL:
            return minimal(fromXml(domainClass));
        default:
            throw new IllegalArgumentException("unsupported style");
        }
    }

    @Override
    public String tnsAndSchemaLocation(final Grid grid) {
        final List<String> parts = Lists.newArrayList();
        parts.add(COMMON_TNS);
        parts.add(COMMON_SCHEMA_LOCATION);
        FluentIterable.from(gridNormalizerServices)
                .transform(new Function<GridNormalizerService, Void>() {
                    @Nullable @Override
                    public Void apply(final GridNormalizerService gridNormalizerService) {
                        parts.add(gridNormalizerService.tns());
                        parts.add(gridNormalizerService.schemaLocation());
                        return null;
                    }
                });
        return Joiner.on(" ").join(parts);
    }

    ////////////////////////////////////////////////////////

    private List<GridNormalizerService<?>> filteredGridNormalizerServices;

    /**
     * For all of the available {@link GridNormalizerService}s available, return only the first one for any that
     * are for the same grid implementation.
     */
    @Programmatic
    public List<GridNormalizerService<?>> gridNormalizerServices() {

        if (filteredGridNormalizerServices == null) {
            List<GridNormalizerService<?>> services = Lists.newArrayList();

            for (GridNormalizerService gridNormalizerService : this.gridNormalizerServices) {
                final Class gridImplementation = gridNormalizerService.gridImplementation();
                final boolean notSeenBefore = FluentIterable.from(services).filter(new Predicate<GridNormalizerService<?>>() {
                    @Override public boolean apply(@Nullable final GridNormalizerService<?> gridNormalizerService) {
                        return gridNormalizerService.gridImplementation() == gridImplementation;
                    }
                }).isEmpty();
                if(notSeenBefore) {
                    services.add(gridNormalizerService);
                }
            }

            filteredGridNormalizerServices = services;

        }
        return filteredGridNormalizerServices;
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
    List<GridNormalizerService> gridNormalizerServices;
    //endregion

}
