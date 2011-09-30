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

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.spec.ObjectActionSet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionContainer.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkReprBuilder;
import org.apache.isis.viewer.json.viewer.representations.RendererFactory;
import org.apache.isis.viewer.json.viewer.representations.RendererFactoryRegistry;
import org.apache.isis.viewer.json.viewer.representations.ReprRenderer;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererAbstract;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererFactoryAbstract;
import org.apache.isis.viewer.json.viewer.util.OidUtils;

import com.google.common.base.Function;

public class DomainObjectReprRenderer extends ReprRendererAbstract<DomainObjectReprRenderer, ObjectAdapter>{

    public static class Factory extends ReprRendererFactoryAbstract {

        public Factory() {
            super(RepresentationType.DOMAIN_OBJECT);
        }

        @Override
        public ReprRenderer<?,?> newRenderer(ResourceContext resourceContext, JsonRepresentation representation) {
            return new DomainObjectReprRenderer(resourceContext, getRepresentationType(), representation);
        }
    }
    
    public static LinkReprBuilder newLinkToBuilder(ResourceContext resourceContext, String rel, ObjectAdapter elementAdapter) {
        String oidStr = resourceContext.getOidStringifier().enString(elementAdapter.getOid());
        String url = "objects/" + oidStr;
        return LinkReprBuilder.newBuilder(resourceContext, rel, url);
    }

    private ObjectAdapterLinkToBuilder linkToBuilder;

    private DomainObjectReprRenderer(ResourceContext resourceContext, RepresentationType representationType, JsonRepresentation representation) {
        super(resourceContext, representationType, representation);
        usingLinkToBuilder(new DomainObjectLinkToBuilder());
    }

    /**
     * Override the default {@link ObjectAdapterLinkToBuilder} (that is used for generating links in
     * {@link #linkTo(ObjectAdapter)}).
     */
    public DomainObjectReprRenderer usingLinkToBuilder(ObjectAdapterLinkToBuilder objectAdapterLinkToBuilder) {
        this.linkToBuilder = objectAdapterLinkToBuilder.usingResourceContext(resourceContext);
        return this;
    }

    public DomainObjectReprRenderer with(ObjectAdapter objectAdapter) {
        if(includesSelf) {
            JsonRepresentation self = linkToBuilder.with(objectAdapter).linkToAdapter().render();
            representation.mapPut("self", self);
        }

        String title = objectAdapter.titleString();
        representation.mapPut("oid", OidUtils.getOidStr(resourceContext, objectAdapter));
        representation.mapPut("title", title);
        withMembers(objectAdapter);
        return this;
    }

    private DomainObjectReprRenderer withMembers(ObjectAdapter objectAdapter) {
        JsonRepresentation members = JsonRepresentation.newArray();
        List<ObjectAssociation> associations = objectAdapter.getSpecification().getAssociations();
        addAssociations(objectAdapter, members, associations);
        
        List<ObjectAction> actions = objectAdapter.getSpecification().getObjectActions(Contributed.INCLUDED);
        addActions(objectAdapter, actions, members);
        representation.mapPut("members", members);
        return this;
    }

