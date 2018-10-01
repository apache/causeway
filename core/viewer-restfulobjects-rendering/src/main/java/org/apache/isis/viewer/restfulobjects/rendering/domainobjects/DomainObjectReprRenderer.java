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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facets.object.domainservicelayout.DomainServiceLayoutFacet;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.services.ServiceUtil;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulHttpMethod;
import org.apache.isis.viewer.restfulobjects.rendering.LinkBuilder;
import org.apache.isis.viewer.restfulobjects.rendering.LinkFollowSpecs;
import org.apache.isis.viewer.restfulobjects.rendering.RendererContext;
import org.apache.isis.viewer.restfulobjects.rendering.ReprRendererAbstract;
import org.apache.isis.viewer.restfulobjects.rendering.domaintypes.DomainTypeReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.util.OidUtils;

public class DomainObjectReprRenderer extends ReprRendererAbstract<DomainObjectReprRenderer, ObjectAdapter> {

    private static final String X_RO_DOMAIN_TYPE = "x-ro-domain-type";

    public static LinkBuilder newLinkToBuilder(final RendererContext rendererContext, final Rel rel, final ObjectAdapter objectAdapter) {
        String domainType = OidUtils.getDomainType(objectAdapter);
        String instanceId = OidUtils.getInstanceId(objectAdapter);
        final String url = "objects/" + domainType + "/" + instanceId;
        return LinkBuilder.newBuilder(rendererContext, rel.getName(), RepresentationType.DOMAIN_OBJECT, url).withTitle(objectAdapter.titleString(null));
    }

    public static LinkBuilder newLinkToObjectLayoutBuilder(final RendererContext rendererContext, final ObjectAdapter objectAdapter) {
        final Rel rel = Rel.OBJECT_LAYOUT;
        String domainType = OidUtils.getDomainType(objectAdapter);
        String instanceId = OidUtils.getInstanceId(objectAdapter);
        final String url = "objects/" + domainType + "/" + instanceId + "/object-layout";
        return LinkBuilder.newBuilder(rendererContext, rel.getName(), RepresentationType.OBJECT_LAYOUT, url);
    }

    public static LinkBuilder newLinkToObjectIconBuilder(final RendererContext rendererContext, final ObjectAdapter objectAdapter) {
        final Rel rel = Rel.OBJECT_ICON;
        String domainType = OidUtils.getDomainType(objectAdapter);
        String instanceId = OidUtils.getInstanceId(objectAdapter);
        final String url = "objects/" + domainType + "/" + instanceId + "/image";
        return LinkBuilder.newBuilder(rendererContext, rel.getName(), RepresentationType.OBJECT_IMAGE, url);
    }

    private static enum Mode {
        REGULAR,
        PERSIST_LINK_ARGUMENTS,
        UPDATE_PROPERTIES_LINK_ARGUMENTS,
        EVENT_SERIALIZATION;

        public boolean isRegular() {
            return this == REGULAR;
        }

        public boolean isPersistLinkArgs() {
            return this == PERSIST_LINK_ARGUMENTS;
        }

        public boolean isUpdatePropertiesLinkArgs() {
            return this == UPDATE_PROPERTIES_LINK_ARGUMENTS;
        }

        public boolean isEventSerialization() {
            return this == EVENT_SERIALIZATION;
        }

        public boolean includeDescribedBy() {
            return isRegular() || isPersistLinkArgs();
        }

        public boolean includeUp() {
            return isRegular();
        }

        public boolean checkVisibility() {
            return isRegular() || isUpdatePropertiesLinkArgs();
        }

        public boolean isArgs() {
            return isPersistLinkArgs() || isUpdatePropertiesLinkArgs();
        }
    }

    private ObjectAdapterLinkTo linkToBuilder;
    private ObjectAdapter objectAdapter;
    private Mode mode = Mode.REGULAR;

    public DomainObjectReprRenderer(
            final RendererContext resourceContext,
            final LinkFollowSpecs linkFollower,
            final JsonRepresentation representation) {
        super(resourceContext, linkFollower, RepresentationType.DOMAIN_OBJECT, representation);
        usingLinkToBuilder(new DomainObjectLinkTo());
    }

    /**
     * Override the default {@link ObjectAdapterLinkTo} (that is used for
     * generating links.
     */
    public DomainObjectReprRenderer usingLinkToBuilder(final ObjectAdapterLinkTo objectAdapterLinkToBuilder) {
        this.linkToBuilder = objectAdapterLinkToBuilder.usingUrlBase(rendererContext);
        return this;
    }

