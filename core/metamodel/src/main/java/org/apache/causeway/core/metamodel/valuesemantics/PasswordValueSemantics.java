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

import javax.annotation.Priority;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.placeholder.PlaceholderRenderService.PlaceholderLiteral;
import org.apache.causeway.applib.value.Password;
import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.schema.common.v2.ValueType;
import org.apache.causeway.schema.common.v2.ValueWithTypeDto;

@Component
@Named("causeway.val.PasswordValueSemantics")
@Priority(PriorityPrecedence.LATE)
public class PasswordValueSemantics
extends ValueSemanticsAbstract<Password>
implements
    Parser<Password>,
    Renderer<Password> {

    @Override
    public Class<Password> getCorrespondingClass() {
        return Password.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.STRING;
    }

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final Password value) {
        return decomposeAsNullable(value, Password::getPassword, ()->null);
    }

    @Override
    public Password compose(final ValueDecomposition decomposition) {
        return composeFromNullable(
                decomposition, ValueWithTypeDto::getString, Password::new, ()->null);
    }

    // -- RENDERER

    @Override
    public String titlePresentation(final Context context, final Password value) {
        return renderTitle(value, v->getPlaceholderRenderService().asText(PlaceholderLiteral.SUPPRESSED));
    }

    @Override
    public String htmlPresentation(final Context context, final Password value) {
        return renderHtml(value, v->getPlaceholderRenderService().asHtml(PlaceholderLiteral.SUPPRESSED));
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final Context context, final Password value) {
        return renderTitle(value, v->getPlaceholderRenderService().asText(PlaceholderLiteral.SUPPRESSED));
    }

    @Override
    public Password parseTextRepresentation(final Context context, final String text) {
        if(_Strings.isEmpty(text)) {
            return null;
        }
        return new Password(text);
    }

    @Override
    public int typicalLength() {
        return 12;
    }

    @Override
    public Can<Password> getExamples() {
        return Can.of(Password.of("a Password"), Password.of("another Password"));
    }

}
