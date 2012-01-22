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
    ICON16("icon16"), ICON32("icon32"), OBJECT("object"), SERVICE("service"), CHOICE("choice"), DEFAULT("default"), DETAILS("details"), MODIFY("modify"), CLEAR("clear"), ADD_TO("addto"), REMOVE_FROM("removefrom"), INVOKE("invoke"), PERSIST("persist"), PROPERTY("property"), COLLECTION("collection"), ACTION(
            "action"), TYPE_ACTION("typeaction"), ACTION_PARAM("actionparam"), RETURN_TYPE("returntype"), ELEMENT_TYPE("elementtype"), VERSION("version"), USER("user"), SERVICES("services"), TYPES("types"), DOMAIN_TYPE("domaintype"),

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