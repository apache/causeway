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
package org.apache.causeway.viewer.wicket.ui.components.attributes;

import java.util.EnumSet;
import java.util.Optional;

import org.apache.wicket.util.convert.IConverter;

import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;

/**
 * Specialization of {@link AttributePanelWithTextField},
 * where the scalar (parameter or property) is a value-type, that is
 * textual and requires no conversion.
 */
public abstract class TextualAttributePanel
extends AttributePanelWithTextField<String> {

    private static final long serialVersionUID = 1L;

    protected TextualAttributePanel(
            final String id, final UiAttributeWkt attributeModel) {
        super(id, attributeModel, String.class);
    }

    @Override
    protected void setupFormatModifiers(final EnumSet<FormatModifier> modifiers) {
        // enforce use of text representation
        modifiers.add(FormatModifier.TEXT_ONLY);
    }

    @Override
    protected final Optional<IConverter<String>> converter() {
        return Optional.empty(); // does not use conversion
    }

}
