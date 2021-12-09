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
package org.apache.isis.core.metamodel.facets.object.recreatable;

import java.util.Optional;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.urlencoding.UrlEncodingService;
import org.apache.isis.commons.internal.debug._Debug;
import org.apache.isis.commons.internal.debug.xray.XrayUi;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.HasPostConstructMethodCache;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.Getter;
import lombok.NonNull;

public class RecreatableObjectFacetForXmlRootElementAnnotation
extends RecreatableObjectFacetAbstract {

    public RecreatableObjectFacetForXmlRootElementAnnotation(
            final FacetHolder holder,
            final HasPostConstructMethodCache postConstructMethodCache) {

        super(holder, RecreationMechanism.INSTANTIATES, postConstructMethodCache, Precedence.HIGH);
    }

    @Override
    protected Object doInstantiate(final Class<?> viewModelClass, final @NonNull Optional<Bookmark> bookmark) {
        final String xmlStr = getUrlEncodingService().decodeToString(bookmark.map(Bookmark::getIdentifier).orElse(null));
        final Object viewModelPojo = getJaxbService().fromXml(viewModelClass, xmlStr);
        return viewModelPojo;
    }

    @Override
    protected String serialize(final ManagedObject managedObject) {
        final String xml = getJaxbService().toXml(managedObject.getPojo());
        final String encoded = getUrlEncodingService().encodeString(xml);
        _Debug.onCondition(XrayUi.isXrayEnabled(), ()->{
            _Debug.log("[JAXB] serializing viewmodel %s", managedObject.getSpecification().getLogicalTypeName());
        });
        return encoded;
    }

    @Override
    public boolean containsEntities() {
        return true; //XXX future work might improve that for performance optimizations, such that we need to actually check at facet creation
    }

    // -- DEPENDENCIES

    @Getter(lazy=true)
    private final JaxbService jaxbService =
        getServiceRegistry().lookupServiceElseFail(JaxbService.class);

    @Getter(lazy=true)
    private final UrlEncodingService urlEncodingService =
        getServiceRegistry().lookupServiceElseFail(UrlEncodingService.class);

}
