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
package org.apache.causeway.viewer.wicket.ui.components.pops.passwd;

import org.apache.wicket.markup.html.form.AbstractTextComponent;

import org.apache.causeway.applib.value.Password;
import org.apache.causeway.viewer.wicket.model.models.PopModel;
import org.apache.causeway.viewer.wicket.ui.components.pops.ScalarPanelTextFieldWithValueSemantics;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

/**
 * Panel for rendering scalars of type {@link Password Causeway' applib.Password}.
 */
public class CausewayPasswordPanel
extends ScalarPanelTextFieldWithValueSemantics<Password> {

    private static final long serialVersionUID = 1L;

    public CausewayPasswordPanel(final String id, final PopModel popModel) {
        super(id, popModel, Password.class);
    }

    @Override
    protected AbstractTextComponent<Password> createTextField(final String id) {
        return Wkt.passwordFieldWithConverter(
                id, unwrappedModel(), type, converter());
    }

}
