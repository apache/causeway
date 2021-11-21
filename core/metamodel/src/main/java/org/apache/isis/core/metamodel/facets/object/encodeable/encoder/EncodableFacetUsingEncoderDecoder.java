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

import java.util.function.BiConsumer;

import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;

public class EncodableFacetUsingEncoderDecoder
extends FacetAbstract
implements EncodableFacet {

    private final EncoderDecoder<?> encoderDecoder;

    public EncodableFacetUsingEncoderDecoder(final EncoderDecoder<?> encoderDecoder, final FacetHolder holder) {
        super(EncodableFacet.class, holder);
        this.encoderDecoder = encoderDecoder;
    }

    // TODO: is this safe? really?
    public static final String ENCODED_NULL = "NULL";

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
        super.visitAttributes(visitor);
        visitor.accept("codec", encoderDecoder.toString());
    }


    @Override
    public ManagedObject fromEncodedString(final String encodedData) {
        _Assert.assertNotNull(encodedData);
        if (ENCODED_NULL.equals(encodedData)) {
            return null;
        } else {
            final Object decodedObject = encoderDecoder.fromEncodedString(encodedData);
            return getObjectManager().adapt(decodedObject);
        }

    }

    @Override
    public String toEncodedString(final ManagedObject adapter) {
        return adapter == null ? ENCODED_NULL: encode(encoderDecoder, adapter.getPojo());
    }

    private static <T> String encode(final EncoderDecoder<T> encoderDecoder, final Object pojo) {
        @SuppressWarnings("unchecked")
        T pojoAsT = (T) pojo;
        return encoderDecoder.toEncodedString(pojoAsT);
    }


}
