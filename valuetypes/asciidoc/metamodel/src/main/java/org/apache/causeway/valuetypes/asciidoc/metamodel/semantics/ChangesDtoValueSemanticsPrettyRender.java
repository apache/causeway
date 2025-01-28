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
package org.apache.causeway.valuetypes.asciidoc.metamodel.semantics;

import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.core.metamodel.valuesemantics.ChangesDtoValueSemantics;
import org.apache.causeway.valuetypes.asciidoc.applib.CausewayModuleValAsciidocApplib;

import org.jspecify.annotations.NonNull;

@Component
@Named(CausewayModuleValAsciidocApplib.NAMESPACE + "ChangesDtoValueSemanticsPrettyRender")
@Order(PriorityPrecedence.EARLY)
@Qualifier("pretty-render")
public class ChangesDtoValueSemanticsPrettyRender
extends ChangesDtoValueSemantics {

    @Override
    protected String renderXml(final ValueSemanticsProvider.@NonNull Context context, final @NonNull String xml) {
        return _XmlToHtml.toHtml(xml);
    }

    // -- RENDERER

    @Override
    public SyntaxHighlighter syntaxHighlighter() {
        return SyntaxHighlighter.PRISM_DEFAULT;
    }

}
