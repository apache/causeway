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
package org.apache.isis.viewer.json.applib.domainobjects;

import org.codehaus.jackson.JsonNode;

import org.apache.isis.viewer.json.applib.JsonRepresentation;

public abstract class AbstractDomainObjectRepresentation extends DomainRepresentation {

    public AbstractDomainObjectRepresentation(final JsonNode jsonNode) {
        super(jsonNode);
    }

    public String getTitle() {
        return getString("title");
    }

    public JsonRepresentation getMembers() {
        return getRepresentation("members").ensureArray();
    }

    public JsonRepresentation getProperty(final String id) {
        return getRepresentation("members[memberType=property id=%s]", id);
    }

    public JsonRepresentation getProperties() {
        return getRepresentation("members[memberType=property]").ensureArray();
    }

    public JsonRepresentation getCollection(final String id) {
        return getRepresentation("members[memberType=collection id=%s]", id);
    }

    public JsonRepresentation getCollections() {
        return getRepresentation("members[memberType=collection]").ensureArray();
    }

}