    @Override
    public DomainObjectReprRenderer with(final ObjectAdapter objectAdapter) {
        this.objectAdapter = objectAdapter;
        String domainTypeHref = DomainTypeReprRenderer.newLinkToBuilder(getRendererContext(), Rel.DOMAIN_TYPE, objectAdapter.getSpecification()).build().getString("href");
        addMediaTypeParams(X_RO_DOMAIN_TYPE, domainTypeHref);
        return this;
    }

    @Override
    public JsonRepresentation render() {

        if(representation == null) {
            return null;
        }

        final boolean isService = objectAdapter.getSpecification().isService();

        if (!(mode.isArgs())) {

            // self, extensions.oid
            if (objectAdapter.representsPersistent()) {
                if (includesSelf) {
                    addLinkToSelf();
                }
                getExtensions().mapPut("oid", getOidStr());
            }

            // title
            final String title = objectAdapter.titleString(null);
            representation.mapPut("title", title);

            // serviceId or instance Id
            if (isService) {
                representation.mapPut("serviceId", ServiceUtil.idOfAdapter(objectAdapter));
            } else {
                final String domainType = getDomainType();
                final String instanceId = getInstanceId();
                if (domainType != null) {
                    representation.mapPut("domainType", domainType);
                    representation.mapPut("instanceId", instanceId);

                }
            }
        }

        // members
        if(!mode.isUpdatePropertiesLinkArgs()) {
            withMembers(objectAdapter);
        }

        // described by
        if (mode.includeDescribedBy() && !rendererContext.suppressDescribedByLinks()) {
            addLinkToDescribedBy();
            addLinkToObjectLayout();
            addLinkToObjectIcon();
        }
        if(isService && mode.includeUp()) {
            addLinkToUp();
        }

        if (!mode.isArgs() && !rendererContext.objectPropertyValuesOnly()) {
            // update/persist
            addPersistLinkIfTransientAndPersistable();
            addUpdatePropertiesLinkIfRequired();

            // extensions
            getExtensions().mapPut("isService", isService);
            getExtensions().mapPut("isPersistent", objectAdapter.representsPersistent());
            if(isService) {
                final ObjectSpecification objectSpec = objectAdapter.getSpecification();
                final DomainServiceLayoutFacet layoutFacet =
                        objectSpec.getFacet(DomainServiceLayoutFacet.class);
                if(layoutFacet != null) {
                    final DomainServiceLayout.MenuBar menuBar = layoutFacet.getMenuBar();
                    if(menuBar != null) {
                        getExtensions().mapPut("menuBar", menuBar);
                    }
                }
            }
        }

        return representation;
    }

    private void addLinkToSelf() {
        final JsonRepresentation link = linkToBuilder.with(objectAdapter).builder(Rel.SELF).build();

        final LinkFollowSpecs linkFollower = getLinkFollowSpecs().follow("links");
        if (linkFollower.matches(link)) {
            final DomainObjectReprRenderer renderer =
                    new DomainObjectReprRenderer(getRendererContext(), linkFollower, JsonRepresentation.newMap());
            renderer.with(objectAdapter);
            link.mapPut("value", renderer.render());
        }

        getLinks().arrayAdd(link);
    }

    private void addLinkToDescribedBy() {
        final JsonRepresentation link = DomainTypeReprRenderer.newLinkToBuilder(getRendererContext(), Rel.DESCRIBEDBY, objectAdapter.getSpecification()).build();

        final LinkFollowSpecs linkFollower = getLinkFollowSpecs().follow("links");
        if (linkFollower.matches(link)) {
            final DomainTypeReprRenderer renderer = new DomainTypeReprRenderer(getRendererContext(), linkFollower, JsonRepresentation.newMap());
            renderer.with(objectAdapter.getSpecification());
            link.mapPut("value", renderer.render());
        }
        getLinks().arrayAdd(link);
    }

    private void addLinkToObjectLayout() {
        final LinkBuilder linkBuilder = DomainObjectReprRenderer
                .newLinkToObjectLayoutBuilder(getRendererContext(), objectAdapter);
        final JsonRepresentation link = linkBuilder.build();
        getLinks().arrayAdd(link);
    }

