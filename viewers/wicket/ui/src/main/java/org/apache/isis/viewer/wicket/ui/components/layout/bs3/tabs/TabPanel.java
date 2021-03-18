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
package org.apache.isis.viewer.wicket.ui.components.layout.bs3.tabs;

import org.apache.wicket.markup.html.WebMarkupContainer;

import org.apache.isis.applib.layout.grid.bootstrap3.BS3Row;
import org.apache.isis.applib.layout.grid.bootstrap3.BS3Tab;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.wicket.model.hints.HasUiHintDisambiguator;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.components.layout.bs3.Util;
import org.apache.isis.viewer.wicket.ui.components.layout.bs3.col.RepeatingViewWithDynamicallyVisibleContent;
import org.apache.isis.viewer.wicket.ui.components.layout.bs3.row.Row;
import org.apache.isis.viewer.wicket.ui.panels.HasDynamicallyVisibleContent;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;

public class TabPanel 
extends PanelAbstract<ManagedObject, EntityModel>
implements HasUiHintDisambiguator, HasDynamicallyVisibleContent {

    private static final long serialVersionUID = 1L;

    private static final String ID_TAB_PANEL = "tabPanel";
    private static final String ID_ROWS = "rows";

    private final BS3Tab bs3Tab;

    public TabPanel(String id, final EntityModel model, final BS3Tab bs3Tab) {
        this(id, model, bs3Tab, null);
    }

    public TabPanel(String id, final EntityModel model, final BS3Tab bs3Tab, final RepeatingViewWithDynamicallyVisibleContent repeatingViewWithDynamicallyVisibleContent) {
        super(id);

        this.bs3Tab = bs3Tab;
        buildGui(model, bs3Tab, repeatingViewWithDynamicallyVisibleContent);
    }

    /**
     * when tabs are rendered, they don't distinguish within the path hierarchy: even if on different tabs, the first
     * panel will have the same Wicket path hierarchy.  This property allows us to distinguish.
     */
    @Override
    public String getHintDisambiguator() {
        return bs3Tab.getName();
    }

    protected void buildGui(final EntityModel model, final BS3Tab bs3Tab, final RepeatingViewWithDynamicallyVisibleContent rvIfAny) {

        final WebMarkupContainer div = new WebMarkupContainer(ID_TAB_PANEL);

        final RepeatingViewWithDynamicallyVisibleContent rv = rvIfAny != null ? rvIfAny : newRows(model, bs3Tab);
        div.add(rv);
        visible = visible || rv.isVisible();

        final WebMarkupContainer panel = this;
        if(visible) {
            Util.appendCssClassIfRequired(panel, bs3Tab);
            panel.add(div);
        } else {
            Components.permanentlyHide(panel, div.getId());
        }

    }

    public static RepeatingViewWithDynamicallyVisibleContent newRows(final EntityModel model, final BS3Tab bs3Tab) {
        final RepeatingViewWithDynamicallyVisibleContent rv = new RepeatingViewWithDynamicallyVisibleContent(ID_ROWS);

        for(final BS3Row bs3Row: bs3Tab.getRows()) {
            final String newChildId = rv.newChildId();
            final Row row = new Row(newChildId, model, bs3Row);
            rv.add(row);
        }
        return rv;
    }

    private boolean visible = false;
    @Override
    public boolean isVisible() {
        return visible;
    }
}
