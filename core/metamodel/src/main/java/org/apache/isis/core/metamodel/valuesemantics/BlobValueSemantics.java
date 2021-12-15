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
package org.apache.isis.core.metamodel.valuesemantics;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Bytes;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.schema.common.v2.ValueType;

@Component
@Named("isis.val.BlobValueSemantics")
public class BlobValueSemantics
extends ValueSemanticsAbstract<Blob>
implements
    EncoderDecoder<Blob>,
    Renderer<Blob> {

    @Override
    public Class<Blob> getCorrespondingClass() {
        return Blob.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.BLOB;
    }

    // RENDERER

    @Override
    public String simpleTextPresentation(final ValueSemanticsProvider.Context context, final Blob value) {
        return render(value, Blob::getName);
    }

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final Blob blob) {
        return blob.getName() + ":" + blob.getMimeType().getBaseType() + ":" +
        _Strings.ofBytes(_Bytes.encodeToBase64(Base64.getEncoder(), blob.getBytes()), StandardCharsets.UTF_8);
    }

    @Override
    public Blob fromEncodedString(final String data) {
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

    @Override
    public Can<Blob> getExamples() {
        return Can.of(
                Blob.of("a Blob", CommonMimeType.BIN, new byte[] {1, 2, 3}),
                Blob.of("another Blob", CommonMimeType.BIN, new byte[] {3, 4}));
    }

}
