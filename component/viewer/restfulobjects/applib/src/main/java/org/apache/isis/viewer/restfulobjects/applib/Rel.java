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

public enum Rel {
    
    // IANA registered
    SELF("self"), 
    DESCRIBEDBY("describedby"), 
    UP("up"), 
    PREVIOUS("previous"),
    NEXT("next"),
    HELP("help"), 
    ICON("icon"),

    // Restful Objects namespace
    ACTION(Spec.REL_PREFIX + "action"), 
    ACTION_PARAM(Spec.REL_PREFIX + "action-param"), 
    ADD_TO(Spec.REL_PREFIX + "add-to"), 
    ATTACHMENT(Spec.REL_PREFIX + "attachment"), 
    CHOICE(Spec.REL_PREFIX + "choice"),
    CLEAR(Spec.REL_PREFIX + "clear"), 
    COLLECTION(Spec.REL_PREFIX + "collection"), 
    DEFAULT(Spec.REL_PREFIX + "default"), 
    DELETE(Spec.REL_PREFIX + "delete"), 
    DETAILS(Spec.REL_PREFIX + "details"), 
    DOMAIN_TYPE(Spec.REL_PREFIX + "domain-type"),
    DOMAIN_TYPES(Spec.REL_PREFIX + "domain-types"), 
    ELEMENT(Spec.REL_PREFIX + "element"), 
    ELEMENT_TYPE(Spec.REL_PREFIX + "element-type"), 
    INVOKE(Spec.REL_PREFIX + "invoke"), 
    MODIFY(Spec.REL_PREFIX + "modify"), 
    PERSIST(Spec.REL_PREFIX + "persist"), 
    PROPERTY(Spec.REL_PREFIX + "property"), 
    REMOVE_FROM(Spec.REL_PREFIX + "remove-from"), 
    RETURN_TYPE(Spec.REL_PREFIX + "return-type"), 
    SERVICE(Spec.REL_PREFIX + "service"), 
    SERVICES(Spec.REL_PREFIX + "services"), 
    UPDATE(Spec.REL_PREFIX + "update"), 
    USER(Spec.REL_PREFIX + "user"), 
    VALUE(Spec.REL_PREFIX + "value"), 
    VERSION(Spec.REL_PREFIX + "version"), 
    

    // implementation specific
    CONTRIBUTED_BY(Impl.REL_PREFIX + "contributed-by");

    private final String name;

    private Rel(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * For those {@link Rel}s that also take a param
     */
    public String andParam(String paramName, String paramValue) {
        return name + ";" + paramName + "=" + "\"" + paramValue + "\"";
    }

    private static class Spec {
        final static String REL_PREFIX = "urn:org.restfulobjects:rels/";
    }

    private static class Impl {
        final static String REL_PREFIX = "urn:org.apache.isis.restfulobjects:rels/";
    }

}
