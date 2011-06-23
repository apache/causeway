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

public class LinkRepBuilder extends RepresentationBuilder {

    public static LinkRepBuilder newBuilder(RepContext repContext, String relSuffix, String url) {
        return new LinkRepBuilder(repContext, relSuffix, url);
    }
    public static TypeBuilder newTypeBuilder(RepContext repContext, String relSuffix, ObjectSpecification objectSpec) {
        return new TypeBuilder(repContext, relSuffix, objectSpec);
    }

    private final String relSuffix;
    private final String url;
    
    private HttpMethod method = HttpMethod.GET;
    private String title;
    private String body;
    
    public LinkRepBuilder(RepContext repContext, String relSuffix, String url) {
        super(repContext);
        this.relSuffix = relSuffix;
        this.url = url;
    }
    public LinkRepBuilder withMethod(HttpMethod method) {
        this.method = method;
        return this;
    }
    public LinkRepBuilder withTitle(String title) {
        this.title = title;
        return this;
    }
    public LinkRepBuilder withBody(String body) {
        this.body = body;
        return this;
    }
    public Representation build() {
        representation.put("rel", repContext.relFor(relSuffix));
        representation.put("url", repContext.urlFor(url));
        representation.put("method", method);
        representation.put("title", title);
        representation.put("body", body);
        return representation;
    }
}