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
package org.apache.causeway.viewer.wicket.model.models;

import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.commons.ViewOrEditMode;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.viewer.commons.model.hints.RenderingHint;

/**
 * Wraps a {@link UiAttributeWkt} to act as an {@link ObjectAdapterModel}.
 */
public record ChainingObjectModel(
    /**
     * chaining idiom: the {@link UiAttributeWkt} we are chained to
     */
    UiAttributeWkt attributeModel)
implements ObjectAdapterModel {

    public static ChainingObjectModel chain(final UiAttributeWkt attributeModel) {
        return new ChainingObjectModel(attributeModel);
    }

    /**
     * chaining idiom: the 'local' model derived from the chain
     */
    @Override
    public ManagedObject getObject() {
        return attributeModel().proposedValue().getValue().getValue();
    }

    @Override
    public RenderingHint getRenderingHint() {
        return attributeModel().getRenderingHint();
    }

    @Override
    public ObjectSpecification getTypeOfSpecification() {
        return attributeModel().getElementType();
    }

    @Override
    public ViewOrEditMode getViewOrEditMode() {
        return ViewOrEditMode.VIEWING;
    }

    @Override
    public boolean isInlinePrompt() {
        return attributeModel().getPromptStyle().isInlineAny()
                && !attributeModel().disabledReason().isPresent();
    }

    @Override
    public boolean isContextAdapter(final ManagedObject other) {
        return false;
    }

    @Override
    public void setViewOrEditMode(final ViewOrEditMode mode) {
        throw _Exceptions.unexpectedCodeReach();
    }

}
