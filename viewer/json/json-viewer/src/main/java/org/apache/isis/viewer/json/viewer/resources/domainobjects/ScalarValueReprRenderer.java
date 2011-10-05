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
package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.json.viewer.JsonApplicationException;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.PathFollower;
import org.apache.isis.viewer.json.viewer.representations.ReprRenderer;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererAbstract;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererFactoryAbstract;

public class ScalarValueReprRenderer extends ReprRendererAbstract<ScalarValueReprRenderer, ObjectAdapter> {

    public static class Factory extends ReprRendererFactoryAbstract {
        public Factory() {
            super(RepresentationType.SCALAR_VALUE);
        }

        @Override
        public ReprRenderer<?, ?> newRenderer(ResourceContext resourceContext, PathFollower pathFollower, JsonRepresentation representation) {
            return new ScalarValueReprRenderer(resourceContext, pathFollower, getRepresentationType(), representation);
        }
    }

    private ScalarValueReprRenderer(final ResourceContext resourceContext, PathFollower pathFollower, RepresentationType representationType, JsonRepresentation representation) {
        super(resourceContext, pathFollower, representationType, representation);
    }

    public ScalarValueReprRenderer with(final ObjectAdapter objectAdapter) {
        final EncodableFacet facet = objectAdapter.getSpecification().getFacet(EncodableFacet.class);
        if(facet == null) {
            throw JsonApplicationException.create(HttpStatusCode.INTERNAL_SERVER_ERROR, "Not an (encodable) value", objectAdapter.titleString());
        }
        final String encodedString = facet.toEncodedString(objectAdapter);
        representation.mapPut("value", encodedString);
        return this;
    }

    @Override
    public JsonRepresentation render() {
 
        JsonRepresentation extensions = JsonRepresentation.newMap();
        putExtensionsIsisProprietary(extensions);
        withExtensions(extensions );
        
        JsonRepresentation links = JsonRepresentation.newArray();
        addLinksFormalDomainModel(links, resourceContext);
        addLinksIsisProprietary(links, resourceContext);
        withLinks(links);

        return representation;
    }

    /////////////////////////////////////////////////////
    // extensions and links
    /////////////////////////////////////////////////////
    
    private void putExtensionsIsisProprietary(JsonRepresentation extensions) {
    }

    private void addLinksFormalDomainModel(JsonRepresentation links, ResourceContext resourceContext) {
    }

    private void addLinksIsisProprietary(JsonRepresentation links, ResourceContext resourceContext) {
    }

    
}