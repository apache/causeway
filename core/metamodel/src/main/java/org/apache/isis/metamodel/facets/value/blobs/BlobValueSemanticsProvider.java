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

package org.apache.isis.metamodel.facets.value.blobs;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.apache.isis.applib.adapters.DefaultsProvider;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.commons.internal.base._Bytes;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.object.value.vsp.ValueSemanticsProviderAndFacetAbstract;


public class BlobValueSemanticsProvider extends ValueSemanticsProviderAndFacetAbstract<Blob> implements BlobValueFacet {

    private static final int TYPICAL_LENGTH = 0;

    private static Class<? extends Facet> type() {
        return BlobValueFacet.class;
    }

    private static final Blob DEFAULT_VALUE = null;

    /**
     * Required because implementation of {@link Parser} and
     * {@link EncoderDecoder}.
     */
    public BlobValueSemanticsProvider() {
        this(null);
    }

    public BlobValueSemanticsProvider(final FacetHolder holder) {
        super(type(), holder, Blob.class, TYPICAL_LENGTH, -1, Immutability.IMMUTABLE, EqualByContent.NOT_HONOURED, DEFAULT_VALUE);
    }

    @Override
    public String titleString(final Object object) {
        return object != null? ((Blob)object).getName(): "[null]";
    }

    @Override
    public String titleStringWithMask(final Object value, final String usingMask) {
        return titleString(value);
    }


    // //////////////////////////////////////////////////////////////////
    // Parser
    // //////////////////////////////////////////////////////////////////

    @Override
    public Parser<Blob> getParser() {
        return null;
    }

    // //////////////////////////////////////////////////////////////////
    // DefaultsProvider
    // //////////////////////////////////////////////////////////////////

    @Override
    public DefaultsProvider<Blob> getDefaultsProvider() {
        return null;
    }

    // //////////////////////////////////////////////////////////////////
    // EncoderDecoder
    // //////////////////////////////////////////////////////////////////

    @Override
    protected String doEncode(final Object object) {
        Blob blob = (Blob)object;
        return blob.getName() + ":" + blob.getMimeType().getBaseType() + ":" + 
        _Strings.ofBytes(_Bytes.encodeToBase64(Base64.getEncoder(), blob.getBytes()), StandardCharsets.UTF_8);
    }

    @Override
    protected Blob doRestore(final String data) {
        final int colonIdx = data.indexOf(':');
        final String name  = data.substring(0, colonIdx);
        final int colon2Idx  = data.indexOf(":", colonIdx+1);
        final String mimeTypeBase = data.substring(colonIdx+1, colon2Idx);
        final String payload = data.substring(colon2Idx+1);
        final byte[] bytes = _Bytes.decodeBase64(Base64.getDecoder(), payload.getBytes(StandardCharsets.UTF_8));
        try {
            return new Blob(name, new MimeType(mimeTypeBase), bytes);
        } catch (MimeTypeParseException e) {
            throw new RuntimeException(e);
        }
    }

    // /////// toString ///////

    @Override
    public String toString() {
        return "BlobValueSemanticsProvider";
    }

}
