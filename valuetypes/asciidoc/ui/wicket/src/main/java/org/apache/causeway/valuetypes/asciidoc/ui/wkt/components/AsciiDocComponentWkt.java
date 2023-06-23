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
package org.apache.causeway.valuetypes.asciidoc.ui.wkt.components;

import org.apache.wicket.model.IModel;

import org.apache.causeway.applib.value.semantics.Renderer.SyntaxHighlighter;
import org.apache.causeway.viewer.wicket.ui.components.scalars.markup.MarkupComponent;

public class AsciiDocComponentWkt extends MarkupComponent {

    private static final long serialVersionUID = 1L;

    public AsciiDocComponentWkt(final String id, final IModel<?> model){
        super(id, model,
                org.apache.causeway.viewer.wicket.ui.components.scalars.markup.MarkupComponent.Options.builder()
                .syntaxHighlighter(SyntaxHighlighter.PRISM_COY)
                .build());
    }

}
