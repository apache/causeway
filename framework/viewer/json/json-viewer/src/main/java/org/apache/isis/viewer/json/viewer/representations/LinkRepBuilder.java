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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.viewer.RepContext;
import org.apache.isis.viewer.json.viewer.resources.objects.DomainObjectRepBuilder;

public class LinkRepBuilder extends RepresentationBuilder {

    public static LinkRepBuilder newBuilder(RepContext repContext, String relSuffix, String url) {
        return new LinkRepBuilder(repContext, relSuffix, url);
    }
    public static TypeBuilder newTypeBuilder(RepContext repContext, ObjectSpecification objectSpec) {
        return new TypeBuilder(repContext, objectSpec);
    }

	public static LinkRepBuilder newObjectBuilder(RepContext repContext,
			ObjectAdapter elementAdapter, OidStringifier oidStringifier) {
    	String url = DomainObjectRepBuilder.urlFor(elementAdapter, oidStringifier);
        return LinkRepBuilder.newBuilder(repContext, "object", url);
	}

    private final String relSuffix;
    private final String href;
    
    private HttpMethod method = HttpMethod.GET;
    private String title;
    private Object body;
    
    public LinkRepBuilder(RepContext repContext, String relSuffix, String href) {
        super(repContext);
        this.relSuffix = relSuffix;
        this.href = href;
    }
    public LinkRepBuilder withHttpMethod(HttpMethod method) {
        this.method = method;
        return this;
    }
    public LinkRepBuilder withTitle(String title) {
        this.title = title;
        return this;
    }
    public LinkRepBuilder withBody(Object body) {
        this.body = body;
        return this;
    }
    public JsonRepresentation build() {
        representation.put("rel", relSuffix);
        representation.put("href", repContext.urlFor(href));
        representation.put("method", method);
        representation.put("title", title);
        representation.put("body", body);
        return representation;
    }
}