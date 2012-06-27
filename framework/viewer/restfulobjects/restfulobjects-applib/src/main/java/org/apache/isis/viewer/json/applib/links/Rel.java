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
package org.apache.isis.viewer.json.applib.links;

public enum Rel {
    // IANA registered
    SELF("self"), DESCRIBEDBY("describedby"), UP("up"), HELP("help"), ICON("icon"),

    // Restful Objects namespace
    OBJECT("urn:org.restfulobjects:rels/object"), 
    SERVICE("urn:org.restfulobjects:rels/service"), 
    CHOICE("urn:org.restfulobjects:rels/choice"), 
    DEFAULT("urn:org.restfulobjects:rels/default"), 
    DETAILS("urn:org.restfulobjects:rels/details"), 
    MODIFY("urn:org.restfulobjects:rels/modify"), 
    CLEAR("urn:org.restfulobjects:rels/clear"), 
    ADD_TO("urn:org.restfulobjects:rels/add-to"), 
    REMOVE_FROM("urn:org.restfulobjects:rels/remove-from"), 
    INVOKE("urn:org.restfulobjects:rels/invoke"), 
    PERSIST("urn:org.restfulobjects:rels/persist"), 
    PROPERTY("urn:org.restfulobjects:rels/property"), 
    COLLECTION("urn:org.restfulobjects:rels/collection"), 
    ACTION("urn:org.restfulobjects:rels/action"), 
    TYPE_ACTION("urn:org.restfulobjects:rels/typeaction"), 
    ACTION_PARAM("urn:org.restfulobjects:rels/action-param"), 
    RETURN_TYPE("urn:org.restfulobjects:rels/return-type"), 
    ELEMENT_TYPE("urn:org.restfulobjects:rels/element-type"), 
    VERSION("urn:org.restfulobjects:rels/version"), 
    USER("urn:org.restfulobjects:rels/user"), 
    SERVICES("urn:org.restfulobjects:rels/services"), 
    DOMAIN_TYPES("urn:org.restfulobjects:rels/domain-types"), 
    DOMAIN_TYPE("urn:org.restfulobjects:rels/domain-type"),

    // implementation specific
    CONTRIBUTED_BY("contributedby");

    private final String name;

    private Rel(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}