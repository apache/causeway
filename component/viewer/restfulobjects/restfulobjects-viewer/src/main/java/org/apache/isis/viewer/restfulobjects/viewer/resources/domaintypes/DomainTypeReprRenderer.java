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
package org.apache.isis.viewer.restfulobjects.viewer.resources.domaintypes;

import java.util.List;

import com.google.common.base.Strings;

import org.codehaus.jackson.node.NullNode;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionContainer.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.links.Rel;
import org.apache.isis.viewer.restfulobjects.viewer.ResourceContext;
import org.apache.isis.viewer.restfulobjects.viewer.representations.LinkBuilder;
import org.apache.isis.viewer.restfulobjects.viewer.representations.LinkFollower;
import org.apache.isis.viewer.restfulobjects.viewer.representations.ReprRenderer;
import org.apache.isis.viewer.restfulobjects.viewer.representations.ReprRendererAbstract;
import org.apache.isis.viewer.restfulobjects.viewer.representations.ReprRendererFactoryAbstract;

public class DomainTypeReprRenderer extends ReprRendererAbstract<DomainTypeReprRenderer, ObjectSpecification> {

    public static class Factory extends ReprRendererFactoryAbstract {
        public Factory() {
            super(RepresentationType.DOMAIN_TYPE);
        }

        @Override
        public ReprRenderer<?, ?> newRenderer(final ResourceContext resourceContext, final LinkFollower linkFollower, final JsonRepresentation representation) {
            return new DomainTypeReprRenderer(resourceContext, linkFollower, getRepresentationType(), representation);
        }
    }

    public static LinkBuilder newLinkToBuilder(final ResourceContext resourceContext, final Rel rel, final ObjectSpecification objectSpec) {
        final String typeFullName = objectSpec.getFullIdentifier();
        final String url = "domainTypes/" + typeFullName;
        return LinkBuilder.newBuilder(resourceContext, rel, RepresentationType.DOMAIN_TYPE, url);
    }

    private ObjectSpecification objectSpecification;

    public DomainTypeReprRenderer(final ResourceContext resourceContext, final LinkFollower linkFollower, final RepresentationType representationType, final JsonRepresentation representation) {
        super(resourceContext, linkFollower, representationType, representation);
    }

    @Override
    public DomainTypeReprRenderer with(final ObjectSpecification objectSpecification) {
        this.objectSpecification = objectSpecification;
        return cast(this);
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
        }

        representation.mapPut("canonicalName", objectSpecification.getFullIdentifier());
        addMembers();

        addTypeActions();

        putExtensionsNames();
        putExtensionsDescriptionIfAvailable();
        putExtensionsIfService();

        return representation;
    }

    private void addMembers() {
        final JsonRepresentation membersList = JsonRepresentation.newArray();
        representation.mapPut("members", membersList);
        final List<ObjectAssociation> associations = objectSpecification.getAssociations();
        for (final ObjectAssociation association : associations) {
            if (association.isOneToOneAssociation()) {
                final OneToOneAssociation property = (OneToOneAssociation) association;
                final LinkBuilder linkBuilder = PropertyDescriptionReprRenderer.newLinkToBuilder(getResourceContext(), Rel.PROPERTY, objectSpecification, property);
                membersList.arrayAdd(linkBuilder.build());
            } else if (association.isOneToManyAssociation()) {
                final OneToManyAssociation collection = (OneToManyAssociation) association;
                final LinkBuilder linkBuilder = CollectionDescriptionReprRenderer.newLinkToBuilder(getResourceContext(), Rel.PROPERTY, objectSpecification, collection);
                membersList.arrayAdd(linkBuilder.build());
            }
        }
        final List<ObjectAction> actions = objectSpecification.getObjectActions(Contributed.INCLUDED);
        for (final ObjectAction action : actions) {
            final LinkBuilder linkBuilder = ActionDescriptionReprRenderer.newLinkToBuilder(getResourceContext(), Rel.ACTION, objectSpecification, action);
            membersList.arrayAdd(linkBuilder.build());
        }
    }

    private JsonRepresentation getTypeActions() {
        JsonRepresentation typeActions = representation.getArray("typeActions");
        if (typeActions == null) {
            typeActions = JsonRepresentation.newArray();
            representation.mapPut("typeActions", typeActions);
        }
        return typeActions;
    }

    private void addTypeActions() {
        getTypeActions().arrayAdd(linkToIsSubtypeOf());
        getTypeActions().arrayAdd(linkToIsSupertypeOf());
        getTypeActions().arrayAdd(linkToNewTransientInstance());
    }

    private JsonRepresentation linkToIsSubtypeOf() {
        final String url = "domainTypes/" + objectSpecification.getFullIdentifier() + "/typeactions/isSubtypeOf/invoke";

        final LinkBuilder linkBuilder = LinkBuilder.newBuilder(getResourceContext(), Rel.TYPE_ACTION, RepresentationType.TYPE_ACTION_RESULT, url);
        final JsonRepresentation arguments = argumentsTo(getResourceContext(), "supertype", null);
        final JsonRepresentation link = linkBuilder.withArguments(arguments).withId("isSubtypeOf").build();
        return link;
    }

    private JsonRepresentation linkToIsSupertypeOf() {
        final String url = "domainTypes/" + objectSpecification.getFullIdentifier() + "/typeactions/isSupertypeOf/invoke";

        final LinkBuilder linkBuilder = LinkBuilder.newBuilder(getResourceContext(), Rel.TYPE_ACTION, RepresentationType.TYPE_ACTION_RESULT, url);
        final JsonRepresentation arguments = argumentsTo(getResourceContext(), "subtype", null);
        final JsonRepresentation link = linkBuilder.withArguments(arguments).withId("isSupertypeOf").build();
        return link;
    }

    private JsonRepresentation linkToNewTransientInstance() {
        final String url = "domainTypes/" + objectSpecification.getFullIdentifier() + "/typeactions/newTransientInstance/invoke";

        final LinkBuilder linkBuilder = LinkBuilder.newBuilder(getResourceContext(), Rel.TYPE_ACTION, RepresentationType.TYPE_ACTION_RESULT, url);
        final JsonRepresentation link = linkBuilder.withId("newTransientInstance").build();
        return link;
    }

    public static JsonRepresentation argumentsTo(final ResourceContext resourceContext, final String paramName, final ObjectSpecification objectSpec) {
        final JsonRepresentation arguments = JsonRepresentation.newMap();
        final JsonRepresentation link = JsonRepresentation.newMap();
        arguments.mapPut(paramName, link);
        if (objectSpec != null) {
            link.mapPut("href", resourceContext.urlFor("domainTypes/" + objectSpec.getFullIdentifier()));
        } else {
            link.mapPut("href", NullNode.instance);
        }
        return arguments;
    }

    protected void putExtensionsNames() {
        final String singularName = objectSpecification.getSingularName();
        getExtensions().mapPut("friendlyName", singularName);

        final String pluralName = objectSpecification.getPluralName();
        getExtensions().mapPut("pluralName", pluralName);
    }

    protected void putExtensionsDescriptionIfAvailable() {
        final String description = objectSpecification.getDescription();
        if (!Strings.isNullOrEmpty(description)) {
            getExtensions().mapPut("description", description);
        }
    }

    protected void putExtensionsIfService() {
        getExtensions().mapPut("isService", objectSpecification.isService());
    }

}