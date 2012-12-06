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

package org.apache.isis.viewer.html;

import javax.servlet.ServletContext;

import org.apache.isis.viewer.html.servlet.HtmlServletConstants;

public class PathBuilderDefault implements PathBuilder {

    private static final long serialVersionUID = 1L;
    
    private final String suffix;

    private static String getSuffixInitParam(final ServletContext servletContext) {
        final String suffixInitParam = servletContext.getInitParameter(HtmlServletConstants.SUFFIX_INIT_PARAM);
        return suffixInitParam != null ? suffixInitParam : HtmlServletConstants.SUFFIX_INIT_PARAM_VALUE_DEFAULT;
    }

    public PathBuilderDefault(final ServletContext servletContext) {
        this(getSuffixInitParam(servletContext));
    }

    public PathBuilderDefault(final String suffix) {
        this.suffix = suffix;
    }

    @Override
    public String getSuffix() {
        return suffix;
    }

    @Override
    public String pathTo(final String prefix) {
        final StringBuilder buf = new StringBuilder(prefix);
        if (!prefix.endsWith(".")) {
            buf.append(".");
        }
        buf.append(suffix);
        return buf.toString();
    }

}
