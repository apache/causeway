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
package org.apache.isis.viewer.wicket.ui.components.scalars.value.compound;

import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.LambdaModel;

import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.Renderer;
import org.apache.isis.applib.annotation.Value;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelTextFieldParseableAbstract;

/**
 * Panel for rendering any value types that do not have their own custom
 * {@link ScalarPanelAbstract panel} to render them.
 *
 * <p>
 * This is a fallback panel; values are expected to be {@link Parser parseable}
 * (typically through the Isis' {@link Value} annotation.
 */
//FIXME[ISIS-2877] introduced for experiments, should be removed before merge into 'master'
public class ValueCompoundPanel
extends ScalarPanelTextFieldParseableAbstract {

    private static final long serialVersionUID = 1L;

    public ValueCompoundPanel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
    }

    @Override
    protected String getScalarPanelType() {
        return "valuePanel";
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        onInitializeReadonly(null);
    }

//    @Override
//    protected InlinePromptConfig getInlinePromptConfig() {
//        return InlinePromptConfig.notSupported();
//    }

    @Override
    protected AbstractTextComponent<String> createTextFieldForRegular(final String id) {
        return new TextField<>(id, LambdaModel.of(()->
            renderer().simpleTextRepresentation(null, scalarModel.getObject().getPojo())));
    }


//    @Override
//    protected Component createComponentForCompact() {
//        val objAdapter = getModel().getObject();
//        return new Label(ID_SCALAR_IF_COMPACT, "compact");
//                //renderer().simpleTextRepresentation(null, objAdapter.getPojo()));
//    }
//
//    @Override
//    protected MarkupContainer createComponentForRegular() {
//        return new LabeledWebMarkupContainer(ID_SCALAR_IF_REGULAR, Model.of("regular")) {};
//    }
//
//    @Override
//    protected Component getScalarValueComponent() {
//        return new Label(ID_SCALAR_VALUE, "regular");
//
////        val formGroup = new FormGroup(ID_SCALAR_IF_REGULAR, null);
////        return formGroup;
//    }

    // -- HELPER

    private Renderer renderer() {
        final ValueFacet<?> valueFacet = scalarModel.getScalarTypeSpec().getFacet(ValueFacet.class);
        return valueFacet.selectDefaultRenderer().get();
    }
}
