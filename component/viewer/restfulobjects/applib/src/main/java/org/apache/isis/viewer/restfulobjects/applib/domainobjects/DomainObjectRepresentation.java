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
     * Populated only for domain objects, not for domain services.
     */
    public String getDomainType() {
        return getString("instanceId");
    }

    /**
     * Populated only for domain objects, not for domain services.
     */
    public String getInstanceId() {
        return getString("instanceId");
    }

    /**
     * Populated only for domain services, not for domain objects.
     */
    public String getServiceId() {
        return getString("serviceId");
    }

    public JsonRepresentation getMembers() {
        return getRepresentation("members");
    }

    public DomainObjectMemberRepresentation getProperty(final String id) {
        return getMember(id, "property");
    }

    public JsonRepresentation getProperties() {
        return getRepresentation("members[memberType=property]").ensureArray();
    }

    public DomainObjectMemberRepresentation getCollection(final String id) {
        return getMember(id, "collection");
    }

    public JsonRepresentation getCollections() {
        return getRepresentation("members[memberType=collection]").ensureArray();
    }

    public DomainObjectMemberRepresentation getAction(final String id) {
        return getMember(id, "action");
    }

    private DomainObjectMemberRepresentation getMember(final String id, String memberType) {
        // TODO: would be nice to use "members.%s[memberType=...]" instead
        JsonRepresentation jsonRepr = getRepresentation("members.%s", id);
        if(jsonRepr == null) {
            return null;
        }
        DomainObjectMemberRepresentation member = jsonRepr.as(DomainObjectMemberRepresentation.class);
        return member.getMemberType().equals(memberType) ? member : null;
    }

    public JsonRepresentation getActions() {
        return getRepresentation("members[memberType=action]");
    }

    /**
     * Only for transient, persistable, objects
     */
    public LinkRepresentation getPersistLink() {
        return getLinkWithRel(Rel.PERSIST);
    }

    
    /**
     * Isis extension.
     */
    public String getOid() {
        return getString("extensions.oid");
    }


}
