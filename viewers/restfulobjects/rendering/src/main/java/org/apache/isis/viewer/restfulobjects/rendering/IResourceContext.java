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
import java.util.Optional;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.iactn.InteractionProvider;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.DomainObjectReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAdapterLinkTo;
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService;

/**
 * Provides access to request-specific context (eg HTTP headers),
 * session-specific context (eg authentication) and
 * global context (eg configuration settings).
 *
 * @since 1.x  {@index}
 */
public interface IResourceContext {

    /**
     * Prepends with the base URI
     */
    String urlFor(final String url);

    /**
     * Returns the {@link HttpHeaders#getAcceptableMediaTypes() acceptable media types}
     * as obtained from {@link HttpHeaders}.
     */
    List<MediaType> getAcceptableMediaTypes();

    /**
     * Whether this interaction was initiated directly by a
     * {@link InteractionInitiatedBy#USER user} (or indirectly by the
     * {@link InteractionInitiatedBy#FRAMEWORK framework}.
     */
    InteractionInitiatedBy getInteractionInitiatedBy();

    Where getWhere();

    ObjectAdapterLinkTo getObjectAdapterLinkTo();
    List<List<String>> getFollowLinks();
    boolean isValidateOnly();

    boolean honorUiHints();

    boolean objectPropertyValuesOnly();

    boolean suppressDescribedByLinks();
    boolean suppressUpdateLink();
    boolean suppressMemberId();
    boolean suppressMemberLinks();
    boolean suppressMemberExtensions();
    boolean suppressMemberDisabledReason();

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
    RepresentationService.Intent getIntent();

    InteractionProvider getInteractionProvider();
    MetaModelContext getMetaModelContext();

    // -- UTILITY

    default Optional<ManagedObject> getObjectAdapterForOidFromHref(final String oidFromHref) {
        String oidStrUnencoded = UrlDecoderUtils.urlDecode(oidFromHref);
        return Bookmark.parse(oidStrUnencoded)
        .flatMap(getMetaModelContext()::loadObject);
    }

}
