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
package org.apache.isis.viewer.json.applib;

import javax.ws.rs.core.MediaType;

/**
 * Values per the profile parameter
 * 
 * @see http://buzzword.org.uk/2009/draft-inkster-profile-parameter-00.html
 */
public final class RestfulMediaType {

    private RestfulMediaType() {
    }

    private static final String BASE = MediaType.APPLICATION_JSON + ";profile=urn:org.restfulobjects:repr-types/";

    public final static String APPLICATION_JSON_HOME_PAGE = BASE + "homepage";
    public final static String APPLICATION_JSON_USER = BASE + "user";
    public final static String APPLICATION_JSON_VERSION = BASE + "version";
    public final static String APPLICATION_JSON_LIST = BASE + "list";
    public final static String APPLICATION_JSON_SCALAR_VALUE = BASE + "scalar-value";
    public final static String APPLICATION_JSON_DOMAIN_OBJECT = BASE + "object";
    public final static String APPLICATION_JSON_TRANSIENT_DOMAIN_OBJECT = BASE + "transient";
    public final static String APPLICATION_JSON_OBJECT_PROPERTY = BASE + "object-property";
    public final static String APPLICATION_JSON_OBJECT_COLLECTION = BASE + "object-collection";
    public final static String APPLICATION_JSON_OBJECT_ACTION = BASE + "object-action";
    public final static String APPLICATION_JSON_ACTION_RESULT = BASE + "action-result";
    public final static String APPLICATION_JSON_TYPE_LIST = BASE + "type-list";
    public final static String APPLICATION_JSON_DOMAIN_TYPE = BASE + "domain-type";
    public final static String APPLICATION_JSON_TYPE_ACTION_RESULT = BASE + "type-action-result";
    public final static String APPLICATION_JSON_PROPERTY_DESCRIPTION = BASE + "property-description";
    public final static String APPLICATION_JSON_COLLECTION_DESCRIPTION = BASE + "collection-description";
    public final static String APPLICATION_JSON_ACTION_DESCRIPTION = BASE + "action-description";
    public final static String APPLICATION_JSON_ACTION_PARAMETER_DESCRIPTION = BASE + "action-param-description";
    public final static String APPLICATION_JSON_ERROR = BASE + "error";

}
