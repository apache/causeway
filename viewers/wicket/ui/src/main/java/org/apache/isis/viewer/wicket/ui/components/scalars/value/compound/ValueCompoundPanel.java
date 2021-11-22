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

import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelTextFieldTextualAbstract;

//FIXME[ISIS-2877] introduced for experiments, should be removed
public class ValueCompoundPanel
extends ScalarPanelTextFieldTextualAbstract {

    private static final long serialVersionUID = 1L;

    public ValueCompoundPanel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
    }

    @Override
    protected void onInitialize() {
        System.err.printf("%s%n", scalarModel.getMetaModel().getFeatureIdentifier());

        super.onInitialize();
        //onInitializeReadonly(null);
    }

    @Override
    protected AbstractTextComponent<String> createTextField(final String id) {
        return new TextField<>(id, LambdaModel.of(()->
            renderer().simpleTextPresentation(null, scalarModel.getObject().getPojo())));
    }

    // -- HELPER

    private Renderer renderer() {
        final ValueFacet<?> valueFacet = scalarModel.getScalarTypeSpec().getFacet(ValueFacet.class);
        return valueFacet.selectDefaultRenderer().get();
    }
}
