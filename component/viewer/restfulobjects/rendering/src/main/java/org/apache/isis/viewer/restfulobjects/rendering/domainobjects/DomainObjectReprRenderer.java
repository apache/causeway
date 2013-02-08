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
package org.apache.isis.viewer.restfulobjects.rendering.domainobjects;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facets.object.notpersistable.NotPersistableFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.services.ServiceUtil;
import org.apache.isis.core.metamodel.spec.ObjectActionSet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionContainer.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulHttpMethod;
import org.apache.isis.viewer.restfulobjects.rendering.LinkBuilder;
import org.apache.isis.viewer.restfulobjects.rendering.LinkFollower;
import org.apache.isis.viewer.restfulobjects.rendering.RendererContext;
import org.apache.isis.viewer.restfulobjects.rendering.RendererFactory;
import org.apache.isis.viewer.restfulobjects.rendering.RendererFactoryRegistry;
import org.apache.isis.viewer.restfulobjects.rendering.ReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.ReprRendererAbstract;
import org.apache.isis.viewer.restfulobjects.rendering.ReprRendererFactoryAbstract;
import org.apache.isis.viewer.restfulobjects.rendering.domaintypes.DomainTypeReprRenderer;

public class DomainObjectReprRenderer extends ReprRendererAbstract<DomainObjectReprRenderer, ObjectAdapter> {

    public static class Factory extends ReprRendererFactoryAbstract {

        public Factory() {
            super(RepresentationType.DOMAIN_OBJECT);
        }

        @Override
        public ReprRenderer<?, ?> newRenderer(final RendererContext resourceContext, final LinkFollower linkFollower, final JsonRepresentation representation) {
            return new DomainObjectReprRenderer(resourceContext, linkFollower, getRepresentationType(), representation);
        }
    }

    public static LinkBuilder newLinkToBuilder(final RendererContext resourceContext, final Rel rel, final ObjectAdapter elementAdapter) {
        final String oidStr = ((RootOid) elementAdapter.getOid()).enString(getOidMarshaller());
        final String url = "objects/" + oidStr;
        final LinkBuilder builder = LinkBuilder.newBuilder(resourceContext, rel, RepresentationType.DOMAIN_OBJECT, url).withTitle(elementAdapter.titleString());
        return builder;
    }

    private static enum Mode {
        REGULAR(false, true, true), 
        PERSIST_LINK_ARGUMENTS(true, true, false), 
        MODIFY_PROPERTIES_LINK_ARGUMENTS(true, false, false),
        EVENT_SERIALIZATION(false, false, true);

        private final boolean representsArguments;
        private final boolean describedBy;
        private boolean checkVisibility;

        private Mode(final boolean representsArguments, final boolean describedBy, final boolean visibilityCheck) {
            this.representsArguments = representsArguments;
            this.describedBy = describedBy;
            this.checkVisibility = visibilityCheck;
        }

        public boolean representsArguments() {
            return representsArguments;
        }

        public boolean includesDescribedBy() {
            return describedBy;
        }
        
        public boolean checkVisibility() {
            return checkVisibility;
        }
    }

    private ObjectAdapterLinkTo linkToBuilder;
    private ObjectAdapter objectAdapter;
    private Mode mode = Mode.REGULAR;

    private DomainObjectReprRenderer(final RendererContext resourceContext, final LinkFollower linkFollower, final RepresentationType representationType, final JsonRepresentation representation) {
        super(resourceContext, linkFollower, representationType, representation);
        usingLinkToBuilder(new DomainObjectLinkTo());
    }

    /**
     * Override the default {@link ObjectAdapterLinkTo} (that is used for
     * generating links in {@link #linkTo(ObjectAdapter)}).
     */
    public DomainObjectReprRenderer usingLinkToBuilder(final ObjectAdapterLinkTo objectAdapterLinkToBuilder) {
        this.linkToBuilder = objectAdapterLinkToBuilder.usingUrlBase(resourceContext);
        return this;
    }

    @Override
    public DomainObjectReprRenderer with(final ObjectAdapter objectAdapter) {
        this.objectAdapter = objectAdapter;
        return this;
    }

