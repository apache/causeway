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

package org.apache.isis.core.metamodel.facets.object.encodeable.encoder;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.services.ServicesInjector;

public class EncodableFacetUsingEncoderDecoder extends FacetAbstract implements EncodableFacet {

    private final EncoderDecoder<?> encoderDecoder;
    private final ServicesInjector dependencyInjector;
    private final ObjectAdapterProvider adapterProvider;

    public EncodableFacetUsingEncoderDecoder(final EncoderDecoder<?> encoderDecoder, final FacetHolder holder, final ObjectAdapterProvider adapterProvider, final ServicesInjector dependencyInjector) {
        super(EncodableFacet.class, holder, Derivation.NOT_DERIVED);
        this.encoderDecoder = encoderDecoder;
        this.dependencyInjector = dependencyInjector;
        this.adapterProvider = adapterProvider;
    }

    // TODO: is this safe? really?
    public static String ENCODED_NULL = "NULL";

    @Override
    protected String toStringValues() {
        getDependencyInjector().injectServicesInto(encoderDecoder);
        return encoderDecoder.toString();
    }

    @Override
    public ObjectAdapter fromEncodedString(final String encodedData) {
        Assert.assertNotNull(encodedData);
        if (ENCODED_NULL.equals(encodedData)) {
            return null;
        } else {
            getDependencyInjector().injectServicesInto(encoderDecoder);
            final Object decodedObject = encoderDecoder.fromEncodedString(encodedData);
            return getObjectAdapterProvider().adapterFor(decodedObject);
        }

    }

    @Override
    public String toEncodedString(final ObjectAdapter adapter) {
        getDependencyInjector().injectServicesInto(encoderDecoder);
        return adapter == null ? ENCODED_NULL: encode(encoderDecoder, adapter.getObject());
    }

    private static <T> String encode(final EncoderDecoder<T> encoderDecoder, final Object pojo) {
        @SuppressWarnings("unchecked")
        T pojoAsT = (T) pojo;
        return encoderDecoder.toEncodedString(pojoAsT);
    }

    // //////////////////////////////////////////////////////
    // Dependencies (from constructor)
    // //////////////////////////////////////////////////////

    public ServicesInjector getDependencyInjector() {
        return dependencyInjector;
    }

    public ObjectAdapterProvider getObjectAdapterProvider() {
        return adapterProvider;
    }

}
