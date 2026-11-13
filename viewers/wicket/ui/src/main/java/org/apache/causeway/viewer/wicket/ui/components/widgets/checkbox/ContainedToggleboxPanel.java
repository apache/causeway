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
package org.apache.causeway.viewer.wicket.ui.components.widgets.checkbox;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

/**
 * A button contained within its own form.
 */
public class ContainedToggleboxPanel
extends PanelAbstract<Boolean, Model<Boolean>> {

    private static final long serialVersionUID = 1L;

    private static final String ID_CONTAINER = "container";
    private static final String ID_FORM = "form";
    private static final String ID_TOGGLEBOX = "togglebox";

    public ContainedToggleboxPanel(
            final String id,
            final IModel<Boolean> model) {
        super(id);
        var markupContainer = Wkt.containerAdd(this, ID_CONTAINER);
        var form = Wkt.formAdd(markupContainer, ID_FORM);
        Wkt.checkboxAdd(form, ID_TOGGLEBOX, model, ajaxTarget->{/*noop*/});
    }

}
