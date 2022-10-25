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
import org.apache.causeway.applib.exceptions.recoverable.TextEntryParseException;
import org.apache.causeway.applib.value.semantics.DefaultsProvider;
import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.schema.common.v2.ValueWithTypeDto;

import lombok.val;

/**
 * due to auto-boxing also handles the primitive variant
 */
@Component
@Named("causeway.val.BooleanValueSemantics")
@Priority(PriorityPrecedence.LATE)
public class BooleanValueSemantics
extends ValueSemanticsAbstract<Boolean>
implements
    DefaultsProvider<Boolean>,
    Parser<Boolean>,
    Renderer<Boolean> {

    @Override
    public Class<Boolean> getCorrespondingClass() {
        return Boolean.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.BOOLEAN;
    }

    @Override
    public Boolean getDefaultValue() {
        return Boolean.FALSE;
    }

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final Boolean value) {
        return decomposeAsNullable(value, UnaryOperator.identity(), ()->null);
    }

    @Override
    public Boolean compose(final ValueDecomposition decomposition) {
        return composeFromNullable(
                decomposition, ValueWithTypeDto::isBoolean, UnaryOperator.identity(), ()->null);
    }

    // -- RENDERER

    @Override
    public String titlePresentation(final ValueSemanticsProvider.Context context, final Boolean value) {
        return renderTitle(value, v->translate(v.booleanValue() ? "True" : "False"));
    }

    @Override
    public String htmlPresentation(final ValueSemanticsProvider.Context context, final Boolean value) {
        return renderHtml(value, v->translate(v.booleanValue() ? "True" : "False"));
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final ValueSemanticsProvider.Context context, final Boolean value) {
        return value != null ? value.toString(): null;
    }

    @Override
    public Boolean parseTextRepresentation(final ValueSemanticsProvider.Context context, final String text) {
        val input = _Strings.blankToNullOrTrim(text);
        if(input==null) {
            return null;
        }
        if ("true".equalsIgnoreCase(input)) {
            return Boolean.TRUE;
        } else if ("false".equalsIgnoreCase(input)) {
            return Boolean.FALSE;
        } else {
            throw new TextEntryParseException(String.format("'%s' cannot be parsed as a boolean", input));
        }
    }

    @Override
    public int typicalLength() {
        return maxLength();
    }

    @Override
    public int maxLength() {
        return 6;
    }

    @Override
    public Can<Boolean> getExamples() {
        return Can.of(Boolean.TRUE, Boolean.FALSE);
    }

    //XXX not localized yet - maybe can be done at a more fundamental level - or replace with universal symbols
//  return BooleanModel.forScalarModel(scalarModel())
//          .asStringModel("(not set)", "Yes", "No");


}
