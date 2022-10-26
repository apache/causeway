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

import java.nio.file.InvalidPathException;

import javax.annotation.Priority;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.value.LocalResourcePath;
import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.schema.common.v2.ValueType;

import lombok.val;

@Component
@Named("causeway.val.LocalResourcePathValueSemantics")
@Priority(PriorityPrecedence.LATE)
public class LocalResourcePathValueSemantics
extends ValueSemanticsAbstract<LocalResourcePath>
implements
    Parser<LocalResourcePath>,
    Renderer<LocalResourcePath> {

    @Override
    public Class<LocalResourcePath> getCorrespondingClass() {
        return LocalResourcePath.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.STRING; // this type can be easily converted to string and back
    }

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final LocalResourcePath value) {
        return decomposeAsString(value, LocalResourcePath::getValue, ()->null);
    }

    @Override
    public LocalResourcePath compose(final ValueDecomposition decomposition) {
        return composeFromString(decomposition, LocalResourcePath::new, ()->null);
    }

    // -- RENDERER

    @Override
    public String titlePresentation(final Context context, final LocalResourcePath value) {
        return renderTitle(value, LocalResourcePath::getValue);
    }

    @Override
    public String htmlPresentation(final Context context, final LocalResourcePath value) {
        return renderHtml(value, v->toHtmlLink(v));
    }

    private String toHtmlLink(final LocalResourcePath path) {
        val href = path.getValue();
        return String.format("<a "
                + "target=\"_blank\" "
                + "class=\"no-click-bubbling\" "
                + "href=\"%s\">%s</a>", href, href);
    }


    // -- PARSER

    @Override
    public String parseableTextRepresentation(final Context context, final LocalResourcePath value) {
        return value != null ? value.getValue() : null;
    }

    @Override
    public LocalResourcePath parseTextRepresentation(final Context context, final String text) {
        val input = _Strings.blankToNullOrTrim(text);
        if(input==null) {
            return null;
        }
        try {
            return new LocalResourcePath(input);
        } catch (final InvalidPathException ex) {
            throw new IllegalArgumentException("Not parseable as a LocalResourcePath ('" + input + "')", ex);
        }
    }

    @Override
    public int typicalLength() {
        return 100;
    }

    @Override
    public int maxLength() {
        return 2083;
    }

    @Override
    public Can<LocalResourcePath> getExamples() {
        return Can.of(
                new LocalResourcePath("img/a"),
                new LocalResourcePath("img/b"));
    }


}
