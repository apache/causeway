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
package org.apache.isis.core.runtime.services.menubars;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.layout.menubars.bootstrap3.BS3MenuBars;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.menu.MenuBarsLoaderService;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;

import static org.apache.isis.commons.internal.resources._Resources.loadAsString;

@DomainService(nature = NatureOfService.DOMAIN)
public class MenuBarsLoaderServiceDefault implements MenuBarsLoaderService {
    
    private final static Logger log = LoggerFactory.getLogger(MenuBarsLoaderServiceDefault.class);
    private final static String menubarsLayoutResourceName = "menubars.layout.xml";

    @Override
    public boolean supportsReloading() {
        return _Context.isPrototyping();
    }

    @Override
    public BS3MenuBars menuBars() {
        final AppManifest appManifest = isisSessionFactory.getAppManifest();
        try {
            
            final String xml = 
                    loadAsString(appManifest.getClass(), menubarsLayoutResourceName, StandardCharsets.UTF_8); 

            // if the menubarsLayout resource is not found, print a warning only once and only when PROTOTYPING
            if(_Strings.isEmpty(xml)) {
                if(_Context.isPrototyping()) {
                    warnNotFound();
                }
                return null;
            }
            
            return jaxbService.fromXml(BS3MenuBars.class, xml);
        } catch (Exception e) {
            severeCannotLoad(e);
            return null;
        }
    }
    
    // -- HELPER

    private boolean warnedOnce = false;
    
    private void warnNotFound() {
        if(warnedOnce) {
            return;
        }
        final AppManifest appManifest = isisSessionFactory.getAppManifest();
        log.warn( 
                String.format("Failed to locate resource '%s' at class-path relative to '%s'", 
                menubarsLayoutResourceName, appManifest.getClass().getName()));
        
        warnedOnce = true; 
    }
    
    private void severeCannotLoad(Exception cause) {
        final AppManifest appManifest = isisSessionFactory.getAppManifest();
        log.error(
                String.format("Failed to load resource '%s' from class-path relative to '%s'", 
                menubarsLayoutResourceName, appManifest.getClass().getName()), 
                cause);
    }

    @javax.inject.Inject
    JaxbService jaxbService;

    @javax.inject.Inject
    IsisSessionFactory isisSessionFactory;

}

