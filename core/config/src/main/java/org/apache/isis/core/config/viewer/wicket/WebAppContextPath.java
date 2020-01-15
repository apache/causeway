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
package org.apache.isis.core.config.viewer.wicket;

import java.io.Serializable;
import java.util.regex.Pattern;

import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.resources._Resources;

import lombok.Getter;
import lombok.val;

/**
 * This class is {@link Serializable} so that it can be injected into Wicket components.
 */
@Service
@Singleton
@Named("isisConfig.WebAppContextPath")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
public class WebAppContextPath implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * Either "" or "/xxx".
     */
    @Getter
    private String contextPath = "";

    /**
     * 
     * @param contextPath - form of "xxx/yyy" (no leading nor trailing '/').
     */
    public void setContextPath(final String contextPath) {
        this.contextPath = normalizeContextPath(contextPath);
    }

    public final String prependContextPath(String path) {

        if(path==null) {
            return getContextPath();
        }

        final String contextPath = getContextPath();
        if(_Strings.isNullOrEmpty(contextPath)) {
            return path;
        }

        return contextPath +  _Strings.prefix(path, "/");
    }

    public String prependContextPathIfLocal(String url) {

        if(url==null) {
            return null;
        }
        if(_Resources.isLocalResource(url)) {
            return this.prependContextPath(url);
        }
        return url;
    }
    
    // -- HELPER
    
    private final Pattern pattern = Pattern.compile("^[/]*(.+?)[/]*$");
    
    private String normalizeContextPath(final String contextPath) {
        if(contextPath == null) {
            return "";
        }
        val matcher = pattern.matcher(contextPath);
        val path = matcher.matches()
                ? matcher.group(1) 
                        : "";
                
        if("".equals(path) || "/".equals(path)) {
            return "";
        }
        
        return ensureLeadingSlash(path);
    }
    
    private String ensureLeadingSlash(String url) {
        if(url==null || url.length()<2) {
            return url;
        }
        return !url.startsWith("/")
                ? "/" + url
                        : url;
    }


}
