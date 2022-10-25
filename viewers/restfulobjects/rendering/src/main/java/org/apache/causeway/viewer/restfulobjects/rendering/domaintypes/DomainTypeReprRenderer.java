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
package org.apache.causeway.viewer.restfulobjects.rendering.domaintypes;

import com.fasterxml.jackson.databind.node.NullNode;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.Rel;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkBuilder;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkFollowSpecs;
import org.apache.causeway.viewer.restfulobjects.rendering.ReprRendererAbstract;

import lombok.val;

public class DomainTypeReprRenderer
extends ReprRendererAbstract<ObjectSpecification> {

    public static LinkBuilder newLinkToBuilder(
            final IResourceContext resourceContext, final Rel rel, final ObjectSpecification objectSpec) {
        final String typeFullName = objectSpec.getLogicalTypeName();
        final String url = String.format("domain-types/%s", typeFullName);
        return LinkBuilder.newBuilder(resourceContext, rel.getName(), RepresentationType.DOMAIN_TYPE, url);
    }

    public static LinkBuilder newLinkToLayoutBuilder(
            final IResourceContext resourceContext,
            final ObjectSpecification objectSpec) {
        final Rel rel = Rel.LAYOUT;
        final String typeFullName = objectSpec.getLogicalTypeName();
        final String url = String.format("domain-types/%s/layout", typeFullName);
        return LinkBuilder.newBuilder(resourceContext, rel.getName(), RepresentationType.LAYOUT, url);
    }

    private ObjectSpecification objectSpecification;

    public DomainTypeReprRenderer(
            final IResourceContext resourceContext,
            final LinkFollowSpecs linkFollower,
            final JsonRepresentation representation) {
        super(resourceContext, linkFollower, RepresentationType.DOMAIN_TYPE, representation);
    }

    @Override
    public DomainTypeReprRenderer with(final ObjectSpecification objectSpecification) {
        this.objectSpecification = objectSpecification;
        return this;
    }

    @Override
    public JsonRepresentation render() {

        if (objectSpecification == null) {
            throw new IllegalStateException("ObjectSpecification not specified");
        }

        // self
        if (includesSelf) {
            final JsonRepresentation selfLink = newLinkToBuilder(getResourceContext(), Rel.SELF, objectSpecification).build();
            getLinks().arrayAdd(selfLink);

            final JsonRepresentation layoutLink = newLinkToLayoutBuilder(getResourceContext(), objectSpecification).build();
            getLinks().arrayAdd(layoutLink);
        }

        representation.mapPutString("canonicalName", objectSpecification.getFullIdentifier());
        addMembers();

        addTypeActions();

        putExtensionsNames();
        putExtensionsDescriptionIfAvailable();
        putExtensionsIfService();

        return representation;
    }

    private void addMembers() {
        final JsonRepresentation membersMap = JsonRepresentation.newMap();
        representation.mapPutJsonRepresentation("members", membersMap);

        objectSpecification.streamProperties(MixedIn.INCLUDED)
        .forEach(property->
            membersMap.mapPutJsonRepresentation(
                    property.getId(),
                    PropertyDescriptionReprRenderer
                        .newLinkToBuilder(getResourceContext(), Rel.PROPERTY, objectSpecification, property)
                        .build())
        );

        objectSpecification.streamCollections(MixedIn.INCLUDED)
        .forEach(collection->
            membersMap.mapPutJsonRepresentation(
                    collection.getId(),
                    CollectionDescriptionReprRenderer
                        .newLinkToBuilder(getResourceContext(), Rel.COLLECTION, objectSpecification, collection)
                        .build())
        );

        objectSpecification.streamAnyActions(MixedIn.INCLUDED)
        .forEach(action->
            membersMap.mapPutJsonRepresentation(
                    action.getId(),
                    ActionDescriptionReprRenderer
                        .newLinkToBuilder(getResourceContext(), Rel.ACTION, objectSpecification, action)
                        .build())
        );

    }

    private JsonRepresentation getTypeActions() {
        JsonRepresentation typeActions = representation.getMap("typeActions");
        if (typeActions == null) {
            typeActions = JsonRepresentation.newMap();
            representation.mapPutJsonRepresentation("typeActions", typeActions);
        }
        return typeActions;
    }

    private void addTypeActions() {
        val typeActions = getTypeActions();
        typeActions.mapPutJsonRepresentation("isSubtypeOf", linkToIsSubtypeOf());
        typeActions.mapPutJsonRepresentation("isSupertypeOf", linkToIsSupertypeOf());
    }

    private JsonRepresentation linkToIsSubtypeOf() {
        final String url = "domain-types/" + objectSpecification.getLogicalTypeName() + "/type-actions/isSubtypeOf/invoke";

        final LinkBuilder linkBuilder = LinkBuilder.newBuilder(getResourceContext(), Rel.INVOKE.andParam("typeaction", "isSubtypeOf"), RepresentationType.TYPE_ACTION_RESULT, url);
        final JsonRepresentation arguments = argumentsTo(getResourceContext(), "supertype", null);
        final JsonRepresentation link = linkBuilder.withArguments(arguments).build();
        return link;
    }

    private JsonRepresentation linkToIsSupertypeOf() {
        final String url = "domain-types/" + objectSpecification.getLogicalTypeName() + "/type-actions/isSupertypeOf/invoke";

        final LinkBuilder linkBuilder = LinkBuilder.newBuilder(getResourceContext(), Rel.INVOKE.andParam("typeaction", "isSupertypeOf"), RepresentationType.TYPE_ACTION_RESULT, url);
        final JsonRepresentation arguments = argumentsTo(getResourceContext(), "subtype", null);
        final JsonRepresentation link = linkBuilder.withArguments(arguments).build();
        return link;
    }

    public static JsonRepresentation argumentsTo(final IResourceContext resourceContext, final String paramName, final ObjectSpecification objectSpec) {
        final JsonRepresentation arguments = JsonRepresentation.newMap();
        final JsonRepresentation link = JsonRepresentation.newMap();
        arguments.mapPutJsonRepresentation(paramName, link);
        if (objectSpec != null) {
            link.mapPutString("href", resourceContext.restfulUrlFor("domain-types/" + objectSpec.getLogicalTypeName()));
        } else {
            link.mapPutJsonNode("href", NullNode.instance);
        }
        return arguments;
    }

    protected void putExtensionsNames() {
        final String singularName = objectSpecification.getSingularName();
        getExtensions().mapPutString("friendlyName", singularName);

        final String pluralName = objectSpecification.getPluralName();
        getExtensions().mapPutString("pluralName", pluralName);
    }

    protected void putExtensionsDescriptionIfAvailable() {
        final String description = objectSpecification.getDescription();
        if (!_Strings.isNullOrEmpty(description)) {
            getExtensions().mapPutString("description", description);
        }
    }

    protected void putExtensionsIfService() {
        getExtensions().mapPutBoolean("isService", objectSpecification.isInjectable());
    }

}
