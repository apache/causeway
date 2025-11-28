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
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.config.CausewayConfiguration.Viewer.Restfulobjects;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.interactions.InteractionConstraint;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.DomainObjectReprRenderer;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.ObjectAdapterLinkTo;
import org.apache.causeway.viewer.restfulobjects.rendering.service.RepresentationService;

/**
 * Provides access to request-specific context (eg HTTP headers),
 * session-specific context (eg authentication) and
 * global context (eg configuration settings).
 *
 * @since 1.x revised for 4.0 {@index}
 */
public interface IResourceContext extends HasMetaModelContext {

    /**
     * Prepends with the servlet's base URI
     * @param url - (/restful/) relative resource
     */
    String restfulUrlFor(final String url);

    /**
     * Prepends with the application's base URI.
     * @param url - relative resource, must not include context-path if any
     */
    String applicationUrlFor(final String url);

    /**
     * Returns the {@link HttpHeaders#getAccept() acceptable media types}
     * as obtained from {@link HttpHeaders}.
     */
    List<MediaType> acceptableMediaTypes();

    /**
     * @since 4.0
     */
    InteractionConstraint iConstraint();

    ObjectAdapterLinkTo objectAdapterLinkTo();
    List<List<String>> followLinks();
    boolean isValidateOnly();

    default Restfulobjects config() {
        return getMetaModelContext().getConfiguration().viewer().restfulobjects();
    }

    /**
     * To avoid infinite loops when eagerly rendering graphs
     * of objects as {@link DomainObjectReprRenderer#asEventSerialization() events}.
     *
     * <p>
     * @param objectAdapter - the object proposed to be rendered eagerly
     * @return whether this adapter has already been rendered (implying the caller should not render the value).
     */
    boolean canEagerlyRender(ManagedObject objectAdapter);

    /**
     * Applies only when rendering a domain object.
     */
    RepresentationService.Intent intent();

    // -- UTILITY

    default Optional<ManagedObject> objectAdapterForOidFromHref(final String oidFromHref) {
        String oidStrUnencoded = UrlDecoderUtils.urlDecode(oidFromHref);
        return Bookmark.parse(oidStrUnencoded)
        .flatMap(getMetaModelContext().getObjectManager()::loadObject);
    }

}
