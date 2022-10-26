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

import org.apache.wicket.model.ChainingModel;

import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.commons.ScalarRepresentation;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.viewer.commons.model.hints.RenderingHint;

/**
 * Wraps a {@link ScalarModel} to act as an {@link ObjectAdapterModel}.
 */
public final class ChainingObjectModel
extends ChainingModel<ManagedObject>
implements ObjectAdapterModel {

    private static final long serialVersionUID = 1L;

    public static ChainingObjectModel chain(final ScalarModel scalarModel) {
        return new ChainingObjectModel(scalarModel);
    }

    private ChainingObjectModel(final ScalarModel scalarModel) {
        super(scalarModel);
    }

    /**
     * chaining idiom: the {@link ScalarModel} we are chained to
     */
    public ScalarModel scalarModel() {
        return (ScalarModel) super.getTarget();
    }

    /**
     * chaining idiom: the 'local' model derived from the chain
     */
    @Override
    public ManagedObject getObject() {
        return scalarModel().proposedValue().getValue().getValue();
    }

    @Override
    public RenderingHint getRenderingHint() {
        return scalarModel().getRenderingHint();
    }

    @Override
    public ObjectSpecification getTypeOfSpecification() {
        return scalarModel().getScalarTypeSpec();
    }

    @Override
    public ScalarRepresentation getMode() {
        return ScalarRepresentation.VIEWING;
    }

    @Override
    public boolean isInlinePrompt() {
        return scalarModel().getPromptStyle().isInlineAny()
                && scalarModel().isEnabled();
    }

    @Override
    public boolean isContextAdapter(final ManagedObject other) {
        return false;
    }

    @Override
    public void setMode(final ScalarRepresentation mode) {
        throw _Exceptions.unexpectedCodeReach();
    }

    @Override
    public MetaModelContext getMetaModelContext() {
        return scalarModel().getMetaModelContext();
    }

}
