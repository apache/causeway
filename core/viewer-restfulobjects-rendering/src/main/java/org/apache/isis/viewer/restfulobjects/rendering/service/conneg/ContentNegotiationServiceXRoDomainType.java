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
package org.apache.isis.viewer.restfulobjects.rendering.service.conneg;

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndActionInvocation;
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.isis.viewer.restfulobjects.rendering.service.conmap.ContentMappingService;

/**
 * Handles content negotiation for accept headers requiring <code>application/json</code> or <code>application/xml</code>and specifying an x-ro-domain-type; will delegate to
 * any available {@link ContentMappingService}s to (try to) map the result object into the required representation if possible.
 *
 * <p>
 *     In the accept header the profile is also checked dependent on the resource being invoked; either <code>profile="urn:org.restfulobjects:repr-types/object"</code> for an object representation, or <code>profile="profile=urn:org.restfulobjects:repr-types/action-result"</code> for an action result.
 * </p>
 *
 * <p>
 *     If the accept header specifies <code>application/xml</code> then the service additionally verifies that the (mapped) domain object's
 *     runtime type is annotated with the JAXB {@link javax.xml.bind.annotation.XmlRootElement} annotation so that RestEasy is able to
 *     unambiguously serialize it.
 * </p>
 */
@DomainService(
        nature = NatureOfService.DOMAIN
)
@DomainServiceLayout(
        menuOrder = "1000" // in effect, is the relative priority (lower numbers have higher priority)
)
public class ContentNegotiationServiceXRoDomainType extends ContentNegotiationServiceAbstract {

    public static final String X_RO_DOMAIN_TYPE = "x-ro-domain-type";

    /**
     * search for an accept header in form <code>application/xml;profile=urn:org.restfulobjects:repr-types/object;x-ro-domain-type=todoapp.dto.module.todoitem.ToDoItemDto</code>
     */
    @Override
    public Response.ResponseBuilder buildResponse(
            final RepresentationService.Context2 renderContext2,
            final ObjectAdapter objectAdapter) {

        final Object domainObject = objectOf(objectAdapter);
        return buildResponse(renderContext2, domainObject, RepresentationType.DOMAIN_OBJECT);
    }

    /**
     * search for an accept header in form <code>application/xml;profile=urn:org.restfulobjects:repr-types/action-result;x-ro-domain-type=todoapp.dto.module.todoitem.ToDoItemDto</code>
     */
    @Override
    public Response.ResponseBuilder buildResponse(
            final RepresentationService.Context2 renderContext2,
            final ObjectAndActionInvocation objectAndActionInvocation) {

        final Object domainObject = returnedObjectOf(objectAndActionInvocation);
        return buildResponse(renderContext2, domainObject, RepresentationType.ACTION_RESULT);
    }

    protected Response.ResponseBuilder buildResponse(
            final RepresentationService.Context2 renderContext2,
            final Object domainObject,
            final RepresentationType representationType) {

        final List<MediaType> acceptableMediaTypes = renderContext2.getAcceptableMediaTypes();
        MediaType mediaType =
                representationType.matchesXmlProfileWithParameter(acceptableMediaTypes, X_RO_DOMAIN_TYPE);

        if (mediaType == null) {
            mediaType =
                representationType.matchesJsonProfileWithParameter(acceptableMediaTypes, X_RO_DOMAIN_TYPE);
        }

        if (mediaType == null) {
            return null;
        }

        final String xRoDomainType = mediaType.getParameters().get(X_RO_DOMAIN_TYPE);
        final Class<?> domainType = loadClass(xRoDomainType);

        final Object mappedDomainObject = map(domainObject, acceptableMediaTypes, representationType);

        ensureDomainObjectAssignable(xRoDomainType, domainType, mappedDomainObject);

        if("xml".equals(mediaType.getSubtype())) {
            ensureJaxbAnnotated(mappedDomainObject.getClass());
        }

        return Response.ok(mappedDomainObject, mediaType);

    }

    /**
     * Delegates to any {@link ContentMappingService}s.
     */
    protected Object map(
            final Object domainObject,
            final List<MediaType> acceptableMediaTypes,
            final RepresentationType representationType) {

        final Iterable<ContentMappingService> contentMappingServices = container.lookupServices(ContentMappingService.class);
        for (ContentMappingService contentMappingService : contentMappingServices) {
            Object mappedObject = contentMappingService.map(domainObject, acceptableMediaTypes, representationType);
            if(mappedObject != null) {
                return mappedObject;
            }
        }

        return domainObject;
    }

}
