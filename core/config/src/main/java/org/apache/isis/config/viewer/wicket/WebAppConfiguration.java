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
package org.apache.isis.config.viewer.wicket;

import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import org.apache.isis.commons.internal.resources._Resources;
import org.apache.isis.config.IsisConfiguration;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import lombok.val;

/**
 * @since 2.0 
 */
@Service
@Singleton
@Named("isisConfig.WebAppConfiguration")
@Log4j2
public class WebAppConfiguration {
    
    @Inject private IsisConfiguration isisConfiguration; 

    @Getter private AbstractResource menubarsLayoutXml;
    
    @Getter private String applicationName;
    @Getter private String applicationVersion;
    @Getter private String aboutMessage;
    @Getter private String faviconContentType;

    // URLs *not* sensitive to context path (remove any leading /)
    @Getter private String applicationCss;
    @Getter private String applicationJs;
    @Getter private String welcomeMessage;

    // URLs sensitive to context path (add context-path)
    @Getter private String faviconUrl;
    @Getter private String brandLogoHeader;
    @Getter private String brandLogoSignin;
    
    @PostConstruct
    public void init() {

        val application = isisConfiguration.getViewer().getWicket().getApplication();
        
        this.menubarsLayoutXml = lookup(application.getMenubarsLayoutXml());
        
        this.applicationName = application.getName();
        this.aboutMessage = application.getAbout();
        this.applicationVersion = application.getVersion();
        
        this.applicationCss = ignoreLeadingSlash(application.getCss());
        this.applicationJs = ignoreLeadingSlash(application.getJs());

        this.brandLogoHeader = Optional.ofNullable(honorContextPath(application.getBrandLogoHeader()))
                .orElse(this.applicationName);
        this.brandLogoSignin = Optional.ofNullable(honorContextPath(application.getBrandLogoSignin()))
                .orElse(null);
        this.faviconUrl = honorContextPath(application.getFaviconUrl());
        
        this.faviconContentType = application.getFaviconContentType();
        
        val welcome = isisConfiguration.getViewer().getWicket().getWelcome();
        
        this.welcomeMessage = ignoreLeadingSlash(welcome.getText());

    }
    
    // -- HELPER

    private String honorContextPath(String url) {
        return _Resources.prependContextPathIfRequired(url);
    }

    private String ignoreLeadingSlash(String url) {
        if(url==null || url.length()<2) {
            return url;
        }
        return url.startsWith("/")
                ? url.substring(1)
                        : url;
    }
    
    private ClassPathResource lookup(String path) {
        if(path == null) {
            return lookup("menubars.layout.xml"); // try lookup default name
        }
        return new ClassPathResource(path);
    }
    

}
