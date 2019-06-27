/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.wicket.model.models;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtime.memento.ObjectAdapterMemento;

import static org.apache.isis.viewer.wicket.model.models.EntityModel.createPageParameters;

public class EntityModelForReference implements ObjectAdapterModel {

    private static final long serialVersionUID = 1L;

    private final ScalarModel scalarModel;

    private ObjectAdapterMemento contextAdapterIfAny;
    private EntityModel.RenderingHint renderingHint;


    public EntityModelForReference(final ScalarModel scalarModel) {
        this.scalarModel = scalarModel;
    }

    @Override
    public ObjectAdapter getObject() {
        return scalarModel.getPendingElseCurrentAdapter();
    }

    @Override
    public void setObject(final ObjectAdapter adapter) {
        // no-op
    }

    @Override
    public void detach() {
        // no-op
    }

    @Override
    public ObjectAdapterMemento getContextAdapterIfAny() {
        return contextAdapterIfAny;
    }

    @Override
    public void setContextAdapterIfAny(ObjectAdapterMemento contextAdapterIfAny) {
        this.contextAdapterIfAny = contextAdapterIfAny;
    }

    @Override
    public EntityModel.RenderingHint getRenderingHint() {
        return renderingHint;
    }

    @Override
    public void setRenderingHint(final EntityModel.RenderingHint renderingHint) {
        this.renderingHint = renderingHint;
    }

    @Override
    public PageParameters getPageParametersWithoutUiHints() {
        return createPageParameters(getObject());
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
    public PageParameters getPageParameters() {
        PageParameters pageParameters = createPageParameters(getObject());
        ObjectAdapterMemento oam = ObjectAdapterMemento.ofAdapter(getObject());
        HintPageParameterSerializer.hintStoreToPageParameters(pageParameters, oam);
        return pageParameters;
    }

    @Override
    public boolean isInlinePrompt() {
        return scalarModel.getPromptStyle().isInlineOrInlineAsIfEdit()
                && scalarModel.isEnabled();
    }

}
