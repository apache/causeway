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

package org.apache.isis.viewer.wicket.ui.components.scalars.isisapplib;

import de.agilecoders.wicket.core.util.Attributes;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.isis.applib.value.Password;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelTextFieldParseableAbstract;

/**
 * Panel for rendering scalars of type {@link Password Isis' applib.Password}.
 */
public class IsisPasswordPanel extends ScalarPanelTextFieldParseableAbstract {

    private static final long serialVersionUID = 1L;
    private static final String ID_SCALAR_VALUE = "scalarValue";

    public IsisPasswordPanel(final String id, final ScalarModel scalarModel) {
        super(id, ID_SCALAR_VALUE, scalarModel);
    }

    @Override
    protected void addSemantics() {
        super.addSemantics();
    }

    @Override
    protected AbstractTextComponent<String> createTextFieldForRegular() {
        final PasswordTextField passwordField = new PasswordTextField(idTextField, new Model<String>() {
            private static final long serialVersionUID = 1L;

            @Override
            public String getObject() {
                return getModel().getObjectAsString();
            }

            @Override
            public void setObject(final String object) {
                if (object == null) {
                    getModel().setObject(null);
                } else {
                    getModel().setObjectAsString(object);
                }
            }
        }) {
            @Override
            protected void onComponentTag(ComponentTag tag) {
                Attributes.set(tag, "type", "password");
                super.onComponentTag(tag);
            }
        };

        passwordField.setResetPassword(false);

        return passwordField;
    }

    @Override
    protected IModel<String> getScalarPanelType() {
        return Model.of("isisPasswordPanel");
    }
}
