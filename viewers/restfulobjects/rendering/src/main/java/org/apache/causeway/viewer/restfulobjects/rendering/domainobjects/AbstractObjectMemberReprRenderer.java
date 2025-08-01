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

import com.fasterxml.jackson.databind.node.NullNode;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedMember;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.Rel;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkFollowSpecs;
import org.apache.causeway.viewer.restfulobjects.rendering.ReprRendererAbstract;

import org.jspecify.annotations.NonNull;

public abstract class AbstractObjectMemberReprRenderer<T extends ObjectMember>
extends ReprRendererAbstract<ManagedMember> {

    protected enum Mode {
        INLINE, FOLLOWED, STANDALONE, MUTATED, ARGUMENTS, EVENT_SERIALIZATION;

        public boolean isInline() {
            return this == INLINE;
        }

        public boolean isFollowed() {
            return this == FOLLOWED;
        }

        public boolean isStandalone() {
            return this == STANDALONE;
        }

        public boolean isMutated() {
            return this == MUTATED;
        }

        public boolean isArguments() {
            return this == ARGUMENTS;
        }

        public boolean isEventSerialization() {
            return this == EVENT_SERIALIZATION;
        }
    }

    protected ObjectAdapterLinkTo linkTo;

    protected ManagedObject objectAdapter;
    protected Mode mode = Mode.INLINE; // unless we determine otherwise
    /**
     * Derived from {@link #objectMember} using {@link org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.MemberType#determineFrom(ObjectFeature)}
     */
    protected MemberType objectMemberType;
    protected T objectMember;

    /**
     * Not for rendering, but is the key that the representation being rendered will be held under.
     *
     * <p>
     * Used to determine whether to follow links; only populated for {@link Mode#INLINE inline} Mode.
     */
    private String memberId;
    protected final Where where;

    public AbstractObjectMemberReprRenderer(
            final IResourceContext resourceContext,
            final LinkFollowSpecs linkFollower,
            final String memberId,
            final RepresentationType representationType,
            final JsonRepresentation representation,
            final Where where) {
        super(resourceContext, linkFollower, representationType, representation);
        this.memberId = memberId;
        this.where = where;
    }

    protected String getMemberId() {
        return memberId;
    }

    @Override
    public AbstractObjectMemberReprRenderer<T> with(final ManagedMember objectAndMember) {
        this.objectAdapter = objectAndMember.getOwner();
        this.objectMember = _Casts.uncheckedCast(objectAndMember.getMetaModel());
        this.objectMemberType = MemberType.determineFrom(objectMember);
        this.memberId = objectMember.getId();
        usingLinkTo(new DomainObjectLinkTo());

        return this;
    }

    /**
     * Must be called after {@link #with(ManagedMember)} (which provides the
     * {@link #objectAdapter}).
     */
    public AbstractObjectMemberReprRenderer<T> usingLinkTo(final ObjectAdapterLinkTo linkTo) {
        this.linkTo = linkTo.usingUrlBase(resourceContext).with(objectAdapter);
        return this;
    }

    /**
     * Indicate that this is a standalone representation.
     */
    public AbstractObjectMemberReprRenderer<T> asStandalone() {
        mode = Mode.STANDALONE;
        return this;
    }

    public AbstractObjectMemberReprRenderer<T> asEventSerialization() {
        mode = Mode.EVENT_SERIALIZATION;
        return this;
    }

    /**
     * Indicate that this is a representation to include as the result of a
     * followed link.
     */
    public AbstractObjectMemberReprRenderer<T> asFollowed() {
        mode = Mode.FOLLOWED;
        return this;
    }

    /**
     * Indicates that the representation was produced as the result of a
     * resource that mutated the state.
     *
     * <p>
     * The effect of this is to suppress the link to self.
     */
    public AbstractObjectMemberReprRenderer<T> asMutated() {
        mode = Mode.MUTATED;
        return this;
    }

    public AbstractObjectMemberReprRenderer<T> asArguments() {
        mode = Mode.ARGUMENTS;
        return this;
    }

    /**
     * For subclasses to call from their {@link #render()} method.
     */
    protected void renderMemberContent() {

        if(!resourceContext.config().suppressMemberId()) {
            representation.mapPutString("id", objectMember.getId());
        }

        if(!mode.isArguments()) {
            representation.mapPutString("memberType", objectMemberType.getName());
        }

        if (mode.isInline() && !resourceContext.config().suppressMemberLinks()) {
            addDetailsLinkIfPersistent();
        }

        if (mode.isStandalone()) {
            addLinkToSelf();
        }

        if (mode.isStandalone() || mode.isMutated()) {
            addLinkToUp();
        }

        if (mode.isFollowed() || mode.isStandalone() || mode.isMutated()) {
            addMutatorLinksIfEnabled();

            if(!mode.isInline() || !resourceContext.config().suppressUpdateLink()) {
                putExtensionsCausewayProprietary();
            }
            addLinksToFormalDomainModel();
        }
    }

    public AbstractObjectMemberReprRenderer<T> withMemberMode(final ManagedMember.RepresentationMode memberMode) {
        return switch (memberMode) {
            case READ -> this.asStandalone();
            case WRITE -> this.asMutated();
            case AUTO -> this;
        };
    }

    private void addLinkToSelf() {
        getLinks().arrayAdd(linkTo.memberBuilder(Rel.SELF, objectMemberType, objectMember).build());
    }

    private void addLinkToUp() {
        getLinks().arrayAdd(linkTo.builder(Rel.UP).build());
    }

    protected abstract void addMutatorLinksIfEnabled();

    /**
     * For subclasses to call back to when {@link #addMutatorLinksIfEnabled() adding
     * mutators}.
     */
    protected void addLinkFor(final @NonNull MutatorSpec mutatorSpec) {
        if (!mutatorSpec.appliesTo(objectMember)) {
            return;
        }
        final JsonRepresentation arguments = mutatorArgs(mutatorSpec);
        final RepresentationType representationType = objectMemberType.getRepresentationType();
        final JsonRepresentation mutatorLink = linkToForMutatorInvoke().memberBuilder(mutatorSpec.rel, objectMemberType, objectMember, representationType, mutatorSpec.suffix).withHttpMethod(mutatorSpec.httpMethod).withArguments(arguments).build();
        getLinks().arrayAdd(mutatorLink);
    }

    /**
     * Hook to allow actions to render invoke links that point to the
     * contributing service.
     */
    protected ObjectAdapterLinkTo linkToForMutatorInvoke() {
        return linkTo;
    }

    /**
     * Default implementation (common to properties and collections) that can be
     * overridden (ie by actions) if required.
     */
    protected JsonRepresentation mutatorArgs(final MutatorSpec mutatorSpec) {
        if (mutatorSpec.arguments.isNone()) {
            return null;
        }
        if (mutatorSpec.arguments.isOne()) {
            final JsonRepresentation repr = JsonRepresentation.newMap();
            repr.mapPutJsonNode("value", NullNode.getInstance()); // force a null into
            // the map
            return repr;
        }
        // overridden by actions
        throw new UnsupportedOperationException("override mutatorArgs() to populate for many arguments");
    }

    private void addDetailsLinkIfPersistent() {
        if (!ManagedObjects.isIdentifiable(objectAdapter)) {
            return;
        }
        final JsonRepresentation link = linkTo.memberBuilder(Rel.DETAILS, objectMemberType, objectMember).build();
        getLinks().arrayAdd(link);

        final LinkFollowSpecs membersLinkFollower = getLinkFollowSpecs();
        final LinkFollowSpecs detailsLinkFollower = membersLinkFollower.follow("links");

        // create a temporary map that looks the same as the member map we'll be following
        final JsonRepresentation memberMap = JsonRepresentation.newMap();
        memberMap.mapPutJsonRepresentation(getMemberId(), representation);
        if (membersLinkFollower.matches(memberMap) && detailsLinkFollower.matches(link)) {
            followDetailsLink(link);
        }
        return;
    }

    protected abstract void followDetailsLink(JsonRepresentation detailsLink);

    protected final void putDisabledReasonIfDisabled() {
        if(resourceContext.config().suppressMemberDisabledReason()) {
            return;
        }
        final String disabledReasonRep = usability().getReasonAsString().orElse(null);
        representation.mapPutString("disabledReason", disabledReasonRep);
    }

    protected abstract void putExtensionsCausewayProprietary();

    protected abstract void addLinksToFormalDomainModel();

    /**
     * Convenience method.
     */
    public boolean isMemberVisible() {
        return visibility().isAllowed();
    }

    /**
     * Convenience method.
     */
    protected <F extends Facet> F getMemberSpecFacet(final Class<F> facetType) {
        return objectMember.getElementType().getFacet(facetType);
    }

    protected Consent usability() {
        return objectMember.isUsable(objectAdapter, getInteractionInitiatedBy(), where);
    }

    protected Consent visibility() {
        return objectMember.isVisible(objectAdapter, getInteractionInitiatedBy(), where);
    }

}
