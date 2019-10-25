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
package org.apache.isis.viewer.restfulobjects.rendering;


import javax.ws.rs.core.MediaType;

import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulHttpMethod;


public final class LinkBuilder {

    public static LinkBuilder newBuilder(final IResourceContext resourceContext, final String rel, final RepresentationType representationType, final String hrefFormat, final Object... hrefArgs) {
        return newBuilder(resourceContext, rel, representationType.getJsonElseXmlMediaType(), hrefFormat, hrefArgs);
    }

    public static LinkBuilder newBuilder(final IResourceContext resourceContext, final String rel, final MediaType mediaType, final String hrefFormat, final Object... hrefArgs) {
        return new LinkBuilder(resourceContext, rel, String.format(hrefFormat, hrefArgs), mediaType);
    }

    private final IResourceContext resourceContext;
    private final JsonRepresentation representation = JsonRepresentation.newMap();

    private final String rel;
    private final String href;
    private final MediaType mediaType;

    private RestfulHttpMethod method = RestfulHttpMethod.GET;
    private String title;
    private JsonRepresentation arguments;
    private JsonRepresentation value;

    protected LinkBuilder(final IResourceContext resourceContext, final String rel, final String href, final MediaType mediaType) {
        this.resourceContext = resourceContext;
        this.rel = rel;
        this.href = href;
        this.mediaType = mediaType;
    }

    public LinkBuilder withHttpMethod(final RestfulHttpMethod method) {
        this.method = method;
        return this;
    }

    public LinkBuilder withTitle(final String title) {
        this.title = title;
        return this;
    }

    public LinkBuilder withArguments(final JsonRepresentation arguments) {
        this.arguments = arguments;
        return this;
    }

    public LinkBuilder withValue(final JsonRepresentation value) {
        this.value = value;
        return this;
    }

    public JsonRepresentation build() {
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