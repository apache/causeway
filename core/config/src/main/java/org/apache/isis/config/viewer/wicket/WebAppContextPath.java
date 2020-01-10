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

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;
import javax.inject.Singleton;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.resources._Resources;

import lombok.Getter;

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
     * In the form "xxx/yyy" (no leading nor trailing '/').
     */
    @Getter
    private String contextPath = "";
    private static final Pattern pattern = Pattern.compile("^[/]*(.+?)[/]*$");;

    public void setContextPath(final String contextPath) {
        if(contextPath == null) {
            this.contextPath = "";
            return;
        }
        final Matcher matcher = pattern.matcher(contextPath);
        this.contextPath = matcher.matches()
                                ? matcher.group(1)
                                : "";
    }

    public final String prependContextPath(String path) {

        if(path==null) {
            return getContextPath();
        }

        final String contextPath = getContextPath();
        if(_Strings.isNullOrEmpty(contextPath)) {
            return path;
        }

        return contextPath + prefixed(path, "/");
    }

    private static String prefixed(final String path, final String prefix) {
        return !path.startsWith(prefix)
                ? prefix + path
                : path;
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


}