    private void addAssociations(ObjectAdapter objectAdapter, JsonRepresentation members, List<ObjectAssociation> associations) {
        for (ObjectAssociation assoc : associations) {
            Consent visibility = assoc.isVisible(getSession(), objectAdapter);
            if(!visibility.isAllowed()) {
                continue;
            } 
            if(assoc instanceof OneToOneAssociation) {
                OneToOneAssociation property = (OneToOneAssociation)assoc;
                
                RendererFactory factory = RendererFactoryRegistry.instance.find(RepresentationType.OBJECT_PROPERTY);
                final ObjectPropertyReprRenderer renderer = 
                        (ObjectPropertyReprRenderer) factory.newRenderer(getResourceContext(), JsonRepresentation.newMap());
                
                renderer.with(new ObjectAndProperty(objectAdapter, property))
                        .usingLinkToBuilder(linkToBuilder)
                        .withDetailsLink();
                
                members.arrayAdd(renderer.render());
            }
            if(assoc instanceof OneToManyAssociation) {
                OneToManyAssociation collection = (OneToManyAssociation) assoc;

                RendererFactory factory = RendererFactoryRegistry.instance.find(RepresentationType.OBJECT_COLLECTION);
                final ObjectCollectionReprRenderer renderer = 
                        (ObjectCollectionReprRenderer) factory.newRenderer(getResourceContext(), JsonRepresentation.newMap());

                renderer.with(new ObjectAndCollection(objectAdapter, collection))
                    .usingLinkToBuilder(linkToBuilder)
                    .withDetailsLink();
                
                members.arrayAdd(renderer.render());
            }
        }
    }

    private void addActions(final ObjectAdapter objectAdapter,
			List<ObjectAction> actions, JsonRepresentation members) {
		for (ObjectAction action : actions) {
            Consent visibility = action.isVisible(getSession(), objectAdapter);
            if(!visibility.isAllowed()) {
                continue;
            } 
        	if(action.getType().isSet()) {
        		ObjectActionSet objectActionSet = (ObjectActionSet) action;
        		List<ObjectAction> subactions = objectActionSet.getActions();
        		addActions(objectAdapter, subactions, members);

        	} else {
        	    
                RendererFactory factory = RendererFactoryRegistry.instance.find(RepresentationType.OBJECT_ACTION);
                final ObjectActionReprRenderer renderer = 
                        (ObjectActionReprRenderer) factory.newRenderer(getResourceContext(), JsonRepresentation.newMap());
                
                renderer.with(new ObjectAndAction(objectAdapter, action))
                        .usingLinkToBuilder(linkToBuilder)
                        .withDetailsLink();

                members.arrayAdd(renderer.render());
        	}
        }
	}

    public JsonRepresentation render() {
        withLinks();
        withExtensions();
        return representation;
    }

    
    /////////////////////////////////////////////////////////////////////
    //
    /////////////////////////////////////////////////////////////////////

    public static Function<ObjectAdapter, JsonRepresentation> fromAdapter(final ResourceContext resourceContext) {
        final RendererFactory factory = RendererFactoryRegistry.instance.find(RepresentationType.DOMAIN_OBJECT);
        return new Function<ObjectAdapter, JsonRepresentation>() {
            @Override
            public JsonRepresentation apply(ObjectAdapter adapter) {
                DomainObjectReprRenderer renderer = 
                        (DomainObjectReprRenderer) factory.newRenderer(resourceContext, JsonRepresentation.newMap());
                return renderer.with(adapter).render();
            }
        };
    }

    public static Function<JsonRepresentation, JsonRepresentation> selfOf() {
        return new Function<JsonRepresentation, JsonRepresentation>() {
            @Override
            public JsonRepresentation apply(JsonRepresentation input) {
                return input.getRepresentation("self");
            }
        };
    }


    /////////////////////////////////////////////////////////////////////
    //
    /////////////////////////////////////////////////////////////////////

    public static Object valueOrRef(final ResourceContext resourceContext, final ObjectAdapter objectAdapter, ObjectSpecification objectSpec) {
		ValueFacet valueFacet = objectSpec.getFacet(ValueFacet.class);
		if(valueFacet != null) {
			EncodableFacet encodeableFacet = objectSpec.getFacet(EncodableFacet.class);
			return encodeableFacet.toEncodedString(objectAdapter);
		}
		TitleFacet titleFacet = objectSpec.getFacet(TitleFacet.class);
		String title = titleFacet.title(objectAdapter, resourceContext.getLocalization());
		return DomainObjectReprRenderer.newLinkToBuilder(resourceContext, "object", objectAdapter)
		            .withTitle(title).render();
	}

}