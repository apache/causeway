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

package org.apache.isis.viewer.wicket.ui.components.scalars.uuid;

import java.util.UUID;

import org.apache.wicket.markup.html.form.AbstractTextComponent;

import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelTextFieldAbstract;
import org.apache.isis.viewer.wicket.ui.components.scalars.TextFieldValueModel;

/**
 * Panel for rendering scalars of type {@link UUID}.
 */
public class UuidPanel extends ScalarPanelTextFieldAbstract<UUID> {

    private static final long serialVersionUID = 1L;

    private static final UuidConverter converter = new UuidConverter();

    public UuidPanel(
            final String id,
            final ScalarModel scalarModel) {
        super(id, scalarModel, UUID.class);
    }

    @Override
    protected AbstractTextComponent<UUID> createTextFieldForRegular(final String id) {
        final ScalarModel model = getModel();
        final TextFieldValueModel<UUID> textFieldValueModel = new TextFieldValueModel<>(this);
        return new UuidTextField(id, textFieldValueModel, cls, model, converter);
    }


    @Override
    protected String getScalarPanelType() {
        return "uuidPanel";
    }
}



