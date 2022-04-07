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

import org.apache.isis.applib.layout.grid.bootstrap.BSClearFix;
import org.apache.isis.applib.layout.grid.bootstrap.BSCol;
import org.apache.isis.applib.layout.grid.bootstrap.BSRow;
import org.apache.isis.applib.layout.grid.bootstrap.BSRowContent;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.components.layout.bs3.clearfix.ClearFix;
import org.apache.isis.viewer.wicket.ui.components.layout.bs3.col.Col;
import org.apache.isis.viewer.wicket.ui.components.layout.bs3.col.RepeatingViewWithDynamicallyVisibleContent;
import org.apache.isis.viewer.wicket.ui.panels.HasDynamicallyVisibleContent;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.WktComponents;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

public class Row
extends PanelAbstract<ManagedObject, EntityModel>
implements HasDynamicallyVisibleContent {

    private static final long serialVersionUID = 1L;

    private static final String ID_ROW_CONTENTS = "rowContents";

    private final BSRow bs3Row;

    public Row(
            final String id,
            final EntityModel entityModel,
            final BSRow bs3Row) {

        super(id, entityModel);

        this.bs3Row = bs3Row;

        buildGui();
    }

    private void buildGui() {

        final RepeatingViewWithDynamicallyVisibleContent rv =
                new RepeatingViewWithDynamicallyVisibleContent(ID_ROW_CONTENTS);

        for(final BSRowContent bs3RowContent: bs3Row.getCols()) {

            final String id = rv.newChildId();

            final WebMarkupContainer rowContent;
            if(bs3RowContent instanceof BSCol) {

                final BSCol bs3Col = (BSCol) bs3RowContent;
                final Col col = new Col(id, getModel(), bs3Col);

                visible = visible || col.isVisible();
                rowContent = col;

            } else if (bs3RowContent instanceof BSClearFix) {
                final BSClearFix bs3ClearFix = (BSClearFix) bs3RowContent;
                rowContent = new ClearFix(id, getModel(), bs3ClearFix);
            } else {
                throw new IllegalStateException("Unrecognized implementation of BSRowContent");
            }

            rv.add(rowContent);
        }

        final WebMarkupContainer panel = this;
        if(visible) {
            Wkt.cssAppend(panel, "row");
            Wkt.cssAppend(panel, bs3Row.getCssClass());
            panel.add(rv);
        } else {
            WktComponents.permanentlyHide(panel, rv.getId());
        }

    }


    private boolean visible = false;
    @Override
    public boolean isVisible() {
        return visible;
    }


}
