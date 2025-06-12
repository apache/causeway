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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.Rel;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulHttpMethod;
import org.apache.causeway.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkBuilder;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkFollowSpecs;
import org.apache.causeway.viewer.restfulobjects.rendering.ReprRendererAbstract;
import org.apache.causeway.viewer.restfulobjects.rendering.domaintypes.DomainTypeReprRenderer;
import org.apache.causeway.viewer.restfulobjects.rendering.service.valuerender.JsonValueConverter;
import org.apache.causeway.viewer.restfulobjects.rendering.service.valuerender.JsonValueEncoderService;

public class DomainObjectReprRenderer
extends ReprRendererAbstract<ManagedObject> {

    private static final String X_RO_DOMAIN_TYPE = "x-ro-domain-type";

    public static LinkBuilder newLinkToBuilder(
            final IResourceContext resourceContext,
            final Rel rel,
            final ManagedObject objectAdapter) {

        final String objectRef = ManagedObjects.stringifyElseFail(objectAdapter, "/");
        final String url = "objects/" + objectRef;
        return LinkBuilder.newBuilder(resourceContext, rel.getName(), RepresentationType.DOMAIN_OBJECT, url).withTitle(objectAdapter.getTitle());
    }

    public static LinkBuilder newLinkToObjectLayoutBuilder(
            final IResourceContext resourceContext,
            final ManagedObject objectAdapter) {

        final Rel rel = Rel.OBJECT_LAYOUT;
        final String objectRef = ManagedObjects.stringifyElseFail(objectAdapter, "/");
        final String url = "objects/" + objectRef + "/object-layout";
        return LinkBuilder.newBuilder(resourceContext, rel.getName(), RepresentationType.OBJECT_LAYOUT, url);
    }

    public static LinkBuilder newLinkToObjectIconBuilder(
            final IResourceContext resourceContext,
            final ManagedObject objectAdapter) {

        final Rel rel = Rel.OBJECT_ICON;
        final String objectRef = ManagedObjects.stringifyElseFail(objectAdapter, "/");
        final String url = "objects/" + objectRef + "/object-icon";
        return LinkBuilder.newBuilder(resourceContext, rel.getName(), RepresentationType.IMAGE, url);
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
    private ManagedObject objectAdapter;
    private Mode mode = Mode.REGULAR;

    public DomainObjectReprRenderer(
            final IResourceContext resourceContext,
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
        this.linkToBuilder = objectAdapterLinkToBuilder.usingUrlBase(resourceContext);
        return this;
    }

    @Override
    public DomainObjectReprRenderer with(final ManagedObject objectAdapter) {
        this.objectAdapter = objectAdapter;
        String domainTypeHref = DomainTypeReprRenderer
                .newLinkToBuilder(
                        getResourceContext(),
                        Rel.DOMAIN_TYPE,
                        objectAdapter.objSpec())
                .build()
                .getString("href");
        addMediaTypeParams(X_RO_DOMAIN_TYPE, domainTypeHref);
        return this;
    }

    @Override
    public JsonRepresentation render() {

        if(representation == null) {
            return null;
        }

        final boolean isService = objectAdapter.objSpec().isInjectable();

        if (!(mode.isArgs())) {

            var oidIfAny = objectAdapter.getBookmark();

            // self, extensions.oid
            if (ManagedObjects.isIdentifiable(objectAdapter)) {
                if (includesSelf) {
                    addLinkToSelf();
                }
                oidIfAny.ifPresent(oid->{
                    var oidStr = oid.stringify();
                    getExtensions().mapPutString("oid", oidStr);
                });
            }

            // title
            final String title = objectAdapter.getTitle();
            representation.mapPutString("title", title);

            // serviceId or instance Id
            if (isService) {
                representation.mapPutString("serviceId", objectAdapter.objSpec().logicalTypeName());
            } else {
                oidIfAny.ifPresent(oid->{
                    Optional.ofNullable(oid.logicalTypeName())
                    .ifPresent(domainType->
                        representation.mapPutString("domainType", domainType));
                    representation.mapPutString("instanceId", oid.identifier());
                });
            }
        }

        // members
        if(!mode.isUpdatePropertiesLinkArgs()) {
            withMembers(objectAdapter);
        }

        // described by
        if (mode.includeDescribedBy() && !resourceContext.config().isSuppressDescribedByLinks()) {
            addLinkToDescribedBy();
            addLinkToObjectLayout();
            addLinkToObjectIcon();
        }
        if(isService && mode.includeUp()) {
            addLinkToUp();
        }

        if (!mode.isArgs() && !resourceContext.config().isObjectPropertyValuesOnly()) {
            // update/persist
            addPersistLinkIfTransientAndPersistable();
            addUpdatePropertiesLinkIfRequired();

            // extensions
            getExtensions().mapPutBoolean("isService", isService);
            getExtensions().mapPutBoolean("isPersistent", ManagedObjects.isIdentifiable(objectAdapter));
            if(isService) {

                Facets.domainServiceLayoutMenuBar(objectAdapter.objSpec())
                .ifPresent(menuBar->
                        getExtensions().mapPut("menuBar", menuBar));

            }
        }

        return representation;
    }

    private void addLinkToSelf() {
        final JsonRepresentation link = linkToBuilder.with(objectAdapter).builder(Rel.SELF).build();

        final LinkFollowSpecs linkFollower = getLinkFollowSpecs().follow("links");
        if (linkFollower.matches(link)) {
            var renderer =
                    new DomainObjectReprRenderer(getResourceContext(), linkFollower, JsonRepresentation.newMap())
                    .with(objectAdapter);
            link.mapPutJsonRepresentation("value", renderer.render());
        }

        getLinks().arrayAdd(link);
    }

    private void addLinkToDescribedBy() {
        final JsonRepresentation link = DomainTypeReprRenderer.newLinkToBuilder(getResourceContext(), Rel.DESCRIBEDBY, objectAdapter.objSpec()).build();

        final LinkFollowSpecs linkFollower = getLinkFollowSpecs().follow("links");
        if (linkFollower.matches(link)) {
            final DomainTypeReprRenderer renderer = new DomainTypeReprRenderer(getResourceContext(), linkFollower, JsonRepresentation.newMap());
            renderer.with(objectAdapter.objSpec());
            link.mapPutJsonRepresentation("value", renderer.render());
        }
        getLinks().arrayAdd(link);
    }

    private void addLinkToObjectLayout() {
        final LinkBuilder linkBuilder = DomainObjectReprRenderer
                .newLinkToObjectLayoutBuilder(getResourceContext(), objectAdapter);
        final JsonRepresentation link = linkBuilder.build();
        getLinks().arrayAdd(link);
    }

    private void addLinkToObjectIcon() {
        final LinkBuilder linkBuilder = DomainObjectReprRenderer
                .newLinkToObjectIconBuilder(getResourceContext(), objectAdapter);
        final JsonRepresentation link = linkBuilder.build();
        getLinks().arrayAdd(link);
    }

    private void addLinkToUp() {
        final JsonRepresentation link = LinkBuilder.newBuilder(resourceContext, Rel.UP.getName(), RepresentationType.LIST, "services").build();
        getLinks().arrayAdd(link);
    }

    private DomainObjectReprRenderer withMembers(final ManagedObject objectAdapter) {
        final JsonRepresentation appendTo =
                mode.isUpdatePropertiesLinkArgs() ? representation : JsonRepresentation.newMap();
        final List<ObjectAssociation> associations = objectAdapter.objSpec()
                .streamAssociations(MixedIn.INCLUDED)
                .collect(Collectors.toList());

        addProperties(objectAdapter, appendTo, associations);

        if(!resourceContext.config().isObjectPropertyValuesOnly()) {
            if (!mode.isArgs() ) {
                addCollections(objectAdapter, appendTo, associations);
            }

            if (mode.isRegular()) {
                final Stream<ObjectAction> actions = objectAdapter.objSpec()
                        .streamAnyActions(MixedIn.INCLUDED);

                addActions(objectAdapter, actions, appendTo);
            }
        }
        if(!mode.isUpdatePropertiesLinkArgs()) {
            representation.mapPutJsonRepresentation("members", appendTo);
        }
        return this;
    }

    private void addProperties(final ManagedObject objectAdapter, final JsonRepresentation members, final List<ObjectAssociation> associations) {
        for (final ObjectAssociation assoc : associations) {

            if (mode.checkVisibility()) {
                final Consent visibility = assoc.isVisible(objectAdapter, getInteractionInitiatedBy(), resourceContext.where());
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
                    new ObjectPropertyReprRenderer(getResourceContext(), linkFollowerForProp, property.getId(), propertyRepresentation);
            renderer.with(ManagedProperty.of(objectAdapter, property, resourceContext.where())).usingLinkTo(linkToBuilder);

            if (mode.isArgs()) {
                renderer.asArguments();
            }
            if(mode.isEventSerialization()) {
                renderer.asEventSerialization();
            }

            final JsonRepresentation propertyValueRepresentation = renderer.render();
            final JsonRepresentation propertyRepr = resourceContext.config().isObjectPropertyValuesOnly()
                    ? propertyValueRepresentation.getRepresentation("value")
                            : propertyValueRepresentation;
                    members.mapPutJsonRepresentation(assoc.getId(), propertyRepr);
        }
    }

    private void addCollections(final ManagedObject objectAdapter, final JsonRepresentation members, final List<ObjectAssociation> associations) {
        for (final ObjectAssociation assoc : associations) {

            if (mode.checkVisibility()) {
                final Consent visibility = assoc.isVisible(objectAdapter, getInteractionInitiatedBy(), resourceContext.where());
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
                    new ObjectCollectionReprRenderer(getResourceContext(), linkFollowerForColl, collection.getId(), collectionRepresentation);

            var where = resourceContext.where();

            renderer.with(ManagedCollection.of(objectAdapter, collection, where)).usingLinkTo(linkToBuilder);
            if(mode.isEventSerialization()) {
                renderer.asEventSerialization();
            }

            members.mapPutJsonRepresentation(assoc.getId(), renderer.render());
        }
    }

    private void addActions(
            final ManagedObject objectAdapter,
            final Stream<ObjectAction> actions,
            final JsonRepresentation members) {

        actions
        .filter(action->{
            final Consent visibility = action.isVisible(objectAdapter, getInteractionInitiatedBy(), resourceContext.where());
            return visibility.isAllowed();
        })
        .forEach(action->{
            final LinkFollowSpecs linkFollowSpecs = getLinkFollowSpecs().follow("members["+action.getId()+"]");
            final ObjectActionReprRenderer renderer =
                    new ObjectActionReprRenderer(getResourceContext(), linkFollowSpecs, action.getId(),
                            JsonRepresentation.newMap());

            var where = resourceContext.where();

            renderer.with(ManagedAction.of(objectAdapter, action, where)).usingLinkTo(linkToBuilder);
            members.mapPutJsonRepresentation(action.getId(), renderer.render());
        });

    }

    private void addPersistLinkIfTransientAndPersistable() {
        if (ManagedObjects.isIdentifiable(objectAdapter)) {
            return;
        }
        final DomainObjectReprRenderer renderer =
                new DomainObjectReprRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        final JsonRepresentation domainObjectRepr = renderer.with(objectAdapter).asPersistLinkArguments().render();

        final String domainType = objectAdapter.objSpec().logicalTypeName();
        final LinkBuilder persistLinkBuilder = LinkBuilder.newBuilder(getResourceContext(), Rel.PERSIST.getName(), RepresentationType.DOMAIN_OBJECT, "objects/%s", domainType).withHttpMethod(RestfulHttpMethod.POST).withArguments(domainObjectRepr);
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
        if (!ManagedObjects.isIdentifiable(objectAdapter)) {
            return;
        }
        final boolean isService = objectAdapter.objSpec().isInjectable();
        if(isService) {
            return;
        }

        final DomainObjectReprRenderer renderer =
                new DomainObjectReprRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        final JsonRepresentation domainObjectRepr = renderer.with(objectAdapter).asUpdatePropertiesLinkArguments().render();

        if(!getResourceContext().config().isSuppressUpdateLink()) {
            var objectRef = ManagedObjects.stringifyElseFail(objectAdapter);
            var updateLinkBuilder = LinkBuilder.newBuilder(getResourceContext(), Rel.UPDATE.getName(), RepresentationType.DOMAIN_OBJECT, "objects/%s", objectRef).withHttpMethod(RestfulHttpMethod.PUT).withArguments(domainObjectRepr);
            getLinks().arrayAdd(updateLinkBuilder.build());
        }
    }

    public static Object valueOrRef(
            final IResourceContext context,
            final ObjectFeature objectFeature,
            final JsonValueEncoderService jsonValueEncoder,
            final ManagedObject domainObject) {

        var spec = domainObject.objSpec();
        if(spec.isValue()) {
            var context2 = JsonValueConverter.Context.of(objectFeature, context.config().isSuppressMemberExtensions());
            return jsonValueEncoder.asObject(domainObject, context2);
        }

        return DomainObjectReprRenderer.newLinkToBuilder(
                context,
                Rel.VALUE,
                domainObject)
                .withTitle(domainObject.getTitle())
                .build();
    }

}
