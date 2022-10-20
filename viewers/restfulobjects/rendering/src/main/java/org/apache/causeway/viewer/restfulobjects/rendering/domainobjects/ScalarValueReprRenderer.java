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
package org.apache.causeway.viewer.restfulobjects.rendering.domainobjects;

import javax.ws.rs.core.MediaType;

import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.HasObjectFeature;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.Rel;
import org.apache.causeway.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkFollowSpecs;
import org.apache.causeway.viewer.restfulobjects.rendering.ReprRendererAbstract;
import org.apache.causeway.viewer.restfulobjects.rendering.ReprRendererException;
import org.apache.causeway.viewer.restfulobjects.rendering.service.valuerender.JsonValueConverter;

import lombok.Getter;
import lombok.val;

public class ScalarValueReprRenderer
extends ReprRendererAbstract<ManagedObject>
implements HasObjectFeature {

    private ObjectSpecification returnType;
    @Getter(onMethod_ = {@Override}) private ObjectFeature objectFeature;

    public ScalarValueReprRenderer(
            final IResourceContext resourceContext,
            final ObjectFeature objectFeature,
            final LinkFollowSpecs linkFollower,
            final JsonRepresentation representation) {
        // null for representationType (there is none)
        super(resourceContext, linkFollower, null, representation);
        this.objectFeature = objectFeature;
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
        if (!objectAdapter.getSpecification().isValue()) {
            throw ReprRendererException.create("Not an (encodable) value", objectAdapter.getTitle());
        }

        val context = JsonValueConverter.Context.of(
                getObjectFeature(),
                getResourceContext().config().isSuppressMemberExtensions());
        final Object value = jsonValueEncoder.asObject(objectAdapter, context);

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