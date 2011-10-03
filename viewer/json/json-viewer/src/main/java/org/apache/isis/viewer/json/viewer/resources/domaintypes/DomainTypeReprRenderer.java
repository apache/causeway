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
package org.apache.isis.viewer.json.viewer.resources.domaintypes;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkReprBuilder;
import org.apache.isis.viewer.json.viewer.representations.ReprRenderer;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererAbstract;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererFactoryAbstract;

public class DomainTypeReprRenderer extends ReprRendererAbstract<DomainTypeReprRenderer, ObjectSpecification> {

    public static class Factory extends ReprRendererFactoryAbstract {
        public Factory() {
            super(RepresentationType.DOMAIN_TYPE);
        }

        @Override
        public ReprRenderer<?, ?> newRenderer(ResourceContext resourceContext, JsonRepresentation representation) {
            return new DomainTypeReprRenderer(resourceContext, getRepresentationType(), representation);
        }
    }

    public static LinkReprBuilder newLinkToBuilder(ResourceContext resourceContext, String rel, ObjectSpecification objectSpec) {
        String typeFullName = objectSpec.getFullIdentifier();
        String url = "domainTypes/" + typeFullName;
        return LinkReprBuilder.newBuilder(resourceContext, rel, RepresentationType.DOMAIN_TYPE, url);
    }

    private ObjectSpecification objectSpecification;

    public DomainTypeReprRenderer(ResourceContext resourceContext, RepresentationType representationType, JsonRepresentation representation) {
        super(resourceContext, representationType, representation);
    }

    @Override
    public DomainTypeReprRenderer with(ObjectSpecification objectSpecification) {
        this.objectSpecification = objectSpecification;
        return cast(this);
    }


    public JsonRepresentation render() {

        // self
        if(includesSelf) {
            representation.mapPut("self", newLinkToBuilder(getResourceContext(), "self", objectSpecification).render());
        }
        
        
        
        // links and extensions
        representation.mapPut("links", JsonRepresentation.newArray());
        representation.mapPut("extensions", JsonRepresentation.newMap());

        return representation;
    }

}