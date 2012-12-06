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

import org.apache.isis.core.commons.config.ConfigurationConstants;

public final class HtmlViewerConstants {

    public static final String PROPERTY_BASE = ConfigurationConstants.ROOT + "viewer.html.";
    public static final String STYLE_SHEET = PROPERTY_BASE + "style-sheet";
    public static final String HEADER_FILE = PROPERTY_BASE + "header-file";
    /**
     * Used if {@link #HEADER_FILE} is not specified or does not refer to a
     * valid resource.
     */
    public static final String HEADER = PROPERTY_BASE + "header";
    public static final String FOOTER_FILE = PROPERTY_BASE + "footer-file";
    /**
     * Used if {@link #FOOTER_FILE} is not specified or does not refer to a
     * valid resource.
     */
    public static final String FOOTER = PROPERTY_BASE + "footer";

    public static final String VIEWER_HTML_RESOURCE_BASE_KEY = PROPERTY_BASE + "resourceBase";

    private HtmlViewerConstants() {
    }

}
