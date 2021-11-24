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

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.applib.value.semantics.OrderRelation;
import org.apache.isis.applib.value.semantics.Parser;
import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.schema.common.v2.OidDto;
import org.apache.isis.schema.common.v2.ValueType;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Component
@Named("isis.val.OidDtoValueSemantics")
public class OidDtoValueSemantics
extends ValueSemanticsAbstract<OidDto>
implements
    OrderRelation<OidDto, Void>,
    EncoderDecoder<OidDto>,
    Parser<OidDto>,
    Renderer<OidDto> {

    @Override
    public Class<OidDto> getCorrespondingClass() {
        return OidDto.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return UNREPRESENTED;
    }

    // -- ORDER RELATION

    @Override
    public Void epsilon() {
        return null; // not used
    }

    @Override
    public int compare(final OidDto a, final OidDto b, final Void epsilon) {
        int c = _Strings.compareNullsFirst(a.getType(), b.getType());
        if(c!=0) {
            return c;
        }
        c = _Strings.compareNullsFirst(a.getId(), b.getId());
        if(c!=0) {
            return c;
        }
        return 0;
    }

    @Override
    public boolean equals(final OidDto a, final OidDto b, final Void epsilon) {
        return compare(a, b, epsilon) == 0;
    }

    // -- CONSTRUCTOR EXTRACTOR

    @Action
    @RequiredArgsConstructor
    public static class ValueMixin {

        final OidDto value;

        @MemberSupport
        public OidDto act(final String data) {
            return Bookmark.parseElseFail(data).toOidDto();
        }

        @MemberSupport
        public String defaultData() {
            return Bookmark.forOidDto(value).stringify();
        }

    }

    @Override
    public ValueMixin getValueMixin(final OidDto object) {
        return new ValueMixin(object);
    }

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final OidDto object) {
        return Bookmark.forOidDto(object).stringify();
    }

    @Override
    public OidDto fromEncodedString(final String data) {
        return Bookmark.parseElseFail(data).toOidDto();
    }

    // -- RENDERER

    @Override
    public String simpleTextPresentation(final ValueSemanticsProvider.Context context, final OidDto value) {
        return value == null ? "" : Bookmark.forOidDto(value).stringify();
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final ValueSemanticsProvider.Context context, final OidDto value) {
        return value == null ? null : Bookmark.forOidDto(value).stringify();
    }

    @Override
    public OidDto parseTextRepresentation(final ValueSemanticsProvider.Context context, final String text) {
        val input = _Strings.blankToNullOrTrim(text);
        return input!=null
                ? Bookmark.parseElseFail(input)
                        .toOidDto()
                : null;
    }

    @Override
    public int typicalLength() {
        return maxLength();
    }

    @Override
    public int maxLength() {
        return 4048;
    }

}
