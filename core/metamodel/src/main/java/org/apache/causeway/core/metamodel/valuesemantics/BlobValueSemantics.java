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
package org.apache.causeway.core.metamodel.valuesemantics;

import java.util.function.UnaryOperator;

import javax.annotation.Priority;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.util.schema.CommonDtoUtils;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.schema.common.v2.ValueType;

@Component
@Named("causeway.val.BlobValueSemantics")
@Priority(PriorityPrecedence.LATE)
public class BlobValueSemantics
extends ValueSemanticsAbstract<Blob>
implements
    Renderer<Blob> {

    @Override
    public Class<Blob> getCorrespondingClass() {
        return Blob.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.BLOB;
    }

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final Blob value) {
        return decomposeAsNullable(value, UnaryOperator.identity(), ()->null);
    }

    @Override
    public Blob compose(final ValueDecomposition decomposition) {
        return composeFromNullable(
                decomposition, dto->(Blob)CommonDtoUtils.getValueAsObject(dto), UnaryOperator.identity(), ()->null);
    }

    // RENDERER

    @Override
    public String titlePresentation(final ValueSemanticsProvider.Context context, final Blob value) {
        return renderTitle(value, Blob::getName);
    }

    @Override
    public String htmlPresentation(final ValueSemanticsProvider.Context context, final Blob value) {
        return renderHtml(value, Blob::getName);
    }

    // -- ENCODER DECODER

//    public String toEncodedString(final Blob blob) {
//        return blob.getName() + ":" + blob.getMimeType().getBaseType() + ":" +
//        _Strings.ofBytes(_Bytes.encodeToBase64(Base64.getEncoder(), blob.getBytes()), StandardCharsets.UTF_8);
//    }
//
//    public Blob fromEncodedString(final String data) {
//        final int colonIdx = data.indexOf(':');
//        final String name  = data.substring(0, colonIdx);
//        final int colon2Idx  = data.indexOf(":", colonIdx+1);
//        final String mimeTypeBase = data.substring(colonIdx+1, colon2Idx);
//        final String payload = data.substring(colon2Idx+1);
//        final byte[] bytes = _Bytes.decodeBase64(Base64.getDecoder(), payload.getBytes(StandardCharsets.UTF_8));
//        try {
//            return new Blob(name, new MimeType(mimeTypeBase), bytes);
//        } catch (MimeTypeParseException e) {
//            throw new RuntimeException(e);
//        }
//    }

    // -- EXAMPLES

    @Override
    public Can<Blob> getExamples() {
        return Can.of(
                Blob.of("a Blob", CommonMimeType.BIN, new byte[] {1, 2, 3}),
                Blob.of("another Blob", CommonMimeType.BIN, new byte[] {3, 4}));
    }

}
