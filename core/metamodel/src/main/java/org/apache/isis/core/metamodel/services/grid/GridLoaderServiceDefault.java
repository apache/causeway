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
import javax.xml.bind.JAXBException;

import com.google.common.io.Resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.applib.services.grid.GridLoaderService;
import org.apache.isis.applib.services.grid.GridSystemService;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.deployment.DeploymentCategoryProvider;

@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
        )
public class GridLoaderServiceDefault implements GridLoaderService {

    private static final Logger LOG = LoggerFactory.getLogger(GridLoaderServiceDefault.class);


    // for better logging messages (used only in prototyping mode)
    private final Map<Class<?>, String> badXmlByClass = _Maps.newHashMap();

    // cache (used only in prototyping mode)
    private final Map<String, Grid> gridByXml = _Maps.newHashMap();

    private JAXBContext jaxbContext;

    @PostConstruct
    public void init(){
        final Class<?>[] pageImplementations =
                _NullSafe.stream(gridSystemServices)
                .map(GridSystemService::gridImplementation)
                .collect(_Arrays.toArray(Class.class));
        try {
            jaxbContext = JAXBContext.newInstance(pageImplementations);
        } catch (JAXBException e) {
            // leave as null
        }
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
        return resourceNameFor(domainClass) != null;
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
            if(jaxbContext == null) {
                // shouldn't occur, indicates that initialization failed to locate any GridSystemService implementations.
                return null;
            }

            final Grid grid = (Grid) jaxbService.fromXml(jaxbContext, xml);
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
                messageService.warnUser(message);
            }
            LOG.warn(message);

            return null;
        }
    }

    private String loadXml(final Class<?> domainClass) {
        final String resourceName = resourceNameFor(domainClass);
        if(resourceName == null) {
            LOG.debug("Failed to locate layout file for '{}'", domainClass.getName());
            return null;
        }
        try {
            return resourceContentOf(domainClass, resourceName);
        } catch (IOException ex) {
            LOG.debug(
                    "Failed to locate file {} (relative to {}.class)",
                    resourceName, domainClass.getName(), ex);
            return null;
        }
    }

    private static String resourceContentOf(final Class<?> cls, final String resourceName) throws IOException {
        final URL url = Resources.getResource(cls, resourceName);
        return Resources.toString(url, Charset.defaultCharset());
    }

    String resourceNameFor(final Class<?> domainClass) {
        for (final Type type : Type.values()) {
            final String candidateResourceName = resourceNameFor(domainClass, type);
            try {
                final URL resource = Resources.getResource(domainClass, candidateResourceName);
                if (resource != null) {
                    return candidateResourceName;
                }
            } catch(IllegalArgumentException ex) {
                // continue
            }
        }
        return null;
    }

    enum Type {
        DEFAULT {
            @Override
            protected String suffix() {
                return ".layout.xml";
            }
        },
        FALLBACK {
            @Override
            protected String suffix() {
                return ".layout.fallback.xml";
            }
        };

        private String resourceNameFor(final Class<?> domainClass) {
            return domainClass.getSimpleName() + suffix();
        }

        protected abstract String suffix();
    }

    private String resourceNameFor(
            final Class<?> domainClass,
            final Type type) {
        return type.resourceNameFor(domainClass);
    }


    // -- injected dependencies

    @javax.inject.Inject
    DeploymentCategoryProvider deploymentCategoryProvider;

    @javax.inject.Inject
    MessageService messageService;

    @javax.inject.Inject
    JaxbService jaxbService;

    @javax.inject.Inject
    List<GridSystemService<?>> gridSystemServices;

}
