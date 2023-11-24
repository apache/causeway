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

import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @since 1.x {@index}
 */
public final class LinkRepresentation extends JsonRepresentation {

    public LinkRepresentation() {
        this(new ObjectNode(JsonNodeFactory.instance));
        withMethod(RestfulHttpMethod.GET);
    }

    public LinkRepresentation(final JsonNode jsonNode) {
        super(jsonNode);
    }

    public String getRel() {
        return asObjectNode().path("rel").textValue();
    }

    public LinkRepresentation withRel(final String rel) {
        asObjectNode().put("rel", rel);
        return this;
    }

    public LinkRepresentation withRel(Rel rel) {
        return withRel(rel.getName());
    }


    public String getHref() {
        return asObjectNode().path("href").textValue();
    }

    public LinkRepresentation withHref(final String href) {
        asObjectNode().put("href", href);
        return this;
    }

    public JsonRepresentation getValue() {
        return getRepresentation("value");
    }

    public String getTitle() {
        return getString("title");
    }

    public LinkRepresentation withTitle(final String title) {
        asObjectNode().put("title", title);
        return this;
    }

    public RestfulHttpMethod getHttpMethod() {
        final String methodStr = asObjectNode().path("method").textValue();
        return RestfulHttpMethod.valueOf(methodStr);
    }

    public MediaType getType() {
        final String typeStr = asObjectNode().path("type").textValue();
        if (typeStr == null) {
            return MediaType.APPLICATION_JSON_TYPE;
        }
        return MediaType.valueOf(typeStr);
    }

    public LinkRepresentation withMethod(final RestfulHttpMethod httpMethod) {
        asObjectNode().put("method", httpMethod.name());
        return this;
    }

    /**
     * Returns the &quot;arguments&quot; json-property of the link (a map).
     *
     * <p>
     * If there is no &quot;arguments&quot; node, then as a convenience will
     * return an empty map.
     */
    public JsonRepresentation getArguments() {
        final JsonNode arguments = asObjectNode().get("arguments");
        if (arguments == null || arguments.isNull()) {
            return JsonRepresentation.newMap();
        }
        return new JsonRepresentation(arguments);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getHref() == null) ? 0 : getHref().hashCode());
        result = prime * result + ((getHttpMethod() == null) ? 0 : getHttpMethod().hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LinkRepresentation other = (LinkRepresentation) obj;
        if (getHref() == null) {
            if (other.getHref() != null) {
                return false;
            }
        } else if (!getHref().equals(other.getHref())) {
            return false;
        }
        if (getHttpMethod() != other.getHttpMethod()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Link [rel=" + getRel() + ", href=" + getHref() + ", method=" + getHttpMethod() + ", type=" + getType() + "]";
    }


}
