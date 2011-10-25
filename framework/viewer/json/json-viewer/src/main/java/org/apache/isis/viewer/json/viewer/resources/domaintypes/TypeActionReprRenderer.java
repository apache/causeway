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

import java.util.List;

import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkBuilder;
import org.apache.isis.viewer.json.viewer.representations.LinkFollower;
import org.apache.isis.viewer.json.viewer.representations.Rel;
import org.apache.isis.viewer.json.viewer.representations.ReprRenderer;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererFactoryAbstract;

import com.google.common.base.Strings;

public class TypeActionReprRenderer extends AbstractTypeMemberReprBuilder<TypeActionReprRenderer, ObjectAction> {

    public static class Factory extends ReprRendererFactoryAbstract {

        public Factory() {
            super(RepresentationType.TYPE_ACTION);
        }

        @Override
        public ReprRenderer<?,?> newRenderer(ResourceContext resourceContext, LinkFollower linkFollower, JsonRepresentation representation) {
            return new TypeActionReprRenderer(resourceContext, linkFollower, getRepresentationType(), representation);
        }
    }

    public static LinkBuilder newLinkToBuilder(ResourceContext resourceContext, Rel rel, ObjectSpecification objectSpecification, ObjectAction objectAction) {
        String typeFullName = objectSpecification.getFullIdentifier();
        String actionId = objectAction.getId();
        String url = "domainTypes/" + typeFullName + "/actions/" + actionId;
        return LinkBuilder.newBuilder(resourceContext, rel, RepresentationType.TYPE_ACTION, url);
    }

    public TypeActionReprRenderer(ResourceContext resourceContext, LinkFollower linkFollower, RepresentationType representationType, JsonRepresentation representation) {
        super(resourceContext, linkFollower, representationType, representation);
    }

    
    @Override
    protected void addLinksSpecificToFeature() {
        addLinksForParameters();
        addLinkToReturnTypeIfAny();
        addLinkToElementTypeIfAny();
    }

    private void addLinksForParameters() {
        if(parentSpec == null) {
            return;
        }
        final List<ObjectActionParameter> parameters = getObjectFeature().getParameters();
        for (ObjectActionParameter parameter : parameters) {
            final LinkBuilder linkBuilder = TypeActionParamReprRenderer.newLinkToBuilder(getResourceContext(), Rel.ACTION_PARAM, parentSpec, parameter);
            getLinks().arrayAdd(linkBuilder.build());
        }
        
    }

    protected void addLinkToElementTypeIfAny() {
        final TypeOfFacet facet = getObjectFeature().getFacet(TypeOfFacet.class);
        if(facet == null) {
            return;
        } 
        final ObjectSpecification typeOfSpec = facet.valueSpec();
        final LinkBuilder linkBuilder = DomainTypeReprRenderer.newLinkToBuilder(getResourceContext(), Rel.ELEMENT_TYPE, typeOfSpec);
        getLinks().arrayAdd(linkBuilder.build());
    }

    private void addLinkToReturnTypeIfAny() {
        final ObjectSpecification returnType = getObjectFeature().getReturnType();
        if(returnType == null) {
            return;
        }
        final LinkBuilder linkBuilder = 
            DomainTypeReprRenderer.newLinkToBuilder(getResourceContext(), Rel.RETURN_TYPE, returnType);
        getLinks().arrayAdd(linkBuilder.build());
    }

    @Override
    protected void putExtensionsSpecificToFeature() {
        putExtensionsName();
        putExtensionsDescriptionIfAvailable();
    }


}