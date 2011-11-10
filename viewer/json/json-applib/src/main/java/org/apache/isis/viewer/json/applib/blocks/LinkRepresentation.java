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
package org.apache.isis.viewer.json.applib.blocks;


import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;


public final class LinkRepresentation extends JsonRepresentation {
    
    public LinkRepresentation() {
        this(new ObjectNode(JsonNodeFactory.instance));
        withMethod(Method.GET);
    }

    public LinkRepresentation(JsonNode jsonNode) {
        super(jsonNode);
    }

    public String getRel() {
        return asObjectNode().path("rel").getTextValue();
    }
    
    public LinkRepresentation withRel(String rel) {
        asObjectNode().put("rel", rel);
        return this;
    }

    public String getHref() {
        return asObjectNode().path("href").getTextValue();
    }
    public LinkRepresentation withHref(String href) {
        asObjectNode().put("href", href);
        return this;
    }

    public JsonRepresentation getValue() {
        return getRepresentation("value");
    }

    public Method getMethod() {
        String methodStr = asObjectNode().path("method").getTextValue();
        return Method.valueOf(methodStr);
    }

    public MediaType getType() {
        String typeStr = asObjectNode().path("type").getTextValue();
        if(typeStr == null) { return MediaType.APPLICATION_JSON_TYPE; }
        return MediaType.valueOf(typeStr);
    }


    public LinkRepresentation withMethod(Method method) {
        asObjectNode().put("method", method.name());
        return this;
    }

    /**
     * Returns the &quot;arguments&quot; json-property of the link (a map).
     * 
     * <p>
     * If there is no &quot;arguments&quot; node, then as a convenience
     * will return an empty map.
     * @return
     */
    public JsonRepresentation getArguments() {
        JsonNode arguments = asObjectNode().get("arguments");
        if(arguments.isNull()) {
            return JsonRepresentation.newMap();
        }
        return new JsonRepresentation(arguments);
    }
    
    public <T> Response follow(ClientExecutor executor) throws Exception {
        return follow(executor, null);
    }

    public <T> Response follow(ClientExecutor executor, JsonRepresentation requestArgs) throws Exception {
        ClientRequest restEasyRequest = executor.createRequest(getHref());
        restEasyRequest.accept(MediaType.APPLICATION_JSON_TYPE);
        
        getMethod().setUp(restEasyRequest, requestArgs);
        
        @SuppressWarnings("unchecked")
        ClientResponse<T> restEasyResponse = executor.execute(restEasyRequest);
        return restEasyResponse;
    }

    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getHref() == null) ? 0 : getHref().hashCode());
        result = prime * result + ((getMethod() == null) ? 0 : getMethod().hashCode());
        result = prime * result + ((getRel() == null) ? 0 : getRel().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LinkRepresentation other = (LinkRepresentation) obj;
        if (getHref() == null) {
            if (other.getHref() != null)
                return false;
        } else if (!getHref().equals(other.getHref()))
            return false;
        if (getMethod() != other.getMethod())
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Link [rel=" + getRel() + ", href=" + getHref() + ", method=" + getMethod() + ", type=" + getType() + "]";
    }


    
}
