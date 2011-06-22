/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.restful.viewer2.representations;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.restful.viewer2.RepContext;
import org.apache.isis.viewer.restful.viewer2.ResourceContext;
import org.apache.isis.viewer.restful.viewer2.representations.LinkRep.TypeBuilder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;


@JsonSerialize(include=Inclusion.NON_NULL)
public class LinkRep {

    private String rel;
    private String url;
    private HttpMethod method;
    private String title;
    private String body;
    private Object value;
    
    
    public String getRel() {
        return rel;
    }
    public void setRel(String rel) {
        this.rel = rel;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public HttpMethod getMethod() {
        return method;
    }
    public void setMethod(HttpMethod method) {
        this.method = method;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }
    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }

    public static LinkRep.TypeBuilder newTypeBuilder(RepContext repContext, String relSuffix, ObjectSpecification objectSpec) {
        return new LinkRep.TypeBuilder(repContext, relSuffix, objectSpec);
    }
    public static Builder newBuilder(RepContext repContext, String relSuffix, String url) {
        return new Builder(repContext, relSuffix, url);
    }
    
    public static class Builder {
        
        private final RepContext representationContext;
        private final String relSuffix;
        private final String url;
        
        private HttpMethod method = HttpMethod.GET;
        private String title;
        private String body;
        
        public Builder(RepContext representationContext, String relSuffix, String url) {
            this.representationContext = representationContext;
            this.relSuffix = relSuffix;
            this.url = url;
        }
        public LinkRep.Builder withMethod(HttpMethod method) {
            this.method = method;
            return this;
        }
        public LinkRep.Builder withTitle(String title) {
            this.title = title;
            return this;
        }
        public LinkRep.Builder withBody(String body) {
            this.body = body;
            return this;
        }
        public LinkRep build() {
            LinkRep linkRepresentation = new LinkRep();
            linkRepresentation.setMethod(method);
            linkRepresentation.setTitle(title);
            linkRepresentation.setRel(representationContext.relFor(relSuffix));
            linkRepresentation.setUrl(representationContext.urlFor(url));
            linkRepresentation.setBody(body);
            return linkRepresentation;
        }
    }

    public static class TypeBuilder {
        
        private final RepContext representationContext;
        private final String relSuffix;
        private final ObjectSpecification objectSpec;
        
        public TypeBuilder(RepContext representationContext, String relSuffix, ObjectSpecification objectSpec) {
            this.representationContext = representationContext;
            this.relSuffix = relSuffix;
            this.objectSpec = objectSpec;
        }
        
        public LinkRep build() {
            Builder typeBuilder = newBuilder(representationContext, relSuffix, urlFor(objectSpec));
            LinkRep linkRep = typeBuilder.build();
            return linkRep;
        }
    
        private static String urlFor(ObjectSpecification objectSpec) {
            return "types/application/vnd+" + objectSpec.getFullIdentifier();
        }
    }
}
