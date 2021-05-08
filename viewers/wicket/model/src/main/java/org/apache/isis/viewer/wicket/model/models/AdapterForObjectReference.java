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
package org.apache.isis.viewer.wicket.model.models;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.common.model.object.ObjectUiModel.Mode;
import org.apache.isis.viewer.common.model.object.ObjectUiModel.RenderingHint;

import lombok.val;

/**
 *
 * Wraps a {@link ScalarModel} to act as an {@link ObjectAdapterModel}.
 *
 */
public class AdapterForObjectReference implements ObjectAdapterModel {

    private static final long serialVersionUID = 1L;

    private final ScalarModel scalarModel;

    public AdapterForObjectReference(final ScalarModel scalarModel) {
        this.scalarModel = scalarModel;
    }

    @Override
    public ManagedObject getObject() {
        return scalarModel.getPendingElseCurrentAdapter();
    }

    @Override
    public RenderingHint getRenderingHint() {
        return scalarModel.getRenderingHint();
    }

    @Override
    public void setRenderingHint(RenderingHint renderingHint) {
        scalarModel.setRenderingHint(renderingHint);
    }

    @Override
    public ObjectSpecification getTypeOfSpecification() {
        return scalarModel.getTypeOfSpecification();
    }

    @Override
    public EntityModel.Mode getMode() {
        return EntityModel.Mode.VIEW;
    }

    @Override
    public void setMode(Mode mode) {
        // no-op
    }

    @Override
    public void setObject(final ManagedObject adapter) {
        // no-op
    }

    @Override
    public void detach() {
        // no-op
    }

    @Override
    public PageParameters getPageParameters() {
        val pageParameters = getPageParametersWithoutUiHints();
        HintPageParameterSerializer.hintStoreToPageParameters(pageParameters, scalarModel);
        return pageParameters;
    }

    @Override
    public PageParameters getPageParametersWithoutUiHints() {
        return PageParameterUtil.createPageParametersForObject(getObject());
    }

    @Override
    public boolean isInlinePrompt() {
        return scalarModel.getPromptStyle().isInlineOrInlineAsIfEdit()
                && scalarModel.isEnabled();
    }

    @Override
    public boolean isContextAdapter(ManagedObject other) {
        return false;
    }

}
