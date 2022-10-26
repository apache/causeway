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
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.value.semantics.OrderRelation;
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
@Named("causeway.val.BookmarkValueSemantics")
@Priority(PriorityPrecedence.LATE)
public class BookmarkValueSemantics
extends ValueSemanticsAbstract<Bookmark>
implements
    OrderRelation<Bookmark, Void>,
    Parser<Bookmark>,
    Renderer<Bookmark> {

    @Override
    public Class<Bookmark> getCorrespondingClass() {
        return Bookmark.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.STRING; // in this context not a ValueType.REFERENCE
    }

    // -- ORDER RELATION

    @Override
    public Void epsilon() {
        return null; // not used
    }

    @Override
    public int compare(final Bookmark a, final Bookmark b, final Void epsilon) {
        int c = _Strings.compareNullsFirst(a.getLogicalTypeName(), b.getLogicalTypeName());
        if(c!=0) {
            return c;
        }
        c = _Strings.compareNullsFirst(a.getIdentifier(), b.getIdentifier());
        if(c!=0) {
            return c;
        }
        return 0;
    }

    @Override
    public boolean equals(final Bookmark a, final Bookmark b, final Void epsilon) {
        return compare(a, b, epsilon) == 0;
    }

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final Bookmark value) {
        return decomposeAsString(value, Bookmark::stringify, ()->null);
    }

    @Override
    public Bookmark compose(final ValueDecomposition decomposition) {
        return composeFromString(decomposition, Bookmark::parseElseFail, ()->null);
    }

    // -- RENDERER

    @Override
    public String titlePresentation(final ValueSemanticsProvider.Context context, final Bookmark value) {
        return value == null ? "" : value.stringify();
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final ValueSemanticsProvider.Context context, final Bookmark value) {
        return value == null ? null : value.stringify();
    }

    @Override
    public Bookmark parseTextRepresentation(final ValueSemanticsProvider.Context context, final String text) {
        val input = _Strings.blankToNullOrTrim(text);
        return input!=null
                ? Bookmark.parseElseFail(input)
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

    @Override
    public Can<Bookmark> getExamples() {
        return Can.of(
                Bookmark.parseElseFail("a:b"),
                Bookmark.parseElseFail("c:d"));
    }

}
