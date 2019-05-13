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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.isis.applib.layout.menubars.bootstrap3.BS3MenuBars;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.menu.MenuBarsLoaderService;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.config.beans.WebAppConfigBean;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

@Singleton @Slf4j
public class MenuBarsLoaderServiceDefault implements MenuBarsLoaderService {
    
    @Override
    public boolean supportsReloading() {
        return _Context.isPrototyping();
    }

    @Override
    public BS3MenuBars menuBars() {
        
        val menubarsLayoutResource = webAppConfigBean.getMenubarsLayoutXml();
        if(menubarsLayoutResource==null) {
            warnNotFound();
            return null;
        }
        
        try {
            
            final String xml = 
                    _Strings.read(menubarsLayoutResource.getInputStream(), StandardCharsets.UTF_8); 

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
        
        log.warn( 
                String.format("Configured '%s' failes to provide a readable resource for "
                        + "the Menubars-Layout.", 
                        WebAppConfigBean.class.getName()));
        warnedOnce = true; 
    }
    
    private void severeCannotLoad(Exception cause) {
        
        log.error(
                String.format("Configured '%s' failes to provide a readable resource for "
                        + "the Menubars-Layout.", 
                        WebAppConfigBean.class.getName()), 
                cause);
    }

    @Inject JaxbService jaxbService;
    @Inject WebAppConfigBean webAppConfigBean;

}

