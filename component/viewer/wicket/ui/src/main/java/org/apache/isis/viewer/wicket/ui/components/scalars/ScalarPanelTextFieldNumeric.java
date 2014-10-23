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

package org.apache.isis.viewer.wicket.ui.components.scalars;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;

import org.apache.isis.viewer.wicket.model.models.ScalarModel;

/**
 * Panel for rendering numeric scalars.
 */
public abstract class ScalarPanelTextFieldNumeric<T extends Serializable> extends ScalarPanelTextFieldAbstract<T> {

    private static final long serialVersionUID = 1L;

    public ScalarPanelTextFieldNumeric(final String id, final ScalarModel scalarModel, final Class<T> cls) {
        super(id, scalarModel, cls);
    }

    @Override
    protected void addSemantics() {
        super.addSemantics();
    }

    @Override
    protected AbstractTextComponent<T> createTextFieldForRegular() {
        return createTextField(ID_SCALAR_VALUE);
    }

    protected Component addComponentForCompact() {
        Fragment compactFragment = getCompactFragment(CompactType.INPUT_TEXT);
        final AbstractTextComponent<T> textField = createTextField(ID_SCALAR_IF_COMPACT);
        final IModel<T> model = textField.getModel();
        final T object = model.getObject();
        model.setObject(object);
        
        textField.setEnabled(false);
        setTextFieldSizeAndMaxLengthIfSpecified(textField);

        compactFragment.add(textField);
        scalarTypeContainer.addOrReplace(compactFragment);
        return textField;
    }

}
