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
package org.apache.causeway.core.runtimeservices.menubars;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.layout.menubars.bootstrap.BSMenuBars;
import org.apache.causeway.applib.services.jaxb.JaxbService;
import org.apache.causeway.applib.services.menu.MenuBarsLoaderService;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;
import org.apache.causeway.core.config.viewer.web.WebAppContextPath;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".MenuBarsLoaderServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@Log4j2
public class MenuBarsLoaderServiceDefault
implements MenuBarsLoaderService {

    private final JaxbService jaxbService;
    private final boolean supportsReloading;
    private final AtomicReference<AbstractResource> menubarsLayoutXmlResourceRef;

    @Inject
    public MenuBarsLoaderServiceDefault(
            final CausewaySystemEnvironment causewaySystemEnvironment,
            final JaxbService jaxbService,
            final CausewayConfiguration causewayConfiguration) {
        this.jaxbService = jaxbService;
        this.supportsReloading = causewaySystemEnvironment.isPrototyping();

        val menubarsLayoutXmlResource =
                new ClassPathResource(causewayConfiguration.getViewer().getWicket().getApplication().getMenubarsLayoutXml());
        this.menubarsLayoutXmlResourceRef = new AtomicReference<>(menubarsLayoutXmlResource);
    }

    // JUnit support
    public MenuBarsLoaderServiceDefault(
            final JaxbService jaxbService,
            final AtomicReference<AbstractResource> menubarsLayoutXmlResourceRef) {
        this.jaxbService = jaxbService;
        this.supportsReloading = true;

        menubarsLayoutXmlResourceRef.getAndUpdate(r->r!=null
                ? r
                : new AbstractResource() {
                    @Override public String getDescription() { return "Empty Resource"; }
                    @Override public InputStream getInputStream() throws IOException { return null; }}
                );
        this.menubarsLayoutXmlResourceRef = menubarsLayoutXmlResourceRef;
    }

    @Override
    public boolean supportsReloading() {
        return supportsReloading;
    }

    @Override
    public BSMenuBars menuBars() {
        return loadMenuBars(loadMenubarsLayoutResource());
    }

    // public, in support of JUnit testing
    public BSMenuBars loadMenuBars(String xmlString) {
        try {
            return jaxbService.fromXml(BSMenuBars.class, xmlString);
        } catch (Exception e) {
            severeCannotLoad(menubarsLayoutXmlResourceRef.get(), e);
            return null;
        }
    }

    // -- HELPER

    private String loadMenubarsLayoutResource() {

        val menubarsLayoutXmlResource = menubarsLayoutXmlResourceRef.get();
        try {

            if(!menubarsLayoutXmlResource.exists()) {
                return null;
            }

            val source = menubarsLayoutXmlResource.getInputStream(); // throws if not found
            final String xml =
                    _Strings.read(source, StandardCharsets.UTF_8);

            if(xml == null) {
                warnNotFound(menubarsLayoutXmlResource);
            }
            return xml;
        } catch (Exception e) {
            severeCannotLoad(menubarsLayoutXmlResource, e);
            return null;
        }

    }

    private boolean warnedOnce = false;

    private void warnNotFound(AbstractResource menubarsLayoutXmlResource) {
        if(warnedOnce) {
            return;
        }

        log.warn(
                "{}: could not find readable resource {} for the Menubars-Layout.",
                        WebAppContextPath.class.getName(),
                        menubarsLayoutXmlResource);
        warnedOnce = true;
    }

    private void severeCannotLoad(AbstractResource menubarsLayoutXmlResource, Exception cause) {

        log.error("{}: could not find readable resource {} for the Menubars-Layout.",
                        WebAppContextPath.class.getName(),
                        menubarsLayoutXmlResource,
                cause);
    }
}