    @Override
    public JsonRepresentation render() {

        // self, oid
        if (!mode.representsArguments()) {
            if (objectAdapter.representsPersistent()) {
                if (includesSelf) {
                    final JsonRepresentation self = linkToBuilder.with(objectAdapter).builder(Rel.SELF).build();
                    getLinks().arrayAdd(self);
                }
                representation.mapPut("oid", getOidStr());
            }
        }

        // title
        if (!mode.representsArguments()) {
            final String title = objectAdapter.titleString();
            representation.mapPut("title", title);

        }

        // serviceId
        if (!mode.representsArguments()) {
            final boolean isService = objectAdapter.getSpecification().isService();
            if (isService) {
                representation.mapPut("serviceId", ServiceUtil.id(objectAdapter.getObject()));
            }
        }

        // members
        withMembers(objectAdapter);

        // described by
        if (mode.includesDescribedBy()) {
            getLinks().arrayAdd(DomainTypeReprRenderer.newLinkToBuilder(getResourceContext(), Rel.DESCRIBEDBY, objectAdapter.getSpecification()).build());
        }

        if (!mode.representsArguments()) {
            // update/persist
            addPersistLinkIfTransientAndPersistable();
            addUpdatePropertiesLinkIfPersistent();

            // extensions
            final boolean isService = objectAdapter.getSpecification().isService();
            getExtensions().mapPut("isService", isService);
            getExtensions().mapPut("isPersistent", objectAdapter.representsPersistent());
        }

        return representation;
    }

    private String getOidStr() {
        return org.apache.isis.viewer.restfulobjects.rendering.util.OidUtils.getOidStr(resourceContext, objectAdapter);
    }

    private DomainObjectReprRenderer withMembers(final ObjectAdapter objectAdapter) {
        final JsonRepresentation members = JsonRepresentation.newArray();
        final List<ObjectAssociation> associations = objectAdapter.getSpecification().getAssociations();
        addAssociations(objectAdapter, members, associations);

        if (!mode.representsArguments()) {
            final List<ObjectAction> actions = objectAdapter.getSpecification().getObjectActions(Contributed.INCLUDED);
            addActions(objectAdapter, actions, members);
        }
        representation.mapPut("members", members);
        return this;
    }

    private void addAssociations(final ObjectAdapter objectAdapter, final JsonRepresentation members, final List<ObjectAssociation> associations) {
        final LinkFollower linkFollower = getLinkFollower().follow("members");
        for (final ObjectAssociation assoc : associations) {

            if (mode.checkVisibility()) {
                final Consent visibility = assoc.isVisible(getAuthenticationSession(), objectAdapter, resourceContext.getWhere());
                if (!visibility.isAllowed()) {
                    continue;
                }
            }
            if (assoc instanceof OneToOneAssociation) {
                final OneToOneAssociation property = (OneToOneAssociation) assoc;

                final RendererFactory factory = getRendererFactoryRegistry().find(RepresentationType.OBJECT_PROPERTY);
                final ObjectPropertyReprRenderer renderer = (ObjectPropertyReprRenderer) factory.newRenderer(getResourceContext(), linkFollower, JsonRepresentation.newMap());

                renderer.with(new ObjectAndProperty(objectAdapter, property)).usingLinkTo(linkToBuilder);

                if (mode.representsArguments()) {
                    renderer.asArguments();
                }

                members.arrayAdd(renderer.render());
            }

            if (mode.representsArguments()) {
                // don't include collections
                continue;
            }
            if (assoc instanceof OneToManyAssociation) {
                final OneToManyAssociation collection = (OneToManyAssociation) assoc;

                final RendererFactory factory = getRendererFactoryRegistry().find(RepresentationType.OBJECT_COLLECTION);
                final ObjectCollectionReprRenderer renderer = (ObjectCollectionReprRenderer) factory.newRenderer(getResourceContext(), linkFollower, JsonRepresentation.newMap());

                renderer.with(new ObjectAndCollection(objectAdapter, collection)).usingLinkTo(linkToBuilder);

                members.arrayAdd(renderer.render());
            }
        }
    }

