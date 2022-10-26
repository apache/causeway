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
package org.apache.causeway.viewer.restfulobjects.applib.domainobjects;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation.HasExtensions;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation.HasLinkToSelf;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation.HasLinks;
import org.apache.causeway.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.Rel;

/**
 * @since 1.x {@index}
 */
public abstract class DomainRepresentation
extends JsonRepresentation
implements HasLinkToSelf, HasLinks, HasExtensions {

    public DomainRepresentation(final JsonNode jsonNode) {
        super(jsonNode);
    }

    @Override
    public LinkRepresentation getSelf() {
        return getLinkWithRel(Rel.SELF);
    }

    @Override
    public JsonRepresentation getLinks() {
        return getArray("links").ensureArray();
    }

    public LinkRepresentation getLinkWithRel(final Rel rel) {

        return getLinks().streamArrayElements(LinkRepresentation.class)
                .filter(linkRepr->rel.matches(linkRepr.getRel()))
                .findFirst()
                .orElse(null);
    }

    public LinkRepresentation getLinkWithRel(final String rel) {
        return getLink(String.format("links[rel=%s]", rel));
    }

    @Override
    public JsonRepresentation getExtensions() {
        return getMap("extensions");
    }

}
