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

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.applib.services.grid.GridLoaderService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.commons.internal.resources._Resources;

import lombok.Value;
import lombok.extern.log4j.Log4j2;

@Service
@Named("isisMetaModel.GridLoaderServiceDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
@Log4j2
public class GridLoaderServiceDefault implements GridLoaderService {

    @Inject private MessageService messageService;
    @Inject private GridReaderUsingJaxb gridReader;
    @Inject private IsisSystemEnvironment isisSystemEnvironment;

    @Value
    static class DomainClassAndLayout {
        private final Class<?> domainClass;
        private final String layoutIfAny;
    }

    // for better logging messages (used only in prototyping mode)
    private final Map<DomainClassAndLayout, String> badXmlByDomainClassAndLayout = _Maps.newHashMap();

    @Value
    static class DomainClassAndLayoutAndXml {
        private final DomainClassAndLayout domainClassAndLayout;
        private final String xml;
    }

    // cache (used only in prototyping mode)
    private final Map<DomainClassAndLayoutAndXml, Grid> gridByDomainClassAndLayoutAndXml = _Maps.newHashMap();

    @Override
    public boolean supportsReloading() {
        return isisSystemEnvironment.isPrototyping();
    }

    @Override
    public void remove(final Class<?> domainClass) {
        final String layoutIfAny = null;
        final DomainClassAndLayout dcal = new DomainClassAndLayout(domainClass, layoutIfAny);
        if(!supportsReloading()) {
            return;
        }
        badXmlByDomainClassAndLayout.remove(dcal);
        final String xml = loadXml(dcal);
        if(xml == null) {
            return;
        }
        gridByDomainClassAndLayoutAndXml.remove(new DomainClassAndLayoutAndXml(dcal, xml));
    }

    @Override
    public boolean existsFor(final Class<?> domainClass) {
        return resourceNameFor(new DomainClassAndLayout(domainClass, null)) != null;
    }

    @Override
    public Grid load(final Class<?> domainClass, final String layoutIfAny) {
        final DomainClassAndLayout dcal = new DomainClassAndLayout(domainClass, layoutIfAny);
        final String xml = loadXml(dcal);
        if(xml == null) {
            return null;
        }

        final DomainClassAndLayoutAndXml dcalax = new DomainClassAndLayoutAndXml(dcal, xml);
        if(supportsReloading()) {
            final Grid grid = gridByDomainClassAndLayoutAndXml.get(dcalax);
            if(grid != null) {
                return grid;
            }

            final String badXml = badXmlByDomainClassAndLayout.get(dcal);
            if(badXml != null) {
                if(Objects.equals(xml, badXml)) {
                    // seen this before and already logged; just quit
                    return null;
                } else {
                    // this different XML might be good
                    badXmlByDomainClassAndLayout.remove(dcal);
                }
            }
        }

        try {
            final Grid grid = gridReader.loadGrid(xml);
            grid.setDomainClass(domainClass);
            if(supportsReloading()) {
                gridByDomainClassAndLayoutAndXml.put(dcalax, grid);
            }
            return grid;
        } catch(Exception ex) {

            if(supportsReloading()) {
                // save fact that this was bad XML, so that we don't log again if called next time
                badXmlByDomainClassAndLayout.put(dcal, xml);
            }

            // note that we don't blacklist if the file exists but couldn't be parsed;
            // the developer might fix so we will want to retry.
            final String resourceName = resourceNameFor(dcal);
            final String message = "Failed to parse " + resourceName + " file (" + ex.getMessage() + ")";
            if(supportsReloading()) {
                messageService.warnUser(message);
            }
            log.warn(message);

            return null;
        }
    }



    private String loadXml(final DomainClassAndLayout dcal) {
        final String resourceName = resourceNameFor(dcal);
        if(resourceName == null) {
            log.debug("Failed to locate layout file for '{}'", dcal.toString());
            return null;
        }
        try {
            return _Resources.loadAsStringUtf8(dcal.domainClass, resourceName);
        } catch (IOException ex) {
            log.debug(
                    "Failed to locate file {} (relative to {}.class)",
                    resourceName, dcal.domainClass.getName(), ex);
            return null;
        }
    }

    String resourceNameFor(final DomainClassAndLayout dcal) {
        final List<String> candidateResourceNames = _Lists.newArrayList();
        if(dcal.layoutIfAny != null) {
            candidateResourceNames.add(
                    String.format("%s-%s.layout.xml", dcal.domainClass.getSimpleName(), dcal.layoutIfAny));
        }
        candidateResourceNames.add(
                String.format("%s.layout.xml", dcal.domainClass.getSimpleName()));
        candidateResourceNames.add(
                String.format("%s.layout.fallback.xml", dcal.domainClass.getSimpleName()));
        for (final String candidateResourceName : candidateResourceNames) {
            try {
                final URL resource = _Resources.getResourceUrl(dcal.domainClass, candidateResourceName);
                if (resource != null) {
                    return candidateResourceName;
                }
            } catch(IllegalArgumentException ex) {
                // continue
            }
        }
        return null;
    }


}
