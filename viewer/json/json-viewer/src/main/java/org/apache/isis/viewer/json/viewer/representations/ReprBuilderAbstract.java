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

public abstract class ReprBuilderAbstract<T extends ReprBuilderAbstract<T>> implements ReprBuilder {

    protected final ResourceContext resourceContext;
    protected final JsonRepresentation representation;
    
    public ReprBuilderAbstract(ResourceContext resourceContext) {
        this(resourceContext, JsonRepresentation.newMap());
    }

    public ReprBuilderAbstract(ResourceContext resourceContext, JsonRepresentation representation) {
        this.resourceContext = resourceContext;
        this.representation = representation;
    }

    public ResourceContext getResourceContext() {
        return resourceContext;
    }

    public ReprBuilderAbstract<T> withLinks() {
        return withLinks(JsonRepresentation.newArray());
    }

    public T withLinks(JsonRepresentation links) {
        if(!links.isArray()) {
            throw new IllegalArgumentException("links must be a list");
        }
        representation.mapPut("links", links);
        return cast(this);
    }

    public T withExtensions() {
        return withExtensions(JsonRepresentation.newMap());
    }

    public T withExtensions(JsonRepresentation extensions) {
        if(!extensions.isMap()) {
            throw new IllegalArgumentException("extensions must be a map");
        }
        representation.mapPut("extensions", extensions);
        return cast(this);
    }

    @SuppressWarnings("unchecked")
    protected static <T extends ReprBuilderAbstract<T>> T cast(ReprBuilderAbstract<T> builder) {
        return (T) builder;
    }

    public abstract JsonRepresentation render();
    
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