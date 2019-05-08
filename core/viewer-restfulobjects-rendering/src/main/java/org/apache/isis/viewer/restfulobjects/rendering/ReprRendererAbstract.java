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
package org.apache.isis.viewer.restfulobjects.rendering;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.ws.rs.core.MediaType;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.MetaModelContext;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.DomainObjectReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.domaintypes.DomainTypeReprRenderer;

import lombok.val;

public abstract class ReprRendererAbstract<R extends ReprRendererAbstract<R, T>, T> implements ReprRenderer<R, T> {

    protected final RendererContext rendererContext;
    private final LinkFollowSpecs linkFollower;
    private final RepresentationType representationType;
    protected final JsonRepresentation representation;
    private final Map<String,String> mediaTypeParams = _Maps.newLinkedHashMap();

    private final InteractionInitiatedBy interactionInitiatedBy;

    protected boolean includesSelf;

    public ReprRendererAbstract(
            final RendererContext rendererContext,
            final LinkFollowSpecs linkFollower,
            final RepresentationType representationType,
            final JsonRepresentation representation) {
        this.rendererContext = rendererContext;
        this.linkFollower = asProvidedElseCreate(linkFollower);
        this.representationType = representationType;
        this.representation = representation;

        this.interactionInitiatedBy = determineInteractionInitiatedByFrom(this.rendererContext);
    }

    private static InteractionInitiatedBy determineInteractionInitiatedByFrom(
            final RendererContext rendererContext) {
        return rendererContext.getInteractionInitiatedBy();
    }

    protected InteractionInitiatedBy getInteractionInitiatedBy() {
        return interactionInitiatedBy;
    }


    public RendererContext getRendererContext() {
        return rendererContext;
    }

    public LinkFollowSpecs getLinkFollowSpecs() {
        return linkFollower;
    }

    private LinkFollowSpecs asProvidedElseCreate(final LinkFollowSpecs linkFollower) {
        if (linkFollower != null) {
            return linkFollower;
        }
        return LinkFollowSpecs.create(rendererContext.getFollowLinks());
    }

    @Override
    public MediaType getMediaType() {
        return representationType.getMediaType(mediaTypeParams);
    }

    protected void addMediaTypeParams(String param, String paramValue) {
        mediaTypeParams.put(param, paramValue);
    }

    @SuppressWarnings("unchecked")
    public R includesSelf() {
        this.includesSelf = true;
        return (R) this;
    }

    public R withLink(final Rel rel, final String href) {
        if (href != null) {
            getLinks().arrayAdd(LinkBuilder.newBuilder(rendererContext, rel.getName(), representationType, href).build());
        }
        return cast(this);
    }

    public R withLink(final Rel rel, final JsonRepresentation link) {
        final String relStr = link.getString("rel");
        if (relStr == null || !relStr.equals(rel.getName())) {
            throw new IllegalArgumentException("Provided link does not have a 'rel' of '" + rel.getName() + "'; was: " + link);
        }
        if (link != null) {
            getLinks().arrayAdd(link);
        }
        return cast(this);
    }


    /**
     * Will lazily create links array as required
     */
    protected JsonRepresentation getLinks() {
        JsonRepresentation links = representation.getArray("links");
        if (links == null) {
            links = JsonRepresentation.newArray();
            representation.mapPut("links", links);
        }
        return links;
    }

    protected void addLink(final Rel rel, final ObjectSpecification objectSpec) {
        if (objectSpec == null) {
            return;
        }
        final LinkBuilder linkBuilder = DomainTypeReprRenderer.newLinkToBuilder(getRendererContext(), rel, objectSpec);
        JsonRepresentation link = linkBuilder.build();
        getLinks().arrayAdd(link);

        final LinkFollowSpecs linkFollower = getLinkFollowSpecs().follow("links");
        if (linkFollower.matches(link)) {
            final DomainTypeReprRenderer renderer = new DomainTypeReprRenderer(getRendererContext(), linkFollower, JsonRepresentation.newMap())
                    .with(objectSpec);
            link.mapPut("value", renderer.render());
        }

    }

    /**
     * Will lazily create extensions map as required
     */
    protected JsonRepresentation getExtensions() {
        JsonRepresentation extensions = representation.getMap("extensions");
        if (extensions == null) {
            extensions = JsonRepresentation.newMap();
            representation.mapPut("extensions", extensions);
        }
        return extensions;
    }

    public R withExtensions(final JsonRepresentation extensions) {
        if (!extensions.isMap()) {
            throw new IllegalArgumentException("extensions must be a map");
        }
        representation.mapPut("extensions", extensions);
        return cast(this);
    }

    @SuppressWarnings("unchecked")
    protected static <R extends ReprRendererAbstract<R, T>, T> R cast(final ReprRendererAbstract<R, T> builder) {
        return (R) builder;
    }

    @Override
    public abstract JsonRepresentation render();

    /**
     * Convenience for representations that are returned from objects that
     * mutate state.
     */
    protected final void addExtensionsIsisProprietaryChangedObjects() {

        // TODO: have removed UpdateNotifier, plan is to re-introduce using the IsisTransaction enlisted objects
        // (which would also allow newly-created objects to be shown)
        final List<ObjectAdapter> changedObjects = _Lists.newArrayList(); // updateNotifier.getChangedObjects();
        final List<ObjectAdapter> disposedObjects = _Lists.newArrayList(); // updateNotifier.getDisposedObjects();

        addToExtensions("changed", changedObjects);
        addToExtensions("disposed", disposedObjects);
    }

    private void addToExtensions(final String key, final List<ObjectAdapter> adapters) {
        if(adapters == null || adapters.isEmpty()) {
            return;
        }
        final JsonRepresentation adapterList = JsonRepresentation.newArray();
        getExtensions().mapPut(key, adapterList);
        for (final ObjectAdapter adapter : adapters) {
            adapterList.arrayAdd(DomainObjectReprRenderer.newLinkToBuilder(getRendererContext(), Rel.VALUE, adapter).build());
        }
    }

    protected Stream<ObjectAdapter> streamServiceAdapters() {
    	val metaModelContext = MetaModelContext.current();
        return metaModelContext.streamServiceAdapters();
    }

}
