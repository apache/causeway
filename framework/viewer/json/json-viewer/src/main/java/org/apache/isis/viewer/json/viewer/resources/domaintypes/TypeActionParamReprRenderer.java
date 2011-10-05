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
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.PathFollower;
import org.apache.isis.viewer.json.viewer.representations.LinkBuilder;
import org.apache.isis.viewer.json.viewer.representations.ReprRenderer;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererFactoryAbstract;

public class TypeActionParamReprRenderer extends AbstractTypeFeatureReprBuilder<TypeActionParamReprRenderer, ObjectActionParameter> {

    public static class Factory extends ReprRendererFactoryAbstract {

        public Factory() {
            super(RepresentationType.TYPE_ACTION_PARAMETER);
        }

        @Override
        public ReprRenderer<?,?> newRenderer(ResourceContext resourceContext, PathFollower pathFollower, JsonRepresentation representation) {
            return new TypeActionParamReprRenderer(resourceContext, pathFollower, getRepresentationType(), representation);
        }
    }

    public static LinkBuilder newLinkToBuilder(ResourceContext resourceContext, String rel, ObjectSpecification objectSpecification, ObjectActionParameter objectActionParameter) {
        String typeFullName = objectSpecification.getFullIdentifier();
        final ObjectAction objectAction = objectActionParameter.getAction();
        String actionId = objectAction.getId();
        final String paramName = objectActionParameter.getName();
        String url = String.format("domainTypes/%s/actions/%s/params/%s", typeFullName, actionId, paramName);
        return LinkBuilder.newBuilder(resourceContext, rel, RepresentationType.TYPE_ACTION_PARAMETER, url);
    }

    public TypeActionParamReprRenderer(ResourceContext resourceContext, PathFollower pathFollower, RepresentationType representationType, JsonRepresentation representation) {
        super(resourceContext, pathFollower, representationType, representation);
    }


    protected void includeSelfIfRequired() {
    }

    public JsonRepresentation render() {

        // self
        if(includesSelf) {
            representation.mapPut("self", 
                newLinkToBuilder(getResourceContext(), "self", getObjectSpecification(), getObjectFeature()).build());
        }
        
        // links and extensions
        representation.mapPut("links", JsonRepresentation.newArray());
        representation.mapPut("extensions", JsonRepresentation.newMap());

        return representation;
    }


}