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

import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.oid.stringable.OidStringifier;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.OidGenerator;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.viewer.ResourceContext;

public abstract class RepresentationBuilder<T extends RepresentationBuilder<T>> {

    protected final ResourceContext resourceContext;
    protected final JsonRepresentation representation = JsonRepresentation.newMap();
    
    public RepresentationBuilder(ResourceContext resourceContext) {
        this.resourceContext = resourceContext;
    }

    public T withSelf(String href) {
        representation.put("self", LinkBuilder.newBuilder(resourceContext, "self", href).build());
        return asT(this);
    }

    public RepresentationBuilder<T> withLinks() {
        return withLinks(JsonRepresentation.newArray());
    }

    public T withLinks(JsonRepresentation links) {
        if(!links.isArray()) {
            throw new IllegalArgumentException("links must be a list");
        }
        representation.put("links", links);
        return asT(this);
    }

    public T withMetadata() {
        return withMetadata(JsonRepresentation.newMap());
    }

    public T withMetadata(JsonRepresentation metadata) {
        if(!metadata.isMap()) {
            throw new IllegalArgumentException("metadata must be a map");
        }
        representation.put("metadata", metadata);
        return asT(this);
    }

    @SuppressWarnings("unchecked")
    private static <T extends RepresentationBuilder<T>> T asT(RepresentationBuilder<T> builder) {
        return (T) builder;
    }

    public abstract JsonRepresentation build();
    
    protected OidStringifier getOidStringifier() {
        return getOidGenerator().getOidStringifier();
    }

    protected OidGenerator getOidGenerator() {
        return getPersistenceSession().getOidGenerator();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected AuthenticationSession getSession() {
        return IsisContext.getAuthenticationSession();
    }

    protected Localization getLocalization() {
        return IsisContext.getLocalization();
    }

}