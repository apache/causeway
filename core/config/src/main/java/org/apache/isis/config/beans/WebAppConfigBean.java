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
package org.apache.isis.config.beans;

import java.io.Serializable;

import org.springframework.core.io.AbstractResource;

import org.apache.isis.commons.internal.resources._Resources;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
public class WebAppConfigBean implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public static final String MenubarsLayoutResourceName = "menubars.layout.xml";
    
    @Getter @Setter private String applicationName;
    @Getter @Setter private String applicationVersion;
    @Getter @Setter private String aboutMessage;
    @Getter @Setter private String welcomeMessage;
    @Getter @Setter private String faviconContentType;
    
    // URL *not* sensitive to context path
    @Getter @Setter private String applicationCss;
    @Getter @Setter private String applicationJs;

    // URL sensitive to context path
    @Setter private String faviconUrl;
    @Setter private String brandLogoHeader;
    @Setter private String brandLogoSignin;
    
    @Getter @Setter private AbstractResource menubarsLayoutXml;
    
    public String getFaviconUrl() {
        return honorContextPath(faviconUrl);
    }

    public String getBrandLogoHeader() {
        return honorContextPath(brandLogoHeader);
    }

    public String getBrandLogoSignin() {
        return honorContextPath(brandLogoSignin);
    }

    // -- HELPER
    
    private String honorContextPath(String url) {
        return _Resources.prependContextPathIfRequired(url);
    }
    
    
}
