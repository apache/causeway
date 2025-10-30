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
package org.apache.causeway.core.metamodel.facets.object.viewmodel;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.services.jaxb.JaxbService;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.util.hmac.HmacUrlCodec;

public final class ViewModelFacetForXmlRootElementAnnotation
extends SecureViewModelFacet {

    public static Optional<ViewModelFacet> create(
            final boolean hasRootElementAnnotation,
            final HmacUrlCodec hmacUrlCodec,
            final JaxbService jaxbService,
            final FacetHolder facetHolder) {

        return hasRootElementAnnotation
                && hmacUrlCodec!=null
                && jaxbService!=null
            ? Optional.of(new ViewModelFacetForXmlRootElementAnnotation(hmacUrlCodec, jaxbService, facetHolder))
            : Optional.empty();
    }

    private final JaxbService jaxbService;

    private ViewModelFacetForXmlRootElementAnnotation(
            final HmacUrlCodec hmacUrlCodec,
            final JaxbService jaxbService,
            final FacetHolder facetHolder) {
        // overruled by other non fallback ViewModelFacet types
        super(hmacUrlCodec, facetHolder, Precedence.DEFAULT);
        this.jaxbService = jaxbService;
    }

    @Override
    protected Object createViewmodelPojo(
            final @NonNull ObjectSpecification viewmodelSpec,
            final @NonNull byte[] trustedBookmarkIdAsBytes) {

        var trustedXml = new String(trustedBookmarkIdAsBytes, StandardCharsets.UTF_8);
        var viewmodelPojo = jaxbService.fromXml(viewmodelSpec.getCorrespondingClass(), trustedXml);
        return viewmodelPojo;
    }

    @Override
    protected byte[] encodeState(final ManagedObject managedObject) {
        final String xml = jaxbService.toXml(managedObject.getPojo());
        return xml.getBytes(StandardCharsets.UTF_8);
    }

}