    private void addLinkToObjectIcon() {
        final LinkBuilder linkBuilder = DomainObjectReprRenderer
                .newLinkToObjectIconBuilder(getRendererContext(), objectAdapter);
        final JsonRepresentation link = linkBuilder.build();
        getLinks().arrayAdd(link);
    }

    private void addLinkToUp() {
        final JsonRepresentation link = LinkBuilder.newBuilder(rendererContext, Rel.UP.getName(), RepresentationType.LIST, "services").build();
        getLinks().arrayAdd(link);
    }

    private String getDomainType() {
        return org.apache.isis.viewer.restfulobjects.rendering.util.OidUtils.getDomainType(objectAdapter);
    }

    private String getInstanceId() {
        return org.apache.isis.viewer.restfulobjects.rendering.util.OidUtils.getInstanceId(objectAdapter);
    }

    private String getOidStr() {
        return org.apache.isis.viewer.restfulobjects.rendering.util.OidUtils.getOidStr(objectAdapter);
    }

    private DomainObjectReprRenderer withMembers(final ObjectAdapter objectAdapter) {
        final JsonRepresentation appendTo =
                mode.isUpdatePropertiesLinkArgs() ? representation : JsonRepresentation.newMap();
        final List<ObjectAssociation> associations = objectAdapter.getSpecification()
                .streamAssociations(Contributed.INCLUDED)
                .collect(Collectors.toList());

        addProperties(objectAdapter, appendTo, associations);

        if(!rendererContext.objectPropertyValuesOnly()) {
            if (!mode.isArgs() ) {
                addCollections(objectAdapter, appendTo, associations);
            }

            if (mode.isRegular()) {
                final Stream<ObjectAction> actions = objectAdapter.getSpecification()
                        .streamObjectActions(Contributed.INCLUDED);
                
                addActions(objectAdapter, actions, appendTo);
            }
        }
        if(!mode.isUpdatePropertiesLinkArgs()) {
            representation.mapPut("members", appendTo);
        }
        return this;
    }

    private void addProperties(final ObjectAdapter objectAdapter, final JsonRepresentation members, final List<ObjectAssociation> associations) {
        for (final ObjectAssociation assoc : associations) {

            if (mode.checkVisibility()) {
                final Consent visibility = assoc.isVisible(objectAdapter, getInteractionInitiatedBy(), rendererContext.getWhere());
                if (!visibility.isAllowed()) {
                    continue;
                }
            }
            if (!(assoc instanceof OneToOneAssociation)) {
                continue;
            }

            final OneToOneAssociation property = (OneToOneAssociation) assoc;
            final LinkFollowSpecs linkFollowerForProp = getLinkFollowSpecs().follow("members[" + property.getId() + "]");
            final JsonRepresentation propertyRepresentation = JsonRepresentation.newMap();
            final ObjectPropertyReprRenderer renderer =
                    new ObjectPropertyReprRenderer(getRendererContext(), linkFollowerForProp, property.getId(), propertyRepresentation);
            renderer.with(new ObjectAndProperty(objectAdapter, property)).usingLinkTo(linkToBuilder);

            if (mode.isArgs()) {
                renderer.asArguments();
            }
            if(mode.isEventSerialization()) {
                renderer.asEventSerialization();
            }

            final JsonRepresentation propertyValueRepresentation = renderer.render();
            final JsonRepresentation propertyRepr = rendererContext.objectPropertyValuesOnly()
                    ? propertyValueRepresentation.getRepresentation("value")
                            : propertyValueRepresentation;
                    members.mapPut(assoc.getId(), propertyRepr);
        }
    }

    private void addCollections(final ObjectAdapter objectAdapter, final JsonRepresentation members, final List<ObjectAssociation> associations) {
        for (final ObjectAssociation assoc : associations) {

            if (mode.checkVisibility()) {
                final Consent visibility = assoc.isVisible(objectAdapter, getInteractionInitiatedBy(), rendererContext.getWhere());
                if (!visibility.isAllowed()) {
                    continue;
                }
            }

            if (!(assoc instanceof OneToManyAssociation)) {
                continue;
            }

            final OneToManyAssociation collection = (OneToManyAssociation) assoc;

            final LinkFollowSpecs linkFollowerForColl = getLinkFollowSpecs().follow(
                    "members[" + collection.getId() + "]");
            final JsonRepresentation collectionRepresentation = JsonRepresentation.newMap();
            final ObjectCollectionReprRenderer renderer =
                    new ObjectCollectionReprRenderer(getRendererContext(), linkFollowerForColl, collection.getId(), collectionRepresentation);

            renderer.with(new ObjectAndCollection(objectAdapter, collection)).usingLinkTo(linkToBuilder);
            if(mode.isEventSerialization()) {
                renderer.asEventSerialization();
            }

            members.mapPut(assoc.getId(), renderer.render());
        }
    }

