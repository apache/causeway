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
import org.apache.causeway.applib.services.appfeat.ApplicationFeatureId;
import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.schema.common.v2.ValueType;

import lombok.val;

@Component
@Named("causeway.val.ApplicationFeatureIdValueSemantics")
@Priority(PriorityPrecedence.LATE)
public class ApplicationFeatureIdValueSemantics
extends ValueSemanticsAbstract<ApplicationFeatureId>
implements
    Parser<ApplicationFeatureId>,
    Renderer<ApplicationFeatureId> {

    @Override
    public Class<ApplicationFeatureId> getCorrespondingClass() {
        return ApplicationFeatureId.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.STRING; // this type can be easily converted to string and back
    }

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final ApplicationFeatureId value) {
        return decomposeAsString(value, ApplicationFeatureId::asEncodedString, ()->null);
    }

    @Override
    public ApplicationFeatureId compose(final ValueDecomposition decomposition) {
        return composeFromString(decomposition, ApplicationFeatureId::parseEncoded, ()->null);
    }

    // -- RENDERER

    @Override
    public String titlePresentation(final ValueSemanticsProvider.Context context, final ApplicationFeatureId value) {
        return value == null ? "" : value.stringify();
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final ValueSemanticsProvider.Context context, final ApplicationFeatureId value) {
        return value == null ? null : value.stringify();
    }

    @Override
    public ApplicationFeatureId parseTextRepresentation(final ValueSemanticsProvider.Context context, final String text) {
        val input = _Strings.blankToNullOrTrim(text);
        return input!=null
                ? ApplicationFeatureId.parse(input)
                : null;
    }

    @Override
    public int typicalLength() {
        return maxLength();
    }

    @Override
    public int maxLength() {
        return 255;
    }

    @Override
    public Can<ApplicationFeatureId> getExamples() {
        return Can.of(
                ApplicationFeatureId.newNamespace("a.namespace.only"),
                ApplicationFeatureId.newMember("a.namespace", "with_member_id"));
    }

}
