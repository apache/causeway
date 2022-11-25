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

import java.util.Optional;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.jaxb.JaxbService;
import org.apache.causeway.applib.services.urlencoding.UrlEncodingService;
import org.apache.causeway.commons.internal.debug._Debug;
import org.apache.causeway.commons.internal.debug.xray.XrayUi;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public class ViewModelFacetForXmlRootElementAnnotation
extends ViewModelFacetAbstract {

    public static Optional<ViewModelFacet> create(
            final Optional<XmlRootElement> xmlRootElementIfAny,
            final FacetHolder facetHolder) {

        return xmlRootElementIfAny.map(xmlRootElement->
            new ViewModelFacetForXmlRootElementAnnotation(
                    facetHolder));
    }

    private ViewModelFacetForXmlRootElementAnnotation(
            final FacetHolder facetHolder) {
        // overruled by other non fallback ViewModelFacet types
        super(facetHolder, Precedence.DEFAULT);
    }

    @Override
    protected ManagedObject createViewmodel(
            @NonNull final ObjectSpecification viewmodelSpec,
            @NonNull final Bookmark bookmark) {
        final String xmlStr = getUrlEncodingService().decodeToString(bookmark.getIdentifier());

        _Debug.onCondition(XrayUi.isXrayEnabled(), ()->{
            _Debug.log("[JAXB] de-serializing viewmodel %s\n"
                    + "--- XML ---\n"
                    + "%s"
                    + "-----------\n",
                    viewmodelSpec.getLogicalTypeName(),
                    xmlStr);
        });

        val viewmodelPojo = getJaxbService().fromXml(viewmodelSpec.getCorrespondingClass(), xmlStr);
        return viewmodelPojo!=null
                ? ManagedObject.bookmarked(viewmodelSpec, viewmodelPojo, bookmark)
                : ManagedObject.empty(viewmodelSpec);
    }

    @Override
    protected String serialize(final ManagedObject managedObject) {

        final String xml = getJaxbService().toXml(managedObject.getPojo());        final String encoded = getUrlEncodingService().encodeString(xml);
        _Debug.onCondition(XrayUi.isXrayEnabled(), ()->{
            _Debug.log("[JAXB] serializing viewmodel %s\n"
                    + "--- XML ---\n"
                    + "%s"
                    + "-----------\n",
                    managedObject.getSpecification().getLogicalTypeName(),
                    xml);
        });
        return encoded;
    }

    // -- DEPENDENCIES

    @Getter(lazy=true)
    private final JaxbService jaxbService =
        getServiceRegistry().lookupServiceElseFail(JaxbService.class);

    @Getter(lazy=true)
    private final UrlEncodingService urlEncodingService =
        getServiceRegistry().lookupServiceElseFail(UrlEncodingService.class);

}
