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

import javax.ws.rs.core.MediaType;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.context.MetaModelContext;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.security.api.authentication.AuthenticationSession;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.DomainObjectReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAdapterLinkTo;
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService;

public interface IResourceContext {

    String urlFor(final String url);

    List<MediaType> getAcceptableMediaTypes();

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
     * To avoid infinite loops when {@link Render.Type#EAGERLY eagerly} rendering graphs
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
    
    AuthenticationSession getAuthenticationSession();
    
    SpecificationLoader getSpecificationLoader();
    MetaModelContext getMetaModelContext(); // TODO derive from specLoader
    ServiceRegistry getServiceRegistry(); // TODO derive from specLoader
    IsisConfiguration getConfiguration(); // TODO derive from specLoader

    // -- TEMPORARY FOR REFACTORING

    @Deprecated
    default ManagedObject getObjectAdapterElseNull(String oidFromHref) {
        return OidUtils.getObjectAdapterElseNull(this, oidFromHref);
    }

    @Deprecated
    default ManagedObject getObjectAdapterElseNull(String domainType, String instanceIdEncoded) {
        return OidUtils.getObjectAdapterElseNull(this, domainType, instanceIdEncoded);
    }

    
    

}
