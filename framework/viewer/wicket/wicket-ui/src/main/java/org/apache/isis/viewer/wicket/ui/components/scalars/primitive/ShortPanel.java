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

package org.apache.isis.viewer.wicket.ui.components.scalars.primitive;

import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.TextField;

import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelTextFieldAbstract;
import org.apache.isis.viewer.wicket.ui.components.scalars.TextFieldValueModel;

/**
 * Panel for rendering scalars of type {@link Short} or <tt>short</tt>.
 */
public class ShortPanel extends ScalarPanelTextFieldAbstract<Short> {

    private static final long serialVersionUID = 1L;
    private static final String ID_SCALAR_VALUE = "scalarValue";

    public ShortPanel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel, Short.class);
    }

    @Override
    protected void addSemantics() {
        super.addSemantics();
    }

    @Override
    protected AbstractTextComponent<Short> createTextField() {
        return new TextField<Short>(ID_SCALAR_VALUE, new TextFieldValueModel<Short>(this), Short.class);
    }

}
