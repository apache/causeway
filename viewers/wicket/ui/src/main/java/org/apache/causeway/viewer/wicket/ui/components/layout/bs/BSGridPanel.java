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
package org.apache.causeway.viewer.wicket.ui.components.layout.bs;

import java.util.Optional;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.apache.causeway.applib.layout.grid.bootstrap.BSGrid;
import org.apache.causeway.applib.layout.grid.bootstrap.BSRow;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.ui.components.layout.bs.row.Row;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

public class BSGridPanel
extends PanelAbstract<ManagedObject, UiObjectWkt> {

    private static final long serialVersionUID = 1L;

    private static final String ID_ROWS = "rows";

    private final BSGrid bsPage;

    public static Optional<BSGridPanel> extraContentForMixin(final String id, final ActionModel actionModel) {

        //[CAUSEWAY-3210] EntityModel.ofAdapter(commonContext, targetAdapterForMixin); not supported for mixins
        return Optional.empty();

//        val action = actionModel.getAction();
//        if(action.isMixedIn()) {
//
//            val mixinSpec = ((MixedInMember)action).getMixinType();
//            val targetAdapterForMixin = action.realTargetAdapter(actionModel.getActionOwner());
//
//            // if we can bootstrap a grid, use it
//            return Facets.bootstrapGrid(mixinSpec, targetAdapterForMixin)
//            .map(bsGrid->{
//                val commonContext = actionModel.getMetaModelContext();
//                val entityModelForMixin =
//                        EntityModel.ofAdapter(commonContext, targetAdapterForMixin);
//                return new BSGridPanel(id, entityModelForMixin, bsGrid);
//            });
//        }
//        return Optional.empty();
    }


    public BSGridPanel(final String id, final UiObjectWkt entityModel, final BSGrid bsGrid) {
        super(id, entityModel);
        this.bsPage = bsGrid;
        buildGui();
    }

    private void buildGui() {

        Wkt.cssAppend(this, bsPage.getCssClass());

        final RepeatingView rv = new RepeatingView(ID_ROWS);

        for(final BSRow bsRow: this.bsPage.getRows()) {
            final String id = rv.newChildId();
            final WebMarkupContainer row = new Row(id, getModel(), bsRow);
            rv.add(row);
        }
        add(rv);
    }

}
