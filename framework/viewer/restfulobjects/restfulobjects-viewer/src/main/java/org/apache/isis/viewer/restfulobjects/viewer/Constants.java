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
package org.apache.isis.viewer.restfulobjects.viewer;

import org.apache.isis.runtimes.dflt.runtime.system.SystemConstants;

public final class Constants {

    public static final String VIEWER_PREFIX_KEY = SystemConstants.VIEWER_KEY + ".restful";
    public static final String JAVASCRIPT_DEBUG_KEY = VIEWER_PREFIX_KEY + ".javascript-debug";

    public static final String ISIS_REST_SUPPORT_JS = "isis-rest-support.js";
    public static final String JQUERY_SRC_JS = "jquery-1.6.1.js";
    public static final String JQUERY_MIN_JS = "jquery-1.6.1.min.js";
    public static final String URL_ENCODING_CHAR_SET = org.apache.isis.viewer.restfulobjects.applib.Constants.URL_ENCODING_CHAR_SET;

    private Constants() {
    }

}
