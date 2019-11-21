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
package org.apache.isis.viewer.restfulobjects.rendering.domaintypes;

import java.util.stream.Stream;

import com.fasterxml.jackson.databind.node.NullNode;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.Contributed;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.rendering.LinkBuilder;
import org.apache.isis.viewer.restfulobjects.rendering.LinkFollowSpecs;
import org.apache.isis.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.isis.viewer.restfulobjects.rendering.ReprRendererAbstract;

public class DomainTypeReprRenderer extends ReprRendererAbstract<DomainTypeReprRenderer, ObjectSpecification> {

    public static LinkBuilder newLinkToBuilder(final IResourceContext resourceContext, final Rel rel, final ObjectSpecification objectSpec) {
        final String typeFullName = objectSpec.getSpecId().asString();
        final String url = String.format("domain-types/%s", typeFullName);
        return LinkBuilder.newBuilder(resourceContext, rel.getName(), RepresentationType.DOMAIN_TYPE, url);
    }

    public static LinkBuilder newLinkToLayoutBuilder(
            final IResourceContext resourceContext,
            final ObjectSpecification objectSpec) {
        final Rel rel = Rel.LAYOUT;
        final String typeFullName = objectSpec.getSpecId().asString();
        final String url = String.format("domain-types/%s/layout", typeFullName);
        return LinkBuilder.newBuilder(resourceContext, rel.getName(), RepresentationType.LAYOUT, url);
    }

    private ObjectSpecification objectSpecification;

    public DomainTypeReprRenderer(final IResourceContext resourceContext, final LinkFollowSpecs linkFollower, final JsonRepresentation representation) {
        super(resourceContext, linkFollower, RepresentationType.DOMAIN_TYPE, representation);
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

            final JsonRepresentation layoutLink = newLinkToLayoutBuilder(getResourceContext(), objectSpecification).build();
            getLinks().arrayAdd(layoutLink);
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
        final Stream<ObjectAssociation> associations = objectSpecification.streamAssociations(Contributed.EXCLUDED);

        associations.forEach(association->{
            if (association.isOneToOneAssociation()) {
                final OneToOneAssociation property = (OneToOneAssociation) association;
                final LinkBuilder linkBuilder = PropertyDescriptionReprRenderer.newLinkToBuilder(getResourceContext(), Rel.PROPERTY, objectSpecification, property);
                membersList.arrayAdd(linkBuilder.build());
            } else if (association.isOneToManyAssociation()) {
                final OneToManyAssociation collection = (OneToManyAssociation) association;
                final LinkBuilder linkBuilder = CollectionDescriptionReprRenderer.newLinkToBuilder(getResourceContext(), Rel.PROPERTY, objectSpecification, collection);
                membersList.arrayAdd(linkBuilder.build());
            }
        });

        final Stream<ObjectAction> actions = objectSpecification.streamObjectActions(Contributed.INCLUDED);

        actions.forEach(action->{
            final LinkBuilder linkBuilder = ActionDescriptionReprRenderer
                    .newLinkToBuilder(getResourceContext(), Rel.ACTION, objectSpecification, action);
            membersList.arrayAdd(linkBuilder.build());            
        });

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
    }

    private JsonRepresentation linkToIsSubtypeOf() {
        final String url = "domain-types/" + objectSpecification.getSpecId().asString() + "/type-actions/isSubtypeOf/invoke";

        final LinkBuilder linkBuilder = LinkBuilder.newBuilder(getResourceContext(), Rel.INVOKE.andParam("typeaction", "isSubtypeOf"), RepresentationType.TYPE_ACTION_RESULT, url);
        final JsonRepresentation arguments = argumentsTo(getResourceContext(), "supertype", null);
        final JsonRepresentation link = linkBuilder.withArguments(arguments).build();
        return link;
    }

    private JsonRepresentation linkToIsSupertypeOf() {
        final String url = "domain-types/" + objectSpecification.getSpecId().asString() + "/type-actions/isSupertypeOf/invoke";

        final LinkBuilder linkBuilder = LinkBuilder.newBuilder(getResourceContext(), Rel.INVOKE.andParam("typeaction", "isSupertypeOf"), RepresentationType.TYPE_ACTION_RESULT, url);
        final JsonRepresentation arguments = argumentsTo(getResourceContext(), "subtype", null);
        final JsonRepresentation link = linkBuilder.withArguments(arguments).build();
        return link;
    }

    public static JsonRepresentation argumentsTo(final IResourceContext resourceContext, final String paramName, final ObjectSpecification objectSpec) {
        final JsonRepresentation arguments = JsonRepresentation.newMap();
        final JsonRepresentation link = JsonRepresentation.newMap();
        arguments.mapPut(paramName, link);
        if (objectSpec != null) {
            link.mapPut("href", resourceContext.urlFor("domain-types/" + objectSpec.getSpecId().asString()));
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
        if (!_Strings.isNullOrEmpty(description)) {
            getExtensions().mapPut("description", description);
        }
    }

    protected void putExtensionsIfService() {
        getExtensions().mapPut("isService", objectSpecification.isManagedBean());
    }

}
