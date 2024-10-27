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

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.graph.tree.TreePath;
import org.apache.causeway.applib.services.bookmark.IdStringifier;
import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.schema.common.v2.ValueType;

import lombok.NonNull;

@Component
@Named("causeway.metamodel.value.TreePathValueSemantics")
@Priority(PriorityPrecedence.LATE)
public class TreePathValueSemantics
extends ValueSemanticsAbstract<TreePath>
implements
    Parser<TreePath>,
    Renderer<TreePath>,
    IdStringifier.EntityAgnostic<TreePath> {

    @Override
    public Class<TreePath> getCorrespondingClass() {
        return TreePath.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.STRING; // this type can be easily converted to string and back
    }

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final TreePath value) {
        return decomposeAsString(value, TreePathValueSemantics::canonicalStringify, ()->null);
    }

    @Override
    public TreePath compose(final ValueDecomposition decomposition) {
        return composeFromString(decomposition, TreePathValueSemantics::canonicalDestringify, ()->null);
    }

    // -- ID STRINGIFIER

    @Override
    public String enstring(final @NonNull TreePath value) {
        return canonicalStringify(value);
    }

    @Override
    public TreePath destring(final @NonNull String stringified) {
        return canonicalDestringify(stringified);
    }

    // -- RENDERER

    @Override
    public String titlePresentation(final ValueSemanticsProvider.Context context, final TreePath value) {
        return value == null ? "" : canonicalStringify(value);
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final ValueSemanticsProvider.Context context, final TreePath value) {
        return canonicalStringify(value);
    }

    @Override
    public TreePath parseTextRepresentation(final ValueSemanticsProvider.Context context, final String text) {
        var input = _Strings.blankToNullOrTrim(text);
        return canonicalDestringify(input);
    }

    @Override
    public int typicalLength() {
        return 40;
    }

    @Override
    public int maxLength() {
        return 1024;
    }

    @Override
    public Can<TreePath> getExamples() {
        return Can.of(
                TreePath.root(),
                TreePath.of(0, 1, 2, 3));
    }

    // -- HELPER
    
    private static String canonicalStringify(@Nullable TreePath treePath) {
        return treePath!=null
                ? treePath.stringify("/")
                : null;
    }
    
    private static TreePath canonicalDestringify(@Nullable String input) {
        return input!=null
                ? TreePath.parse(input, "/")
                : null;
    }
    
}
