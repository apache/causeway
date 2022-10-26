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
package org.apache.causeway.viewer.restfulobjects.rendering;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.ws.rs.core.MediaType;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.Rel;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.DomainObjectReprRenderer;
import org.apache.causeway.viewer.restfulobjects.rendering.domaintypes.DomainTypeReprRenderer;
import org.apache.causeway.viewer.restfulobjects.rendering.service.valuerender.JsonValueEncoderService;

import lombok.Getter;
import lombok.val;

public abstract class ReprRendererAbstract<T>
implements ReprRenderer<T> {

    @Getter protected final IResourceContext resourceContext;
    @Getter protected final JsonValueEncoderService jsonValueEncoder;

    private final LinkFollowSpecs linkFollower;
    private final RepresentationType representationType;
    protected final JsonRepresentation representation;
    private final Map<String,String> mediaTypeParams = _Maps.newLinkedHashMap();

    private final InteractionInitiatedBy interactionInitiatedBy;

    protected boolean includesSelf;

    public ReprRendererAbstract(
            final IResourceContext resourceContext,
            final LinkFollowSpecs linkFollower,
            final RepresentationType representationType,
            final JsonRepresentation representation) {
        this.resourceContext = resourceContext;
        this.jsonValueEncoder = resourceContext.getMetaModelContext().getServiceRegistry()
                .lookupServiceElseFail(JsonValueEncoderService.class);

        this.linkFollower = asProvidedElseCreate(linkFollower);
        this.representationType = representationType;
        this.representation = representation;

        this.interactionInitiatedBy = determineInteractionInitiatedByFrom(this.resourceContext);
    }

    private static InteractionInitiatedBy determineInteractionInitiatedByFrom(
            final IResourceContext resourceContext) {
        return resourceContext.getInteractionInitiatedBy();
    }

    protected InteractionInitiatedBy getInteractionInitiatedBy() {
        return interactionInitiatedBy;
    }

    public LinkFollowSpecs getLinkFollowSpecs() {
        return linkFollower;
    }

    private LinkFollowSpecs asProvidedElseCreate(final LinkFollowSpecs linkFollower) {
        if (linkFollower != null) {
            return linkFollower;
        }
        return LinkFollowSpecs.create(resourceContext.getFollowLinks());
    }

    @Override
    public MediaType getMediaType() {
        return representationType.getJsonMediaType(mediaTypeParams);
    }

    protected void addMediaTypeParams(final String param, final String paramValue) {
        mediaTypeParams.put(param, paramValue);
    }

    public <R extends ReprRendererAbstract<T>> R includesSelf() {
        this.includesSelf = true;
        return _Casts.uncheckedCast(this);
    }

    public <R extends ReprRendererAbstract<T>> R withLink(final Rel rel, final String href) {
        if (href != null) {
            getLinks().arrayAdd(LinkBuilder.newBuilder(resourceContext, rel.getName(), representationType, href).build());
        }
        return _Casts.uncheckedCast(this);
    }

    public <R extends ReprRendererAbstract<T>> R withLink(final Rel rel, final JsonRepresentation link) {
        final String relStr = link.getString("rel");
        if (relStr == null || !relStr.equals(rel.getName())) {
            throw new IllegalArgumentException("Provided link does not have a 'rel' of '" + rel.getName() + "'; was: " + link);
        }
        if (link != null) {
            getLinks().arrayAdd(link);
        }
        return _Casts.uncheckedCast(this);
    }


    /**
     * Will lazily create links array as required
     */
    protected JsonRepresentation getLinks() {
        JsonRepresentation links = representation.getArray("links");
        if (links == null) {
            links = JsonRepresentation.newArray();
            representation.mapPutJsonRepresentation("links", links);
        }
        return links;
    }

    protected void addLink(final Rel rel, final ObjectSpecification objectSpec) {
        if (objectSpec == null) {
            return;
        }
        final LinkBuilder linkBuilder = DomainTypeReprRenderer.newLinkToBuilder(getResourceContext(), rel, objectSpec);
        JsonRepresentation link = linkBuilder.build();
        getLinks().arrayAdd(link);

        final LinkFollowSpecs linkFollower = getLinkFollowSpecs().follow("links");
        if (linkFollower.matches(link)) {
            final DomainTypeReprRenderer renderer = new DomainTypeReprRenderer(getResourceContext(), linkFollower, JsonRepresentation.newMap())
                    .with(objectSpec);
            link.mapPutJsonRepresentation("value", renderer.render());
        }

    }

    /**
     * Will lazily create extensions map as required
     */
    protected JsonRepresentation getExtensions() {
        JsonRepresentation extensions = representation.getMap("extensions");
        if (extensions == null) {
            extensions = JsonRepresentation.newMap();
            representation.mapPutJsonRepresentation("extensions", extensions);
        }
        return extensions;
    }

    public ReprRendererAbstract<T> withExtensions(final JsonRepresentation extensions) {
        if (!extensions.isMap()) {
            throw new IllegalArgumentException("extensions must be a map");
        }
        representation.mapPutJsonRepresentation("extensions", extensions);
        return this;
    }


    @Override
    public abstract JsonRepresentation render();

    /**
     * Convenience for representations that are returned from objects that
     * mutate state.
     */
    protected final void addExtensionsCausewayProprietaryChangedObjects() {

        // TODO: have removed UpdateNotifier, plan is to re-introduce using the CausewayTransaction
        // enlisted objects (which would also allow newly-created objects to be shown)
        final List<ManagedObject> changedObjects = _Lists.newArrayList(); // updateNotifier.getChangedObjects();
        final List<ManagedObject> disposedObjects = _Lists.newArrayList(); // updateNotifier.getDisposedObjects();

        addToExtensions("changed", changedObjects);
        addToExtensions("disposed", disposedObjects);
    }

    private void addToExtensions(final String key, final List<ManagedObject> adapters) {
        if(adapters == null || adapters.isEmpty()) {
            return;
        }
        final JsonRepresentation adapterList = JsonRepresentation.newArray();
        getExtensions().mapPutJsonRepresentation(key, adapterList);
        for (val adapter : adapters) {
            adapterList.arrayAdd(DomainObjectReprRenderer.newLinkToBuilder(getResourceContext(), Rel.VALUE, adapter).build());
        }
    }

    protected Stream<ManagedObject> streamServiceAdapters() {
        val metaModelContext = resourceContext.getMetaModelContext();
        return metaModelContext.streamServiceAdapters();
    }

}
