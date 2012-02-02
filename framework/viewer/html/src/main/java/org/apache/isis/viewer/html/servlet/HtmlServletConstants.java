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

package org.apache.isis.viewer.html.servlet;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.ConfigurationConstants;

public final class HtmlServletConstants {

    static final String PROPERTY_BASE = ConfigurationConstants.ROOT + "viewer.html.";

    static final String DEBUG_KEY = PROPERTY_BASE + "debug";

    static final String ENCODING_KEY = PROPERTY_BASE + "encoding";
    static final String ENCODING_DEFAULT = "ISO-8859-1";

    public static final String SUFFIX_INIT_PARAM = "viewer-html.suffix";
    public static final String SUFFIX_INIT_PARAM_VALUE_DEFAULT = "app";

    /**
     * Binding to the {@link AuthenticationSession}.
     */
    static final String AUTHENTICATION_SESSION_CONTEXT_KEY = "isis-context";

    public static final String LOGON_PAGE = "logon";
    static final String START_PAGE = "start";

    private HtmlServletConstants() {
    }

}
