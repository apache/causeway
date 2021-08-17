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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.Nullable;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.applib.services.grid.GridLoaderService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.reflection._Reflect;
import org.apache.isis.commons.internal.reflection._Reflect.InterfacePolicy;
import org.apache.isis.commons.internal.resources._Resources;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Named("isis.metamodel.GridLoaderServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor //JUnit Support
@Log4j2
public class GridLoaderServiceDefault implements GridLoaderService {

    private final GridReaderUsingJaxb gridReader;
    private final MessageService messageService;
    private final boolean supportsReloading;

    @Inject
    public GridLoaderServiceDefault(
           final GridReaderUsingJaxb gridReader,
           final MessageService messageService,
           final IsisSystemEnvironment isisSystemEnvironment) {
        this.gridReader = gridReader;
        this.messageService = messageService;
        this.supportsReloading = isisSystemEnvironment.isPrototyping();
    }

    @Value
    static class DomainClassAndLayout {
        private final Class<?> domainClass;
        private final String layoutIfAny;
    }

    @Value
    static class XmlAndResourceName {
        private final @NonNull String xmlContent;
        private final @NonNull String resourceName;
    }


    // for better logging messages (used only in prototyping mode)
    private final Map<DomainClassAndLayout, String> badXmlByDomainClassAndLayout = _Maps.newHashMap();

    @Value
    static class DomainClassAndLayoutAndXml {
        private final DomainClassAndLayout domainClassAndLayout;
        private final XmlAndResourceName xml;
    }

    // cache (used only in prototyping mode)
    private final Map<DomainClassAndLayoutAndXml, Grid> gridByDomainClassAndLayoutAndXml = _Maps.newHashMap();

    @Override
    public boolean supportsReloading() {
        return supportsReloading;//isisSystemEnvironment.isPrototyping();
    }

    @Override
    public void remove(final Class<?> domainClass) {
        if(!supportsReloading()) {
            return;
        }
        final String layoutIfAny = null;
        final DomainClassAndLayout dcal = new DomainClassAndLayout(domainClass, layoutIfAny);
        badXmlByDomainClassAndLayout.remove(dcal);
        final XmlAndResourceName xml = loadXml(dcal).orElse(null);
        if(xml == null) {
            return;
        }
        gridByDomainClassAndLayoutAndXml.remove(new DomainClassAndLayoutAndXml(dcal, xml));
    }

    @Override
    public boolean existsFor(final Class<?> domainClass) {
        return loadXml(new DomainClassAndLayout(domainClass, null)).isPresent();
    }

    @Override
    public Grid load(final Class<?> domainClass, final String layoutIfAny) {
        final DomainClassAndLayout dcal = new DomainClassAndLayout(domainClass, layoutIfAny);
        final XmlAndResourceName xml = loadXml(dcal).orElse(null);
        if(xml == null) {
            return null;
        }

        final DomainClassAndLayoutAndXml dcalax = new DomainClassAndLayoutAndXml(dcal, xml);
        if(supportsReloading()) {
            final String badXml = badXmlByDomainClassAndLayout.get(dcal);
            if(badXml != null) {
                if(Objects.equals(xml.getXmlContent(), badXml)) {
                    // seen this before and already logged; just quit
                    return null;
                } else {
                    // this different XML might be good
                    badXmlByDomainClassAndLayout.remove(dcal);
                }
            }
        } else {
            // if cached, serve from cache - otherwise fall through
            final Grid grid = gridByDomainClassAndLayoutAndXml.get(dcalax);
            if(grid != null) {
                return grid;
            }
        }

        try {
            final Grid grid = gridReader.loadGrid(xml.getXmlContent());
            grid.setDomainClass(domainClass);
            if(supportsReloading()) {
                gridByDomainClassAndLayoutAndXml.put(dcalax, grid);
            }
            return grid;
        } catch(Exception ex) {

            if(supportsReloading()) {
                // save fact that this was bad XML, so that we don't log again if called next time
                badXmlByDomainClassAndLayout.put(dcal, xml.getXmlContent());
            }

            // note that we don't blacklist if the file exists but couldn't be parsed;
            // the developer might fix so we will want to retry.
            final String resourceName = xml.getResourceName();
            final String message = "Failed to parse " + resourceName + " file (" + ex.getMessage() + ")";
            if(supportsReloading()) {
                messageService.warnUser(message);
            }
            log.warn(message);

            return null;
        }
    }

    // -- HELPER

    Optional<XmlAndResourceName> loadXml(final DomainClassAndLayout dcal) {
        return _Reflect.streamTypeHierarchy(dcal.getDomainClass(), InterfacePolicy.EXCLUDE)
        .map(type->loadXml(type, dcal.getLayoutIfAny()))
        .filter(Optional::isPresent)
        .findFirst()
        .map(Optional::get);
    }

    private Optional<XmlAndResourceName> loadXml(
            final @NonNull Class<?> domainClass,
            final @Nullable String layoutIfAny) {
        return streamResourceNameCandidatesFor(domainClass, layoutIfAny)
        .map(candidateResourceName->tryLoadXml(domainClass, candidateResourceName))
        .filter(Optional::isPresent)
        .findFirst()
        .map(Optional::get);
    }

    private Stream<String> streamResourceNameCandidatesFor(
            final @NonNull Class<?> domainClass,
            final @Nullable String layoutIfAny) {

        val typeSimpleName = domainClass.getSimpleName();

        return _Strings.isNotEmpty(layoutIfAny)
                ? Stream.of(
                        String.format("%s-%s.layout.xml", typeSimpleName, layoutIfAny),
                        String.format("%s.layout.xml", typeSimpleName),
                        String.format("%s.layout.fallback.xml", typeSimpleName))
                : Stream.of(
                        String.format("%s.layout.xml", typeSimpleName),
                        String.format("%s.layout.fallback.xml", typeSimpleName));
    }

    private Optional<XmlAndResourceName> tryLoadXml(
            final @NonNull Class<?> type,
            final @NonNull String candidateResourceName) {
        try {
            return Optional.ofNullable(
                    _Resources.loadAsStringUtf8(type, candidateResourceName))
                    .map(xml->new XmlAndResourceName(xml, candidateResourceName));
        } catch (IOException ex) {
            log.error(
                    "Failed to load layout file {} (relative to {}.class)",
                    candidateResourceName, type.getName(), ex);
        }
        return Optional.empty();
    }

}
