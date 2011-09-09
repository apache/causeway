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
package org.apache.isis.viewer.json.viewer.representations;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.viewer.ResourceContext;

public class LinkBuilder extends RepresentationBuilder<LinkBuilder> {

    public static LinkBuilder newBuilder(ResourceContext resourceContext, String rel, String hrefFormat, Object... args) {
        return new LinkBuilder(resourceContext, rel, String.format(hrefFormat, args));
    }

	private final String rel;
    private final String href;
    
    private HttpMethod method = HttpMethod.GET;
    private String title;
    private JsonRepresentation arguments;
    
    private LinkBuilder(ResourceContext resourceContext, String rel, String href) {
        super(resourceContext);
        this.rel = rel;
        this.href = href;
    }
    public LinkBuilder withHttpMethod(HttpMethod method) {
        this.method = method;
        return this;
    }
    public LinkBuilder withTitle(String title) {
        this.title = title;
        return this;
    }
    public LinkBuilder withArguments(JsonRepresentation arguments) {
        this.arguments = arguments;
        return this;
    }
    public JsonRepresentation build() {
        representation.mapPut("rel", rel);
        representation.mapPut("href", resourceContext.urlFor(href));
        representation.mapPut("method", method);
        representation.mapPut("title", title);
        representation.mapPut("arguments", arguments);
        return representation;
    }

}