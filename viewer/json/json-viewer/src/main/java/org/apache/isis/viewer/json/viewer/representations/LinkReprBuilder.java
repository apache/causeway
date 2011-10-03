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

import javax.ws.rs.core.MediaType;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.viewer.ResourceContext;

public class LinkReprBuilder extends ReprBuilderAbstract<LinkReprBuilder> {

    public static LinkReprBuilder newBuilder(ResourceContext resourceContext, String rel, RepresentationType representationType, String hrefFormat, Object... hrefArgs) {
        return newBuilder(resourceContext, rel, representationType.getMediaType(), hrefFormat, hrefArgs);
    }

    public static LinkReprBuilder newBuilder(ResourceContext resourceContext, String rel, MediaType mediaType, String hrefFormat, Object... hrefArgs) {
        return new LinkReprBuilder(resourceContext, rel, String.format(hrefFormat, hrefArgs), mediaType);
    }

	private final String rel;
    private final String href;
    private final MediaType mediaType;
    
    private HttpMethod method = HttpMethod.GET;
    private String title;
    private JsonRepresentation arguments;
    private JsonRepresentation value;
    
    protected LinkReprBuilder(ResourceContext resourceContext, String rel, String href, MediaType mediaType) {
        super(resourceContext);
        this.rel = rel;
        this.href = href;
        this.mediaType = mediaType;
    }
    public LinkReprBuilder withHttpMethod(HttpMethod method) {
        this.method = method;
        return this;
    }
    public LinkReprBuilder withTitle(String title) {
        this.title = title;
        return this;
    }
    public LinkReprBuilder withArguments(JsonRepresentation arguments) {
        this.arguments = arguments;
        return this;
    }
    public LinkReprBuilder withValue(JsonRepresentation value) {
        this.value = value;
        return this;
    }

    public JsonRepresentation render() {
        representation.mapPut("rel", rel);
        representation.mapPut("href", resourceContext.urlFor(href));
        representation.mapPut("method", method);
        representation.mapPut("type", mediaType.toString());
        representation.mapPut("title", title);
        representation.mapPut("arguments", arguments);
        representation.mapPut("value", value);
        return representation;
    }
    
}