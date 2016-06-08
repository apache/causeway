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
package org.apache.isis.viewer.wicket.ui.components.layout.bs3.row;

import org.apache.wicket.markup.html.WebMarkupContainer;

import org.apache.isis.applib.layout.grid.bootstrap3.BS3ClearFix;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Col;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Row;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3RowContent;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.components.layout.bs3.Util;
import org.apache.isis.viewer.wicket.ui.components.layout.bs3.clearfix.ClearFix;
import org.apache.isis.viewer.wicket.ui.components.layout.bs3.col.Col;
import org.apache.isis.viewer.wicket.ui.components.layout.bs3.col.RepeatingViewWithDynamicallyVisibleContent;
import org.apache.isis.viewer.wicket.ui.panels.HasDynamicallyVisibleContent;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;

public class Row extends PanelAbstract<EntityModel> implements HasDynamicallyVisibleContent {

    private static final long serialVersionUID = 1L;

    private static final String ID_ROW_CONTENTS = "rowContents";

    private final BS3Row bs3Row;

    public Row(
            final String id,
            final EntityModel entityModel) {

        super(id, entityModel);

        bs3Row = (BS3Row) entityModel.getLayoutMetadata();

        buildGui();
    }

    private void buildGui() {

        final RepeatingViewWithDynamicallyVisibleContent rv =
                new RepeatingViewWithDynamicallyVisibleContent(ID_ROW_CONTENTS);

        for(final BS3RowContent bs3RowContent: bs3Row.getCols()) {

            final String id = rv.newChildId();
            final EntityModel entityModelWithHints = getModel().cloneWithLayoutMetadata(bs3RowContent);

            final WebMarkupContainer rowContent;
            if(bs3RowContent instanceof BS3Col) {
                Col col = new Col(id, entityModelWithHints);
                visible = visible || col.isVisible();
                rowContent = col;

            } else if (bs3RowContent instanceof BS3ClearFix) {
                rowContent = new ClearFix(id, entityModelWithHints);
            } else {
                throw new IllegalStateException("Unrecognized implementation of BS3RowContent");
            }

            rv.add(rowContent);
        }

        final WebMarkupContainer panel = this;
        if(visible) {
            Util.appendCssClass(panel, bs3Row, "row");
            panel.add(rv);
        } else {
            Components.permanentlyHide(panel, rv.getId());
        }

    }


    private boolean visible = false;
    @Override
    public boolean isVisible() {
        return visible;
    }


}
