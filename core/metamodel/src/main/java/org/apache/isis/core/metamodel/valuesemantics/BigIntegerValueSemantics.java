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

import java.math.BigInteger;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.value.semantics.DefaultsProvider;
import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.applib.value.semantics.Parser;
import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.schema.common.v2.ValueType;

@Component
@Named("isis.val.BigIntegerValueSemantics")
public class BigIntegerValueSemantics
extends ValueSemanticsAbstract<BigInteger>
implements
    DefaultsProvider<BigInteger>,
    EncoderDecoder<BigInteger>,
    Parser<BigInteger>,
    Renderer<BigInteger> {

    @Override
    public Class<BigInteger> getCorrespondingClass() {
        return BigInteger.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.BIG_INTEGER;
    }

    @Override
    public BigInteger getDefaultValue() {
        return BigInteger.ZERO;
    }

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final BigInteger bigInt) {
        return bigInt.toString();
    }

    @Override
    public BigInteger fromEncodedString(final String data) {
        return new BigInteger(data);
    }

    // -- RENDERER

    @Override
    public String simpleTextPresentation(final Context context, final BigInteger value) {
        return render(value, getNumberFormat(context)::format);
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final Context context, final BigInteger value) {
        return value==null
                ? null
                : getNumberFormat(context)
                    .format(value);
    }

    @Override
    public BigInteger parseTextRepresentation(final Context context, final String text) {
        return super.parseInteger(context, text);
    }

    @Override
    public int typicalLength() {
        return 10;
    }

    @Override
    public Can<BigInteger> getExamples() {
        return Can.of(BigInteger.valueOf(-63L), BigInteger.ZERO);
    }

}