    private void addActions(final ObjectAdapter objectAdapter, final List<ObjectAction> actions, final JsonRepresentation members) {
        final LinkFollower linkFollower = getLinkFollower().follow("members");
        for (final ObjectAction action : actions) {
            final Consent visibility = action.isVisible(getAuthenticationSession(), objectAdapter, resourceContext.getWhere());
            if (!visibility.isAllowed()) {
                continue;
            }
            if (action.getType().isSet()) {
                final ObjectActionSet objectActionSet = (ObjectActionSet) action;
                final List<ObjectAction> subactions = objectActionSet.getActions();
                addActions(objectAdapter, subactions, members);

            } else {

                final RendererFactory factory = getRendererFactoryRegistry().find(RepresentationType.OBJECT_ACTION);
                final ObjectActionReprRenderer renderer = (ObjectActionReprRenderer) factory.newRenderer(getResourceContext(), linkFollower, JsonRepresentation.newMap());

                renderer.with(new ObjectAndAction(objectAdapter, action)).usingLinkTo(linkToBuilder);

                members.arrayAdd(renderer.render());
            }
        }
    }

    private void addPersistLinkIfTransientAndPersistable() {
        if (objectAdapter.representsPersistent()) {
            return;
        }
        if(objectAdapter.getSpecification().containsDoOpFacet(NotPersistableFacet.class)) {
            return;
        }
        final RendererFactory rendererFactory = getRendererFactoryRegistry().find(RepresentationType.DOMAIN_OBJECT);
        final DomainObjectReprRenderer renderer = (DomainObjectReprRenderer) rendererFactory.newRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        final JsonRepresentation domainObjectRepr = renderer.with(objectAdapter).asPersistLinkArguments().render();

        final LinkBuilder persistLinkBuilder = LinkBuilder.newBuilder(getResourceContext(), Rel.PERSIST, RepresentationType.DOMAIN_OBJECT, "objects/").withHttpMethod(RestfulHttpMethod.POST).withArguments(domainObjectRepr);
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

    // not part of the spec
    public DomainObjectReprRenderer asEventSerialization() {
        this.mode = Mode.EVENT_SERIALIZATION;
        return this;
    }


    private void addUpdatePropertiesLinkIfPersistent() {
        if (!objectAdapter.representsPersistent()) {
            return;
        }

        final RendererFactory rendererFactory = getRendererFactoryRegistry().find(RepresentationType.DOMAIN_OBJECT);
        final DomainObjectReprRenderer renderer = (DomainObjectReprRenderer) rendererFactory.newRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        final JsonRepresentation domainObjectRepr = renderer.with(objectAdapter).asModifyPropertiesLinkArguments().render();

        final LinkBuilder persistLinkBuilder = LinkBuilder.newBuilder(getResourceContext(), Rel.MODIFY, RepresentationType.DOMAIN_OBJECT, "objects/%s", getOidStr()).withHttpMethod(RestfulHttpMethod.PUT).withArguments(domainObjectRepr);
        getLinks().arrayAdd(persistLinkBuilder.build());
    }

    protected RendererFactoryRegistry getRendererFactoryRegistry() {
        return RendererFactoryRegistry.instance;
    }

    // ///////////////////////////////////////////////////////////////////
    //
    // ///////////////////////////////////////////////////////////////////

    public static Object valueOrRef(final RendererContext resourceContext, final ObjectAdapter objectAdapter, final ObjectSpecification objectSpec) {
        final ValueFacet valueFacet = objectSpec.getFacet(ValueFacet.class);
        if (valueFacet != null) {
            return new JsonValueEncoder().asObject(objectAdapter);
        }
        final TitleFacet titleFacet = objectSpec.getFacet(TitleFacet.class);
        final String title = titleFacet.title(objectAdapter, resourceContext.getLocalization());
        return DomainObjectReprRenderer.newLinkToBuilder(resourceContext, Rel.OBJECT, objectAdapter).withTitle(title).build();
    }

 
    
    // ///////////////////////////////////////////////////////////////////
    // dependencies (from context)
    // ///////////////////////////////////////////////////////////////////

    protected static OidMarshaller getOidMarshaller() {
		return IsisContext.getOidMarshaller();
	}

}