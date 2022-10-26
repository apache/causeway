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
package org.apache.causeway.viewer.wicket.model.value;

import java.util.Optional;

import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.Renderer.SyntaxHighlighter;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.commons.ScalarRepresentation;
import org.apache.causeway.core.metamodel.spec.feature.ObjectFeature;

import lombok.NonNull;
import lombok.val;

public class OptionsBasedOnValueSemantics
extends ValueSemanticsModelAbstract {

    private static final long serialVersionUID = 1L;

    public OptionsBasedOnValueSemantics(
            final @NonNull ObjectFeature propOrParam,
            final @NonNull ScalarRepresentation scalarRepresentation) {
        super(propOrParam, scalarRepresentation);
    }

    public final Optional<Renderer<?>> lookupRenderer() {
        val valueFacet = valueFacet();
        switch(scalarRepresentation) {
        case EDITING:
            return Optional.empty();
        case VIEWING:
            return Optional.of(propOrParam.fold(
                    prop->valueFacet.selectRendererForPropertyElseFallback(prop),
                    param->valueFacet.selectRendererForParameterElseFallback(param)));
        }
        throw _Exceptions.unmatchedCase(scalarRepresentation);
    }

    public final SyntaxHighlighter getSyntaxHighlighter() {
        return lookupRenderer()
        .map(Renderer::syntaxHighlighter)
        .orElse(SyntaxHighlighter.NONE);
    }

}