    private void addActions(
            final ObjectAdapter objectAdapter, 
            final Stream<ObjectAction> actions, 
            final JsonRepresentation members) {
        
        actions
        .filter(action->{
            final Consent visibility = action.isVisible(objectAdapter, getInteractionInitiatedBy(), rendererContext.getWhere());
            return visibility.isAllowed();
        })
        .forEach(action->{
            final LinkFollowSpecs linkFollowSpecs = getLinkFollowSpecs().follow("members["+action.getId()+"]");
            final ObjectActionReprRenderer renderer = 
                    new ObjectActionReprRenderer(getRendererContext(), linkFollowSpecs, action.getId(), 
                            JsonRepresentation.newMap());

            renderer.with(new ObjectAndAction(objectAdapter, action)).usingLinkTo(linkToBuilder);
            members.mapPut(action.getId(), renderer.render());
        });

    }

    private void addPersistLinkIfTransientAndPersistable() {
        if (objectAdapter.representsPersistent()) {
            return;
        }
        final DomainObjectReprRenderer renderer =
                new DomainObjectReprRenderer(getRendererContext(), null, JsonRepresentation.newMap());
        final JsonRepresentation domainObjectRepr = renderer.with(objectAdapter).asPersistLinkArguments().render();

        final String domainType = objectAdapter.getSpecification().getSpecId().asString();
        final LinkBuilder persistLinkBuilder = LinkBuilder.newBuilder(getRendererContext(), Rel.PERSIST.getName(), RepresentationType.DOMAIN_OBJECT, "objects/%s", domainType).withHttpMethod(RestfulHttpMethod.POST).withArguments(domainObjectRepr);
        getLinks().arrayAdd(persistLinkBuilder.build());
    }

    private DomainObjectReprRenderer asPersistLinkArguments() {
        this.mode = Mode.PERSIST_LINK_ARGUMENTS;
        return this;
    }

    private DomainObjectReprRenderer asUpdatePropertiesLinkArguments() {
        this.mode = Mode.UPDATE_PROPERTIES_LINK_ARGUMENTS;
        return this;
    }

    // not part of the spec
    public DomainObjectReprRenderer asEventSerialization() {
        this.mode = Mode.EVENT_SERIALIZATION;
        return this;
    }


    private void addUpdatePropertiesLinkIfRequired() {
        if(mode.isEventSerialization()) {
            return;
        }
        if (!objectAdapter.representsPersistent()) {
            return;
        }
        final boolean isService = objectAdapter.getSpecification().isService();
        if(isService) {
            return;
        }

        final DomainObjectReprRenderer renderer =
                new DomainObjectReprRenderer(getRendererContext(), null, JsonRepresentation.newMap());
        final JsonRepresentation domainObjectRepr = renderer.with(objectAdapter).asUpdatePropertiesLinkArguments().render();

        if(!getRendererContext().suppressUpdateLink()) {
            final LinkBuilder updateLinkBuilder = LinkBuilder.newBuilder(getRendererContext(), Rel.UPDATE.getName(), RepresentationType.DOMAIN_OBJECT, "objects/%s/%s", getDomainType(), getInstanceId()).withHttpMethod(RestfulHttpMethod.PUT).withArguments(domainObjectRepr);
            getLinks().arrayAdd(updateLinkBuilder.build());
        }
    }

    // ///////////////////////////////////////////////////////////////////
    //
    // ///////////////////////////////////////////////////////////////////

    public static Object valueOrRef(final RendererContext resourceContext, final ObjectAdapter objectAdapter, final ObjectSpecification objectSpec) {
        if(objectAdapter.isValue()) {
            String format = null; // TODO
            return JsonValueEncoder.asObject(objectAdapter, format);
        }
        final TitleFacet titleFacet = objectSpec.getFacet(TitleFacet.class);
        final String title = titleFacet.title(objectAdapter);
        return DomainObjectReprRenderer.newLinkToBuilder(resourceContext, Rel.VALUE, objectAdapter).withTitle(title).build();
    }



}