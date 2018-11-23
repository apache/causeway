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
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndAction;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndActionInvocation;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndCollection;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndProperty;
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService;

public abstract class ContentNegotiationServiceAbstract implements ContentNegotiationService {

    @Override
    @Programmatic
    @PostConstruct
    public void init() {}

    @Override
    @Programmatic
    @PreDestroy
    public void shutdown() {}

    @Override
    @Programmatic
    public Response.ResponseBuilder buildResponse(
            final RepresentationService.Context renderContext2,
            final ObjectAdapter objectAdapter) {
        return null;
    }

    @Override
    @Programmatic
    public Response.ResponseBuilder buildResponse(
            final RepresentationService.Context renderContext2,
            final ObjectAndProperty objectAndProperty)  {
        return null;
    }

    @Override
    @Programmatic
    public Response.ResponseBuilder buildResponse(
            final RepresentationService.Context renderContext2,
            final ObjectAndCollection objectAndCollection) {
        return null;
    }

    @Override
    @Programmatic
    public Response.ResponseBuilder buildResponse(
            final RepresentationService.Context renderContext2,
            final ObjectAndAction objectAndAction)  {
        return null;
    }

    @Override
    @Programmatic
    public Response.ResponseBuilder buildResponse(
            final RepresentationService.Context renderContext2,
            final ObjectAndActionInvocation objectAndActionInvocation) {
        return null;
    }

    // -- convenience methods for subclasses (possibly hooks)

    /**
     * Potential hook to allow a domain object to be mapped.
     */
    protected Object objectOf(final ObjectAdapter objectAdapter) {
        return objectAdapter.getPojo();
    }

    protected Object returnedObjectOf(final ObjectAndActionInvocation objectAndActionInvocation) {
        final ObjectAdapter returnedAdapter = objectAndActionInvocation.getReturnedAdapter();
        return objectOf(returnedAdapter);
    }

    protected Class<?> loadClass(final String cls) {
        final Class<?> domainType;
        try {
            domainType = InstanceUtil.loadClass(cls);
        }catch (final Exception ex) {
            throw RestfulObjectsApplicationException.createWithCause(RestfulResponse.HttpStatusCode.BAD_REQUEST, ex);
        }

        return domainType;
    }

    protected void ensureJaxbAnnotated(final Class<?> domainType) {
        if(domainType.getAnnotation(XmlRootElement.class) == null) {
            throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.BAD_REQUEST, "Requested domain Type '" + domainType.getName() + "' is not annotated with JAXB @XmlRootElement annotation");
        }
    }

    protected void ensureDomainObjectAssignable(final String xRoDomainType, final Class<?> domainType, final Object domainObject) {
        if(!domainType.isAssignableFrom(domainObject.getClass())) {
            throw RestfulObjectsApplicationException.createWithMessage(
                    RestfulResponse.HttpStatusCode.NOT_ACCEPTABLE,
                    "Requested object of type '%s' however the object returned by the domain object is not assignable (is '%s')",
                    xRoDomainType, domainObject.getClass().getName());
        }
    }

    protected boolean mediaTypeParameterMatches(
            final List<MediaType> acceptableMediaTypes,
            final String parameter, final String parameterValue) {
        for (MediaType mediaType : acceptableMediaTypes) {
            final String paramValue = sanitize(mediaType.getParameters().get(parameter));
            if (Objects.equals(paramValue, parameterValue)) {
                return true;
            }
        }
        return false;
    }
    
    protected List<String> mediaTypeParameterList(
            final List<MediaType> acceptableMediaTypes,
            final String parameter) {
        final List<String> paramList = _Lists.newArrayList();
        for (MediaType mediaType : acceptableMediaTypes) {
            final String paramValue = sanitize(mediaType.getParameters().get(parameter));
            _Strings.splitThenStream(paramValue, ",")
            .map(String::trim)
            .forEach(paramList::add);
        }
        return paramList;
    }
    

    /**
     * Remove any single quotes.
     */
    private String sanitize(String mediaParam) {
        if (mediaParam == null) {
            return null;
        }
        mediaParam = mediaParam.trim();
        if(mediaParam.startsWith("'")) {
            mediaParam = mediaParam.substring(1);
        }
        if(mediaParam.endsWith("'")) {
            mediaParam = mediaParam.substring(0, mediaParam.length()-1);
        }
        return mediaParam;
    }

}
