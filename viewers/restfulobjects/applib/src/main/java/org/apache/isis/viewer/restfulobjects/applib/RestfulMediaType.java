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
package org.apache.isis.viewer.restfulobjects.applib;


/**
 * Media types including the <tt>profile</tt> parameter.
 *
 * <p>
 * Because these values are used in the <tt>@Produces</tt> annotation on the jax-rs
 * resources, they must be constants and must be strings.
 *
 * @see http://buzzword.org.uk/2009/draft-inkster-profile-parameter-00.html
 */
public final class RestfulMediaType {

    private RestfulMediaType() {
    }

    private static final String DQ = "\""; // double quotes, using abbreviation to reduce clutter
    
    private static final String PROFILE_PARAM_PREFIX = "urn:org.restfulobjects:repr-types/";
    
    private static final String JSON_BASE = "application/json" + ";profile=" + DQ + PROFILE_PARAM_PREFIX;

    public final static String APPLICATION_JSON_HOME_PAGE = JSON_BASE + "homepage" + DQ;
    public final static String APPLICATION_JSON_USER = JSON_BASE + "user" + DQ;
    public final static String APPLICATION_JSON_VERSION = JSON_BASE + "version" + DQ;
    public final static String APPLICATION_JSON_HEALTH = JSON_BASE + "health" + DQ;
    public final static String APPLICATION_JSON_LIST = JSON_BASE + "list" + DQ;
    public final static String APPLICATION_JSON_OBJECT = JSON_BASE + "object" + DQ;
    public final static String APPLICATION_JSON_OBJECT_PROPERTY = JSON_BASE + "object-property" + DQ;
    public final static String APPLICATION_JSON_OBJECT_COLLECTION = JSON_BASE + "object-collection" + DQ;
    public final static String APPLICATION_JSON_OBJECT_ACTION = JSON_BASE + "object-action" + DQ;
    public final static String APPLICATION_JSON_ACTION_RESULT = JSON_BASE + "action-result" + DQ;
    public final static String APPLICATION_JSON_TYPE_LIST = JSON_BASE + "type-list" + DQ;
    public final static String APPLICATION_JSON_DOMAIN_TYPE = JSON_BASE + "domain-type" + DQ;
    public final static String APPLICATION_JSON_PROPERTY_DESCRIPTION = JSON_BASE + "property-description" + DQ;
    public final static String APPLICATION_JSON_COLLECTION_DESCRIPTION = JSON_BASE + "collection-description" + DQ;
    public final static String APPLICATION_JSON_ACTION_DESCRIPTION = JSON_BASE + "action-description" + DQ;
    public final static String APPLICATION_JSON_ACTION_PARAMETER_DESCRIPTION = JSON_BASE + "action-param-description" + DQ;
    public final static String APPLICATION_JSON_ERROR = JSON_BASE + "error" + DQ;
    public final static String APPLICATION_JSON_TYPE_ACTION_RESULT = JSON_BASE + "type-action-result" + DQ;

    private static final String XML_BASE = "application/xml" + ";profile=" + DQ + PROFILE_PARAM_PREFIX;
    public final static String APPLICATION_XML_LAYOUT_BS3 = XML_BASE + "layout-bs3" + DQ;
    public final static String APPLICATION_JSON_LAYOUT_BS3 = JSON_BASE + "layout-bs3" + DQ;

    public final static String APPLICATION_XML_OBJECT_LAYOUT_BS3 = XML_BASE + "object-layout-bs3" + DQ;
    public final static String APPLICATION_JSON_OBJECT_LAYOUT_BS3 = JSON_BASE + "object-layout-bs3" + DQ;

    public final static String APPLICATION_JSON_LAYOUT_MENUBARS = JSON_BASE + "layout-menubars" + DQ;
    public final static String APPLICATION_XML_LAYOUT_MENUBARS = XML_BASE + "layout-menubars" + DQ;


    // currently only support a subset of representations in XML format

    //    public final static String APPLICATION_XML_HOME_PAGE = XML_BASE + "homepage";
    //    public final static String APPLICATION_XML_USER = XML_BASE + "user";
    //    public final static String APPLICATION_XML_VERSION = XML_BASE + "version";
    //    public final static String APPLICATION_XML_LIST = XML_BASE + "list";
    public final static String APPLICATION_XML_OBJECT = XML_BASE + "object" + DQ;
    public final static String APPLICATION_XML_OBJECT_PROPERTY = XML_BASE + "object-property" + DQ;
    public final static String APPLICATION_XML_OBJECT_COLLECTION = XML_BASE + "object-collection" + DQ;
    public final static String APPLICATION_XML_OBJECT_ACTION = XML_BASE + "object-action" + DQ;
    public final static String APPLICATION_XML_ACTION_RESULT = XML_BASE + "action-result" + DQ;
    //    public final static String APPLICATION_XML_TYPE_LIST = XML_BASE + "type-list";
    //    public final static String APPLICATION_XML_DOMAIN_TYPE = XML_BASE + "domain-type";
    //    public final static String APPLICATION_XML_PROPERTY_DESCRIPTION = XML_BASE + "property-description";
    //    public final static String APPLICATION_XML_COLLECTION_DESCRIPTION = XML_BASE + "collection-description";
    //    public final static String APPLICATION_XML_ACTION_DESCRIPTION = XML_BASE + "action-description";
    //    public final static String APPLICATION_XML_ACTION_PARAMETER_DESCRIPTION = XML_BASE + "action-param-description";
    public final static String APPLICATION_XML_ERROR = XML_BASE + "error" + DQ;
    //    public final static String APPLICATION_XML_TYPE_ACTION_RESULT = XML_BASE + "type-action-result";

    public final static String IMAGE_PNG = "image/png";

}
