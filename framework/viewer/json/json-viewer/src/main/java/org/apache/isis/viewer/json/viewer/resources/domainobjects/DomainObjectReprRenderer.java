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
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.services.ServiceUtil;
import org.apache.isis.core.metamodel.spec.ObjectActionSet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionContainer.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociationFilters;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.json.applib.HttpMethod;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.Rel;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkBuilder;
import org.apache.isis.viewer.json.viewer.representations.LinkFollower;
import org.apache.isis.viewer.json.viewer.representations.RendererFactory;
import org.apache.isis.viewer.json.viewer.representations.RendererFactoryRegistry;
import org.apache.isis.viewer.json.viewer.representations.ReprRenderer;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererAbstract;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererFactoryAbstract;
import org.apache.isis.viewer.json.viewer.resources.domaintypes.DomainTypeReprRenderer;
import org.apache.isis.viewer.json.viewer.util.OidUtils;
import org.codehaus.jackson.node.NullNode;

public class DomainObjectReprRenderer extends ReprRendererAbstract<DomainObjectReprRenderer, ObjectAdapter>{

    public static class Factory extends ReprRendererFactoryAbstract {

        public Factory() {
            super(RepresentationType.DOMAIN_OBJECT);
        }

        @Override
        public ReprRenderer<?,?> newRenderer(ResourceContext resourceContext, LinkFollower linkFollower, JsonRepresentation representation) {
            return new DomainObjectReprRenderer(resourceContext, linkFollower, getRepresentationType(), representation);
        }
    }
    
    public static LinkBuilder newLinkToBuilder(ResourceContext resourceContext, Rel rel, ObjectAdapter elementAdapter) {
        String oidStr = resourceContext.getOidStringifier().enString(elementAdapter.getOid());
        String url = "objects/" + oidStr;
        return LinkBuilder.newBuilder(resourceContext, rel, RepresentationType.DOMAIN_OBJECT, url);
    }

    private static enum Mode {
        REGULAR(false, true),
        PERSIST_LINK_ARGUMENTS(true, true),
        MODIFY_PROPERTIES_LINK_ARGUMENTS(true, false);
        
        private final boolean cutDown;
        private final boolean describedBy;

        private Mode(boolean cutDown, boolean describedBy) {
            this.cutDown = cutDown;
            this.describedBy = describedBy;
        }

        public boolean isCutDown() {
            return cutDown;
        }

        public boolean includesDescribedBy() {
            return describedBy;
        }
    }
    

    private ObjectAdapterLinkTo linkToBuilder;
    private ObjectAdapter objectAdapter;
    private Mode mode = Mode.REGULAR;
    

    private DomainObjectReprRenderer(ResourceContext resourceContext, LinkFollower linkFollower, RepresentationType representationType, JsonRepresentation representation) {
        super(resourceContext, linkFollower, representationType, representation);
        usingLinkToBuilder(new DomainObjectLinkTo());
    }

    /**
     * Override the default {@link ObjectAdapterLinkTo} (that is used for generating links in
     * {@link #linkTo(ObjectAdapter)}).
     */
    public DomainObjectReprRenderer usingLinkToBuilder(ObjectAdapterLinkTo objectAdapterLinkToBuilder) {
        this.linkToBuilder = objectAdapterLinkToBuilder.usingResourceContext(resourceContext);
        return this;
    }

    public DomainObjectReprRenderer with(ObjectAdapter objectAdapter) {
        this.objectAdapter = objectAdapter;
        return this;
    }

