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
package org.apache.isis.viewer.restfulobjects.applib.links;

public enum Rel {
    
    // IANA registered
    SELF("self"), 
    DESCRIBEDBY("describedby"), 
    UP("up"), 
    HELP("help"), 
    ICON("icon"),

    // Restful Objects namespace
    ICON16(Spec.REL_PREFIX + "icon16"), 
    ICON32(Spec.REL_PREFIX + "icon32"), 
    OBJECT(Spec.REL_PREFIX + "object"), 
    SERVICE(Spec.REL_PREFIX + "service"), 
    CHOICE(Spec.REL_PREFIX + "choice"), 
    DEFAULT(Spec.REL_PREFIX + "default"), 
    DETAILS(Spec.REL_PREFIX + "details"), 
    MODIFY(Spec.REL_PREFIX + "modify"), 
    CLEAR(Spec.REL_PREFIX + "clear"), 
    ADD_TO(Spec.REL_PREFIX + "addto"), 
    REMOVE_FROM(Spec.REL_PREFIX + "removefrom"), 
    INVOKE(Spec.REL_PREFIX + "invoke"), 
    PERSIST(Spec.REL_PREFIX + "persist"), 
    PROPERTY(Spec.REL_PREFIX + "property"), 
    COLLECTION(Spec.REL_PREFIX + "collection"), 
    ACTION(Spec.REL_PREFIX + "action"), 
    TYPE_ACTION(Spec.REL_PREFIX + "typeaction"), 
    ACTION_PARAM(Spec.REL_PREFIX + "actionparam"), 
    RETURN_TYPE(Spec.REL_PREFIX + "returntype"), 
    ELEMENT_TYPE(Spec.REL_PREFIX + "elementtype"), 
    VERSION(Spec.REL_PREFIX + "version"), 
    USER(Spec.REL_PREFIX + "user"), 
    SERVICES(Spec.REL_PREFIX + "services"), 
    TYPES(Spec.REL_PREFIX + "types"), 
    DOMAIN_TYPE(Spec.REL_PREFIX + "domaintype"),

    // implementation specific
    CONTRIBUTED_BY(Impl.REL_PREFIX + "contributedby");

    private final String name;

    private Rel(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private static class Spec {
        final static String REL_PREFIX = "urn:org.restfulobjects:rels/";
    }

    private static class Impl {
        final static String REL_PREFIX = "urn:org.apache.isis.restfulobjects:rels/";
    }

}
