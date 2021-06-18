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
package org.apache.isis.core.runtimeservices.menubars;

import java.nio.charset.StandardCharsets;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.layout.menubars.bootstrap3.BS3MenuBars;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.menu.MenuBarsLoaderService;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.core.config.viewer.wicket.WebAppContextPath;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Named("isis.runtimeservices.MenuBarsLoaderServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
@Log4j2
public class MenuBarsLoaderServiceDefault implements MenuBarsLoaderService {

    private final IsisSystemEnvironment isisSystemEnvironment;
    private final JaxbService jaxbService;

    private final ClassPathResource menubarsLayoutXmlResource;

    @Inject
    public MenuBarsLoaderServiceDefault(
            final IsisSystemEnvironment isisSystemEnvironment,
            final JaxbService jaxbService,
            final IsisConfiguration isisConfiguration) {
        this.isisSystemEnvironment = isisSystemEnvironment;
        this.jaxbService = jaxbService;

        this.menubarsLayoutXmlResource =
                new ClassPathResource(isisConfiguration.getViewer().getWicket().getApplication().getMenubarsLayoutXml());
    }

    @Override
    public boolean supportsReloading() {
        return isisSystemEnvironment.isPrototyping();
    }

    @Override
    public BS3MenuBars menuBars() {

        val xmlString = loadMenubarsLayoutResource(menubarsLayoutXmlResource);
        if(xmlString == null) {
            warnNotFound(menubarsLayoutXmlResource);
            return null;
        }

        try {
            return jaxbService.fromXml(BS3MenuBars.class, xmlString);
        } catch (Exception e) {
            severeCannotLoad(menubarsLayoutXmlResource, e);
            return null;
        }
    }

    private String loadMenubarsLayoutResource(final AbstractResource menubarsLayoutXmlResource) {
        try {

            if(!menubarsLayoutXmlResource.exists()) {
                return null;
            }

            val source = menubarsLayoutXmlResource.getInputStream(); // throws if not found
            final String xml =
                    _Strings.read(source, StandardCharsets.UTF_8);
            return xml;
        } catch (Exception e) {
            severeCannotLoad(menubarsLayoutXmlResource, e);
            return null;
        }

    }

    // -- HELPER

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

