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

    SELF(RelDefinition.IANA, "self"),
    DESCRIBEDBY(RelDefinition.IANA, "describedby"),
    UP(RelDefinition.IANA,"up"),
    PREVIOUS(RelDefinition.IANA,"previous"),
    NEXT(RelDefinition.IANA,"next"),
    HELP(RelDefinition.IANA,"help"),
    ICON(RelDefinition.IANA,"icon"),

    // Restful Objects namespace
    ACTION(RelDefinition.RO_SPEC, "action"),
    ACTION_PARAM(RelDefinition.RO_SPEC, "action-param"),
    ADD_TO(RelDefinition.RO_SPEC, "add-to"),
    ATTACHMENT(RelDefinition.RO_SPEC, "attachment"),
    CHOICE(RelDefinition.RO_SPEC, "choice"),
    CLEAR(RelDefinition.RO_SPEC, "clear"),
    COLLECTION(RelDefinition.RO_SPEC, "collection"),
    DEFAULT(RelDefinition.RO_SPEC, "default"),
    DELETE(RelDefinition.RO_SPEC, "delete"),
    DETAILS(RelDefinition.RO_SPEC, "details"),
    DOMAIN_TYPE(RelDefinition.RO_SPEC, "domain-type"),
    DOMAIN_TYPES(RelDefinition.RO_SPEC, "domain-types"),
    ELEMENT(RelDefinition.RO_SPEC, "element"),
    ELEMENT_TYPE(RelDefinition.RO_SPEC, "element-type"),
    INVOKE(RelDefinition.RO_SPEC, "invoke"),
    MODIFY(RelDefinition.RO_SPEC, "modify"),
    PERSIST(RelDefinition.RO_SPEC, "persist"),
    PROPERTY(RelDefinition.RO_SPEC, "property"),
    REMOVE_FROM(RelDefinition.RO_SPEC, "remove-from"),
    RETURN_TYPE(RelDefinition.RO_SPEC, "return-type"),
    SERVICE(RelDefinition.RO_SPEC, "service"),
    SERVICES(RelDefinition.RO_SPEC, "services"),
    UPDATE(RelDefinition.RO_SPEC, "update"),
    USER(RelDefinition.RO_SPEC, "user"),
    VALUE(RelDefinition.RO_SPEC, "value"),
    VERSION(RelDefinition.RO_SPEC, "version"),


    // implementation specific
    CONTRIBUTED_BY(RelDefinition.IMPL, "contributed-by"),
    OBJECT_LAYOUT(RelDefinition.IMPL, "object-layout"),
    OBJECT_ICON(RelDefinition.IMPL, "object-icon"),
    LAYOUT(RelDefinition.IMPL, "layout"),
    MENUBARS(RelDefinition.IMPL, "menuBars"),
    LOGOUT(RelDefinition.IMPL, "logout");

    private final RelDefinition relDef;
    private final String relSuffix;

    private Rel(final RelDefinition relDef, final String name) {
        this.relDef = relDef;
        this.relSuffix = name;
    }

    public String getName() {
        return relDef.nameOf(relSuffix);
    }

    /**
     * For those {@link Rel}s that also take a param
     */
    public String andParam(String paramName, String paramValue) {
        return getName() +
                (relDef.canAddParams()
                        ?";" + paramName + "=" + "\"" + paramValue + "\""
                                :"");
    }

    public boolean matches(Rel otherRel) {
        return this == otherRel;
    }

    public boolean matches(final String otherRelStr) {
        final Rel otherRel = Rel.parse(otherRelStr);
        return matches(otherRel);
    }

    public static Rel parse(String str) {
        final int i = str.indexOf(";");
        if(i != -1) {
            str = str.substring(0, i);
        }
        for (Rel candidate: Rel.values()) {
            if(candidate.getName().equals(str)) {
                return candidate;
            }
        }
        return null;
    }
}