    public JsonRepresentation render() {

        // self, oid
        if(!mode.isCutDown()) {
            if (objectAdapter.isPersistent()) {
                if(includesSelf) {
                    JsonRepresentation self = linkToBuilder.with(objectAdapter).builder(Rel.SELF).build();
                    getLinks().arrayAdd(self);
                }
                representation.mapPut("oid", getOidStr());
            }
        }


        // title
        if(!mode.isCutDown()) {
            String title = objectAdapter.titleString();
            representation.mapPut("title", title);
            
        }

        // serviceId
        if(!mode.isCutDown()) {
            final boolean isService = objectAdapter.getSpecification().isService();
            if(isService) {
                representation.mapPut("serviceId", ServiceUtil.id(objectAdapter.getObject()));
            }
        }

        
        // members
        withMembers(objectAdapter);

        // described by
        if(mode.includesDescribedBy()) {
            getLinks().arrayAdd(
                    DomainTypeReprRenderer.newLinkToBuilder(getResourceContext(), Rel.DESCRIBEDBY, objectAdapter.getSpecification()).build());
        }

        if(!mode.isCutDown()) {
            // update/persist
            addPersistLinkIfTransient();
            addUpdatePropertiesLinkIfPersistent();
            
            // extensions
            final boolean isService = objectAdapter.getSpecification().isService();
            getExtensions().mapPut("isService", isService);
            getExtensions().mapPut("isPersistent", objectAdapter.isPersistent());
        }
        
        return representation;
    }

    private String getOidStr() {
        return OidUtils.getOidStr(resourceContext, objectAdapter);
    }

    private DomainObjectReprRenderer withMembers(ObjectAdapter objectAdapter) {
        JsonRepresentation members = JsonRepresentation.newArray();
        List<ObjectAssociation> associations = objectAdapter.getSpecification().getAssociations();
        addAssociations(objectAdapter, members, associations);
        
        if(!mode.isCutDown()) {
            List<ObjectAction> actions = objectAdapter.getSpecification().getObjectActions(Contributed.INCLUDED);
            addActions(objectAdapter, actions, members);
        }
        representation.mapPut("members", members);
        return this;
    }

    private void addAssociations(ObjectAdapter objectAdapter, JsonRepresentation members, List<ObjectAssociation> associations) {
        final LinkFollower linkFollower = getLinkFollower().follow("members");
        for (final ObjectAssociation assoc : associations) {
            
            if(!mode.isCutDown()) {
                final Consent visibility = assoc.isVisible(getSession(), objectAdapter);
                if(!visibility.isAllowed()) {
                    continue;
                } 
            }
            if(assoc instanceof OneToOneAssociation) {
                OneToOneAssociation property = (OneToOneAssociation)assoc;
                
                RendererFactory factory = getRendererFactoryRegistry().find(RepresentationType.OBJECT_PROPERTY);
                final ObjectPropertyReprRenderer renderer = 
                        (ObjectPropertyReprRenderer) factory.newRenderer(getResourceContext(), linkFollower, JsonRepresentation.newMap());
                
                renderer.with(new ObjectAndProperty(objectAdapter, property))
                        .usingLinkTo(linkToBuilder);
                
                if(mode.isCutDown()) {
                    renderer.asArguments();
                }
                
                members.arrayAdd(renderer.render());
            }
            
            if(mode.isCutDown()) {
                // don't include collections
                continue; 
            }
            if(assoc instanceof OneToManyAssociation) {
                OneToManyAssociation collection = (OneToManyAssociation) assoc;

                RendererFactory factory = getRendererFactoryRegistry().find(RepresentationType.OBJECT_COLLECTION);
                final ObjectCollectionReprRenderer renderer = 
                        (ObjectCollectionReprRenderer) factory.newRenderer(getResourceContext(), linkFollower, JsonRepresentation.newMap());

                renderer.with(new ObjectAndCollection(objectAdapter, collection))
                    .usingLinkTo(linkToBuilder);
                
                members.arrayAdd(renderer.render());
            }
        }
    }

