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
package org.apache.causeway.core.metamodel.facets.actions.layout;

import java.util.Optional;

import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.promptStyle.PromptStyleFacet;
import org.apache.causeway.core.metamodel.facets.object.promptStyle.PromptStyleFacetAbstract;

public final class PromptStyleFacetForActionLayoutXml
extends PromptStyleFacetAbstract {

    public static Optional<PromptStyleFacet> create(
            final ActionLayoutData actionLayout,
            final FacetHolder holder,
            final Precedence precedence) {

        return Optional.ofNullable(actionLayout)
            .map(ActionLayoutData::getPromptStyle)
            .map(promptStyle->new PromptStyleFacetForActionLayoutXml(promptStyle, holder, precedence));
    }

    private final PromptStyle promptStyle;

    private PromptStyleFacetForActionLayoutXml(final PromptStyle promptStyle, final FacetHolder holder, final Precedence precedence) {
        super(holder, precedence);
        this.promptStyle = promptStyle;
    }

    @Override
    public PromptStyle value() {
        return promptStyle;
    }

    @Override
    public boolean isObjectTypeSpecific() {
        return true;
    }

}
