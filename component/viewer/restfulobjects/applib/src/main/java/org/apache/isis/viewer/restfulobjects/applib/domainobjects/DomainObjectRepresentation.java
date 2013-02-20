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
package org.apache.isis.viewer.restfulobjects.applib.domainobjects;

import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.codehaus.jackson.JsonNode;

public class DomainObjectRepresentation extends DomainRepresentation  {

    public DomainObjectRepresentation(final JsonNode jsonNode) {
        super(jsonNode);
    }

    public String getTitle() {
        return getString("title");
    }

    /**
     * Only for persistent or addressable objects 
     */
    public String getOid() {
        return getString("oid");
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

    public JsonRepresentation getActions() {
        return getRepresentation("members[memberType=action]");
    }

    public JsonRepresentation getAction(final String id) {
        return getRepresentation("members[memberType=action id=%s]", id);
    }

    /**
     * Only for transient, persistable, objects
     */
    public LinkRepresentation getPersistLink() {
        return getLinkWithRel(Rel.PERSIST);
    }

}
