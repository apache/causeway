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

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.component.Grid;
import org.apache.isis.applib.services.grid.GridLoaderService;
import org.apache.isis.applib.services.grid.GridSystemService;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.core.metamodel.deployment.DeploymentCategoryProvider;

@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
)
public class GridLoaderServiceDefault implements GridLoaderService {

    private static final Logger LOG = LoggerFactory.getLogger(GridLoaderServiceDefault.class);


    // for better logging messages (used only in prototyping mode)
    private final Map<Class<?>, String> badXmlByClass = Maps.newHashMap();

    // cache (used only in prototyping mode)
    private final Map<String, Grid> gridByXml = Maps.newHashMap();

    private List<Class<? extends Grid>> pageImplementations;



    @PostConstruct
    public void init(){
        pageImplementations = FluentIterable.from(gridSystemServices)
                .transform(new Function<GridSystemService, Class<? extends Grid>>() {
                    @Override
                    public Class<? extends Grid> apply(final GridSystemService gridSystemService) {
                        return gridSystemService.gridImplementation();
                    }
                })
                .toList();
    }

    @Override
    public boolean supportsReloading() {
        return !deploymentCategoryProvider.getDeploymentCategory().isProduction();
    }

    @Override
    public void remove(final Class<?> domainClass) {
        if(!supportsReloading()) {
            return;
        }
        badXmlByClass.remove(domainClass);
        final String xml = loadXml(domainClass);
        if(xml == null) {
            return;
        }
        gridByXml.remove(xml);
    }

    @Override
    @Programmatic
    public boolean existsFor(final Class<?> domainClass) {
        final URL resource = Resources.getResource(domainClass, resourceNameFor(domainClass));
        return resource != null;
    }

    @Override
    @Programmatic
    public Grid load(final Class<?> domainClass) {
        final String xml = loadXml(domainClass);
        if(xml == null) {
            return null;
        }

        if(supportsReloading()) {
            final Grid grid = gridByXml.get(xml);
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
            if(supportsReloading()) {
                gridByXml.put(xml, grid);
            }
            return grid;
        } catch(Exception ex) {

            if(supportsReloading()) {
                // save fact that this was bad XML, so that we don't log again if called next time
                badXmlByClass.put(domainClass, xml);
            }

            // note that we don't blacklist if the file exists but couldn't be parsed;
            // the developer might fix so we will want to retry.
            final String resourceName = resourceNameFor(domainClass);
            final String message = "Failed to parse " + resourceName + " file (" + ex.getMessage() + ")";
            if(supportsReloading()) {
                container.warnUser(message);
            }
            LOG.warn(message);

            return null;
        }
    }

    private String loadXml(final Class<?> domainClass) {
        final String resourceName = resourceNameFor(domainClass);
        try {
            return resourceContentOf(domainClass, resourceName);
        } catch (IOException | IllegalArgumentException ex) {

            if(LOG.isDebugEnabled()) {
                final String message = String .format(
                        "Failed to locate file %s (relative to %s.class); ex: %s)",
                        resourceName, domainClass.getName(), ex.getMessage());

                LOG.debug(message);
            }
            return null;
        }
    }

    private static String resourceContentOf(final Class<?> cls, final String resourceName) throws IOException {
        final URL url = Resources.getResource(cls, resourceName);
        return Resources.toString(url, Charset.defaultCharset());
    }

    private String resourceNameFor(final Class<?> domainClass) {
        return domainClass.getSimpleName() + ".layout.xml";
    }



    //region > injected dependencies

    @javax.inject.Inject
    DeploymentCategoryProvider deploymentCategoryProvider;

    @javax.inject.Inject
    DomainObjectContainer container;

    @javax.inject.Inject
    JaxbService jaxbService;

    @javax.inject.Inject
    List<GridSystemService> gridSystemServices;
    //endregion


}
