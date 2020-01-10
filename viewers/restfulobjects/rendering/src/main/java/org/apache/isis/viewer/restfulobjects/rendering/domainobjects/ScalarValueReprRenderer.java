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
package org.apache.isis.viewer.restfulobjects.rendering.domainobjects;

import javax.ws.rs.core.MediaType;

import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.isis.viewer.restfulobjects.rendering.LinkFollowSpecs;
import org.apache.isis.viewer.restfulobjects.rendering.ReprRendererAbstract;
import org.apache.isis.viewer.restfulobjects.rendering.ReprRendererException;

public class ScalarValueReprRenderer extends ReprRendererAbstract<ScalarValueReprRenderer, ManagedObject> {

    private ObjectSpecification returnType;

    public ScalarValueReprRenderer(
            final IResourceContext resourceContext,
            final LinkFollowSpecs linkFollower,
            final JsonRepresentation representation) {
        super(resourceContext, linkFollower, null, representation); // null for representationType (there is none)
    }

    /**
     * Fail early...
     *
     * <p>
     * In case I forget in the future that scalar values don't have a representation.
     */
    @Override
    public MediaType getMediaType() {
        throw new UnsupportedOperationException("no mediaType defined for scalar values");
    }

    @Override
    public ScalarValueReprRenderer with(final ManagedObject objectAdapter) {
        final EncodableFacet facet = objectAdapter.getSpecification().getFacet(EncodableFacet.class);
        if (facet == null) {
            throw ReprRendererException.create("Not an (encodable) value", objectAdapter.titleString());
        }
        String format = null; // TODO
        final Object value = jsonValueEncoder.asObject(objectAdapter, format);

        representation.mapPut("value", value);
        return this;
    }

    @Override
    public JsonRepresentation render() {

        addLinkToReturnType();

        getExtensions();

        return representation;
    }

    public ScalarValueReprRenderer withReturnType(final ObjectSpecification returnType) {
        this.returnType = returnType;
        return this;
    }

    private void addLinkToReturnType() {
        addLink(Rel.RETURN_TYPE, returnType);
    }

}