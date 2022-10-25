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
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.schema.common.v2.ValueType;

@Component
@Named("causeway.val.ClobValueSemantics")
@Priority(PriorityPrecedence.LATE)
public class ClobValueSemantics
extends ValueSemanticsAbstract<Clob>
implements
    Renderer<Clob> {

    @Override
    public Class<Clob> getCorrespondingClass() {
        return Clob.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.CLOB;
    }

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final Clob value) {
        return decomposeAsNullable(value, UnaryOperator.identity(), ()->null);
    }

    @Override
    public Clob compose(final ValueDecomposition decomposition) {
        return composeFromNullable(
                decomposition, dto->(Clob)CommonDtoUtils.getValueAsObject(dto), UnaryOperator.identity(), ()->null);
    }

    // RENDERER

    @Override
    public String titlePresentation(final ValueSemanticsProvider.Context context, final Clob value) {
        return renderTitle(value, Clob::getName);
    }

    @Override
    public String htmlPresentation(final ValueSemanticsProvider.Context context, final Clob value) {
        return renderHtml(value, Clob::getName);
    }

    // -- ENCODER DECODER

//    private String toEncodedString(final Clob clob) {
//        return clob.getName() + ":" + clob.getMimeType().getBaseType() + ":" + clob.getChars();
//    }
//
//    private Clob fromEncodedString(final String data) {
//        final int colonIdx = data.indexOf(':');
//        final String name  = data.substring(0, colonIdx);
//        final int colon2Idx  = data.indexOf(":", colonIdx+1);
//        final String mimeTypeBase = data.substring(colonIdx+1, colon2Idx);
//        final CharSequence chars = data.substring(colon2Idx+1);
//        try {
//            return new Clob(name, new MimeType(mimeTypeBase), chars);
//        } catch (MimeTypeParseException e) {
//            throw new RuntimeException(e);
//        }
//    }

    // -- EXAMPLES

    @Override
    public Can<Clob> getExamples() {
        return Can.of(
                Clob.of("a Clob", CommonMimeType.TXT, "abc"),
                Clob.of("another Clob", CommonMimeType.TXT, "ef"));
    }

}
