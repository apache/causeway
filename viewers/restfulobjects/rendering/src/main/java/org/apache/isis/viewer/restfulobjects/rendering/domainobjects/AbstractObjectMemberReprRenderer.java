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
package org.apache.isis.viewer.restfulobjects.rendering.domainobjects;

import com.fasterxml.jackson.databind.node.NullNode;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.interaction.ManagedMember;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.isis.viewer.restfulobjects.rendering.LinkFollowSpecs;
import org.apache.isis.viewer.restfulobjects.rendering.ReprRendererAbstract;

public abstract class AbstractObjectMemberReprRenderer<
    R extends ReprRendererAbstract<R, ManagedMember>, 
    T extends ObjectMember> 
extends ReprRendererAbstract<R, ManagedMember> {

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
     * Derived from {@link #objectMember} using {@link org.apache.isis.viewer.restfulobjects.rendering.domainobjects.MemberType#determineFrom(ObjectFeature)}
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
    private final Where where;

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
    public R with(final ManagedMember objectAndMember) {
        this.objectAdapter = objectAndMember.getOwner();
        this.objectMember = _Casts.uncheckedCast(objectAndMember.getMember());
        this.objectMemberType = MemberType.determineFrom(objectMember);
        this.memberId = objectMember.getId();
        usingLinkTo(new DomainObjectLinkTo());

        return cast(this);
    }

    /**
     * Must be called after {@link #with(ObjectAndMember)} (which provides the
     * {@link #objectAdapter}).
     */
    public R usingLinkTo(final ObjectAdapterLinkTo linkTo) {
        this.linkTo = linkTo.usingUrlBase(resourceContext).with(objectAdapter);
        return cast(this);
    }

    /**
     * Indicate that this is a standalone representation.
     */
    public R asStandalone() {
        mode = Mode.STANDALONE;
        return cast(this);
    }

    public R asEventSerialization() {
        mode = Mode.EVENT_SERIALIZATION;
        return cast(this);
    }

    /**
     * Indicate that this is a representation to include as the result of a
     * followed link.
     */
    public R asFollowed() {
        mode = Mode.FOLLOWED;
        return cast(this);
    }

    /**
     * Indicates that the representation was produced as the result of a
     * resource that mutated the state.
     *
     * <p>
     * The effect of this is to suppress the link to self.
     */
    public R asMutated() {
        mode = Mode.MUTATED;
        return cast(this);
    }

    public R asArguments() {
        mode = Mode.ARGUMENTS;
        return cast(this);
    }

    /**
     * For subclasses to call from their {@link #render()} method.
     */
    protected void renderMemberContent() {

        if(!resourceContext.suppressMemberId()) {
            representation.mapPut("id", objectMember.getId());
        }

        if(!mode.isArguments()) {
            representation.mapPut("memberType", objectMemberType.getName());
        }

        if (mode.isInline() && !resourceContext.suppressMemberLinks()) {
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

            if(!mode.isInline() || !resourceContext.suppressUpdateLink()) {
                putExtensionsIsisProprietary();
            }
            addLinksToFormalDomainModel();
            addLinksIsisProprietary();
        }
    }

    public void withMemberMode(ManagedMember.RepresentationMode memberMode) {
        if(memberMode.isWrite()) {
            this.asMutated();
        } else {
            this.asStandalone();
        }
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
    protected void addLinkFor(final MutatorSpec mutatorSpec) {
        if (!hasMemberFacet(mutatorSpec.mutatorFacetType)) {
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
            repr.mapPut("value", NullNode.getInstance()); // force a null into
            // the map
            return repr;
        }
        // overridden by actions
        throw new UnsupportedOperationException("override mutatorArgs() to populate for many arguments");
    }

    private void addDetailsLinkIfPersistent() {
        if (!ManagedObject.isIdentifiable(objectAdapter)) {
            return;
        }
        final JsonRepresentation link = linkTo.memberBuilder(Rel.DETAILS, objectMemberType, objectMember).build();
        getLinks().arrayAdd(link);

        final LinkFollowSpecs membersLinkFollower = getLinkFollowSpecs();
        final LinkFollowSpecs detailsLinkFollower = membersLinkFollower.follow("links");

        // create a temporary map that looks the same as the member map we'll be following
        final JsonRepresentation memberMap = JsonRepresentation.newMap();
        memberMap.mapPut(getMemberId(), representation);
        if (membersLinkFollower.matches(memberMap) && detailsLinkFollower.matches(link)) {
            followDetailsLink(link);
        }
        return;
    }

    protected abstract void followDetailsLink(JsonRepresentation detailsLink);

    protected final void putDisabledReasonIfDisabled() {
        if(resourceContext.suppressMemberDisabledReason()) {
            return;
        }
        final String disabledReasonRep = usability().getReason();
        representation.mapPut("disabledReason", disabledReasonRep);
    }

    protected abstract void putExtensionsIsisProprietary();

    protected abstract void addLinksToFormalDomainModel();

    protected abstract void addLinksIsisProprietary();

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
        final ObjectSpecification objetMemberSpec = objectMember.getSpecification();
        return objetMemberSpec.getFacet(facetType);
    }

    protected boolean hasMemberFacet(final Class<? extends Facet> facetType) {
        return objectMember.getFacet(facetType) != null;
    }

    protected Consent usability() {
        return objectMember.isUsable(objectAdapter, getInteractionInitiatedBy(), where);
    }

    protected Consent visibility() {
        return objectMember.isVisible(objectAdapter, getInteractionInitiatedBy(), where);
    }

}
