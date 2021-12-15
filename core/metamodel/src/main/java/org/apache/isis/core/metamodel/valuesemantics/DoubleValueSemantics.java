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

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.value.semantics.DefaultsProvider;
import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.applib.value.semantics.Parser;
import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.schema.common.v2.ValueType;

import lombok.val;

/**
 * due to auto-boxing also handles the primitive variant
 */
@Component
@Named("isis.val.DoubleValueSemantics")
public class DoubleValueSemantics
extends ValueSemanticsAbstract<Double>
implements
    DefaultsProvider<Double>,
    EncoderDecoder<Double>,
    Parser<Double>,
    Renderer<Double> {

    @Override
    public Class<Double> getCorrespondingClass() {
        return Double.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.DOUBLE;
    }

    @Override
    public Double getDefaultValue() {
        return 0.;
    }

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final Double object) {
        return object.toString();
    }

    @Override
    public Double fromEncodedString(final String data) {
        return Double.valueOf(data);
    }

    // -- RENDERER

    @Override
    public String simpleTextPresentation(final Context context, final Double value) {
        return render(value, getNumberFormat(context)::format);
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final Context context, final Double value) {
        return value==null
                ? null
                : getNumberFormat(context)
                    .format(value);
    }

    @Override
    public Double parseTextRepresentation(final Context context, final String text) {
        //TODO at least overflow should be detected
        val bigDec = super.parseDecimal(context, text);
        return bigDec!=null
                ? bigDec.doubleValue() // simply ignoring loss of precision or overflow
                : null;
    }

    @Override
    public int typicalLength() {
        //TODO research - legacy value, what motivates this number?
        return 10;
    }

    @Override
    public int maxLength() {
        //TODO research - legacy value, what motivates this number?
        return 25;
    }

    @Override
    public Can<Double> getExamples() {
        return Can.of(Double.MIN_VALUE, Double.MAX_VALUE);
    }


}
