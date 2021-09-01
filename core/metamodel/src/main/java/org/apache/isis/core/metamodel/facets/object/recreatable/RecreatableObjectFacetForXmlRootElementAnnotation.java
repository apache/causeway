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

import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.urlencoding.UrlEncodingService;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.PostConstructMethodCache;

public class RecreatableObjectFacetForXmlRootElementAnnotation
extends RecreatableObjectFacetAbstract {


    public RecreatableObjectFacetForXmlRootElementAnnotation(
            final FacetHolder holder,
            final PostConstructMethodCache postConstructMethodCache) {

        super(holder, RecreationMechanism.INSTANTIATES, postConstructMethodCache, Precedence.HIGH);
    }

    @Override
    protected Object doInstantiate(final Class<?> viewModelClass, final String mementoStr) {

        final String xmlStr = getUrlEncodingService().decodeToString(mementoStr);
        final Object viewModelPojo = getJaxbService().fromXml(viewModelClass, xmlStr);

        return viewModelPojo;
    }

    @Override
    public String memento(final Object pojo) {

        final String xml = getJaxbService().toXml(pojo);
        final String encoded = getUrlEncodingService().encodeString(xml);

        return encoded;
    }

    // -- DEPENDENCIES

    private JaxbService getJaxbService() {
        return getServiceRegistry().lookupServiceElseFail(JaxbService.class);
    }

    private UrlEncodingService getUrlEncodingService() {
        return getServiceRegistry().lookupServiceElseFail(UrlEncodingService.class);
    }

}