    private void addActions(final ObjectAdapter objectAdapter,
            List<ObjectAction> actions, JsonRepresentation members) {
        final LinkFollower linkFollower = getLinkFollower().follow("members");
        for (final ObjectAction action : actions) {
            final Consent visibility = action.isVisible(getSession(), objectAdapter);
            if(!visibility.isAllowed()) {
                continue;
            } 
            if(action.getType().isSet()) {
                ObjectActionSet objectActionSet = (ObjectActionSet) action;
                List<ObjectAction> subactions = objectActionSet.getActions();
                addActions(objectAdapter, subactions, members);

            } else {
                
                RendererFactory factory = getRendererFactoryRegistry().find(RepresentationType.OBJECT_ACTION);
                final ObjectActionReprRenderer renderer = 
                        (ObjectActionReprRenderer) factory.newRenderer(getResourceContext(), linkFollower, JsonRepresentation.newMap());
                
                renderer.with(new ObjectAndAction(objectAdapter, action))
                        .usingLinkTo(linkToBuilder);

                members.arrayAdd(renderer.render());
            }
        }
    }


    private void addPersistLinkIfTransient() {
        if (objectAdapter.isPersistent()) {
            return;
        }
        final RendererFactory rendererFactory = 
                getRendererFactoryRegistry().find(RepresentationType.DOMAIN_OBJECT);
        final DomainObjectReprRenderer renderer = 
                (DomainObjectReprRenderer) rendererFactory.newRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        final JsonRepresentation domainObjectRepr = renderer.with(objectAdapter).asPersistLinkArguments().render();
        
        final LinkBuilder persistLinkBuilder = LinkBuilder.newBuilder(getResourceContext(), Rel.PERSIST, RepresentationType.DOMAIN_OBJECT, "objects/")
                .withHttpMethod(HttpMethod.POST)
                .withArguments(domainObjectRepr);
        getLinks().arrayAdd(persistLinkBuilder.build());
    }

    private DomainObjectReprRenderer asPersistLinkArguments() {
        this.mode = Mode.PERSIST_LINK_ARGUMENTS;
        return this;
    }

    private DomainObjectReprRenderer asModifyPropertiesLinkArguments() {
        this.mode = Mode.MODIFY_PROPERTIES_LINK_ARGUMENTS;
        return this;
    }

    private void addUpdatePropertiesLinkIfPersistent() {
        if (!objectAdapter.isPersistent()) {
            return;
        }

        final RendererFactory rendererFactory = 
                getRendererFactoryRegistry().find(RepresentationType.DOMAIN_OBJECT);
        final DomainObjectReprRenderer renderer = 
                (DomainObjectReprRenderer) rendererFactory.newRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        final JsonRepresentation domainObjectRepr = renderer.with(objectAdapter).asModifyPropertiesLinkArguments().render();

        final LinkBuilder persistLinkBuilder = LinkBuilder.newBuilder(getResourceContext(), Rel.MODIFY, RepresentationType.DOMAIN_OBJECT, "objects/%s", getOidStr())
                .withHttpMethod(HttpMethod.PUT)
                .withArguments(domainObjectRepr);
        getLinks().arrayAdd(persistLinkBuilder.build());
    }

    protected RendererFactoryRegistry getRendererFactoryRegistry() {
        return RendererFactoryRegistry.instance;
    }


    /////////////////////////////////////////////////////////////////////
    //
    /////////////////////////////////////////////////////////////////////

    public static Object valueOrRef(final ResourceContext resourceContext, final ObjectAdapter objectAdapter, ObjectSpecification objectSpec) {
		ValueFacet valueFacet = objectSpec.getFacet(ValueFacet.class);
		if(valueFacet != null) {
		    return new JsonValueEncoder().asObject(objectAdapter);
		}
		TitleFacet titleFacet = objectSpec.getFacet(TitleFacet.class);
		String title = titleFacet.title(objectAdapter, resourceContext.getLocalization());
		return DomainObjectReprRenderer.newLinkToBuilder(resourceContext, Rel.OBJECT, objectAdapter)
		            .withTitle(title).build();
	}

}