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
package org.apache.causeway.viewer.wicket.ui.components.layout.bs.row;

import org.apache.wicket.markup.html.WebMarkupContainer;

import org.apache.causeway.applib.layout.grid.bootstrap.BSClearFix;
import org.apache.causeway.applib.layout.grid.bootstrap.BSCol;
import org.apache.causeway.applib.layout.grid.bootstrap.BSRow;
import org.apache.causeway.applib.layout.grid.bootstrap.BSRowContent;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.ui.components.layout.bs.clearfix.ClearFix;
import org.apache.causeway.viewer.wicket.ui.components.layout.bs.col.Col;
import org.apache.causeway.viewer.wicket.ui.components.layout.bs.col.RepeatingViewWithDynamicallyVisibleContent;
import org.apache.causeway.viewer.wicket.ui.panels.HasDynamicallyVisibleContent;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;

public class Row
extends PanelAbstract<ManagedObject, UiObjectWkt>
implements HasDynamicallyVisibleContent {

    private static final long serialVersionUID = 1L;

    private static final String ID_ROW_CONTENTS = "rowContents";

    private final BSRow bsRow;

    public Row(
            final String id,
            final UiObjectWkt entityModel,
            final BSRow bsRow) {

        super(id, entityModel);

        this.bsRow = bsRow;

        buildGui();
    }

    private void buildGui() {

        final RepeatingViewWithDynamicallyVisibleContent rv =
                new RepeatingViewWithDynamicallyVisibleContent(ID_ROW_CONTENTS);

        for(final BSRowContent bsRowContent: bsRow.getCols()) {

            final String id = rv.newChildId();

            final WebMarkupContainer rowContent;
            if(bsRowContent instanceof BSCol) {

                final BSCol bsCol = (BSCol) bsRowContent;
                final Col col = new Col(id, getModel(), bsCol);

                visible = visible || col.isVisible();
                rowContent = col;

            } else if (bsRowContent instanceof BSClearFix) {
                final BSClearFix bsClearFix = (BSClearFix) bsRowContent;
                rowContent = new ClearFix(id, getModel(), bsClearFix);
            } else {
                throw new IllegalStateException("Unrecognized implementation of BSRowContent");
            }

            rv.add(rowContent);
        }

        final WebMarkupContainer panel = this;
        if(visible) {
            Wkt.cssAppend(panel, "row");
            Wkt.cssAppend(panel, bsRow.getCssClass());
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
