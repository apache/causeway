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

package org.apache.isis.viewer.wicket.ui.components.value;

import org.apache.wicket.markup.html.basic.Label;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.wicket.model.models.ValueModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * Panel for rendering any value types that do not have their own custom
 * {@link ScalarPanelAbstract panel} to render them.
 */
public class StandaloneValuePanel extends PanelAbstract<ValueModel> {

    private static final long serialVersionUID = 1L;
    private static final String ID_STANDALONE_VALUE = "standaloneValue";

    public StandaloneValuePanel(final String id, final ValueModel valueModel) {
        super(id, valueModel);
        final ObjectAdapter objectAdapter = getModel().getObject();

        final String label = objectAdapter.titleString(null);
        add(new Label(ID_STANDALONE_VALUE, label));
    }

}
