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
package org.apache.isis.viewer.wicket.ui.components.layout.bs3;

import java.util.Optional;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.repeater.RepeatingView;

import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Grid;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Row;
import org.apache.isis.core.metamodel.facets.object.grid.GridFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionMixedIn;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.components.layout.bs3.row.Row;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.val;

public class BS3GridPanel
extends PanelAbstract<ManagedObject, EntityModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_ROWS = "rows";

    private final BS3Grid bs3Page;

    public static Optional<BS3GridPanel> extraContentForMixin(final String id, final ActionModel actionModel) {
        final ObjectAction action = actionModel.getAction();
        if(action instanceof ObjectActionMixedIn) {
            final ObjectActionMixedIn actionMixedIn = (ObjectActionMixedIn) action;
            final ObjectSpecification mixinSpec = actionMixedIn.getMixinType();
            if(mixinSpec.isViewModel()) {
                val commonContext = actionModel.getCommonContext();
                final ManagedObject targetAdapterForMixin = action.realTargetAdapter(actionModel.getActionOwner());
                final GridFacet gridFacet = mixinSpec.getFacet(GridFacet.class);
                final Grid gridForMixin = gridFacet.getGrid(targetAdapterForMixin);
                if(gridForMixin instanceof BS3Grid) {
                    final BS3Grid bs3Grid = (BS3Grid) gridForMixin;
                    final EntityModel entityModelForMixin =
                            EntityModel.ofAdapter(commonContext, targetAdapterForMixin);
                    return Optional.of(new BS3GridPanel(id, entityModelForMixin, bs3Grid));
                }
            }
        }
        return Optional.empty();
    }


    public BS3GridPanel(final String id, final EntityModel entityModel, final BS3Grid bs3Grid) {
        super(id, entityModel);
        this.bs3Page = bs3Grid;
        buildGui();
    }

    private void buildGui() {

        Wkt.cssAppend(this, bs3Page.getCssClass());

        final RepeatingView rv = new RepeatingView(ID_ROWS);

        for(final BS3Row bs3Row: this.bs3Page.getRows()) {
            final String id = rv.newChildId();
            final WebMarkupContainer row = new Row(id, getModel(), bs3Row);
            rv.add(row);
        }
        add(rv);
    }

}
