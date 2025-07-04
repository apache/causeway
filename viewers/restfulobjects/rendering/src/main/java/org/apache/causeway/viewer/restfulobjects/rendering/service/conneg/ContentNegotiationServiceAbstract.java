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
package org.apache.causeway.viewer.restfulobjects.rendering.service.conneg;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.factory._InstanceUtil;
import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.ResponseFactory;
import org.apache.causeway.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.ObjectAndActionInvocation;

/**
 * @since 1.x {@index}
 */
public abstract class ContentNegotiationServiceAbstract implements ContentNegotiationService {

    @Autowired protected ResponseFactory responseFactory;

    @Override
    public ResponseEntity<Object> buildResponse(
            final IResourceContext resourceContext,
            final ManagedObject objectAdapter) {
        return null;
    }

    @Override
    public ResponseEntity<Object> buildResponse(
            final IResourceContext resourceContext,
            final ManagedProperty objectAndProperty)  {
        return null;
    }

    @Override
    public ResponseEntity<Object> buildResponse(
            final IResourceContext resourceContext,
            final ManagedCollection objectAndCollection) {
        return null;
    }

    @Override
    public ResponseEntity<Object> buildResponse(
            final IResourceContext resourceContext,
            final ManagedAction objectAndAction)  {
        return null;
    }

    @Override
    public ResponseEntity<Object> buildResponse(
            final IResourceContext resourceContext,
            final ObjectAndActionInvocation objectAndActionInvocation) {
        return null;
    }

    // -- convenience methods for subclasses (possibly hooks)

    /**
     * Potential hook to allow a domain object to be mapped.
     */
    protected Object objectOf(final ManagedObject objectAdapter) {
        return objectAdapter.getPojo();
    }

    protected Object returnedObjectOf(final ObjectAndActionInvocation objectAndActionInvocation) {
        final ManagedObject returnedAdapter = objectAndActionInvocation.getReturnedAdapter();
        return objectOf(returnedAdapter);
    }

    protected Class<?> loadClass(final String cls) {
        final Class<?> domainType;
        try {
            domainType = _InstanceUtil.loadClass(cls);
        }catch (final Exception ex) {
            throw RestfulObjectsApplicationException.createWithCause(HttpStatus.BAD_REQUEST, ex);
        }

        return domainType;
    }

    protected void ensureJaxbAnnotated(final Class<?> domainType) {
        if(!_ClassCache.getInstance().head(domainType).hasJaxbRootElementSemantics()) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatus.BAD_REQUEST, "Requested domain Type '" + domainType.getName() + "' is not annotated with JAXB @XmlRootElement annotation");
        }
    }

    protected void ensureDomainObjectAssignable(final String xRoDomainType, final Class<?> domainType, final Object domainObject) {
        if(!domainType.isAssignableFrom(domainObject.getClass())) {
            throw RestfulObjectsApplicationException.createWithMessage(
                HttpStatus.NOT_ACCEPTABLE,
                    "Requested object of type '%s' however the object returned by the domain object is not assignable (is '%s')"
                .formatted(xRoDomainType, domainObject.getClass().getName()));
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
