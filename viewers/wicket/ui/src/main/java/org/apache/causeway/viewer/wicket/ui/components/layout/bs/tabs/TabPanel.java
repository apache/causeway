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
package org.apache.causeway.viewer.wicket.ui.components.layout.bs.tabs;

import org.apache.wicket.markup.html.WebMarkupContainer;

import org.apache.causeway.applib.layout.grid.bootstrap.BSRow;
import org.apache.causeway.applib.layout.grid.bootstrap.BSTab;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.wicket.model.hints.HasUiHintDisambiguator;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.ui.components.layout.bs.col.RepeatingViewWithDynamicallyVisibleContent;
import org.apache.causeway.viewer.wicket.ui.components.layout.bs.row.Row;
import org.apache.causeway.viewer.wicket.ui.panels.HasDynamicallyVisibleContent;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;

public class TabPanel
extends PanelAbstract<ManagedObject, UiObjectWkt>
implements HasUiHintDisambiguator, HasDynamicallyVisibleContent {

    private static final long serialVersionUID = 1L;

    private static final String ID_TAB_PANEL = "tabPanel";
    private static final String ID_ROWS = "rows";

    private final BSTab bsTab;

    public TabPanel(final String id, final UiObjectWkt model, final BSTab bsTab) {
        this(id, model, bsTab, null);
    }

    public TabPanel(final String id, final UiObjectWkt model, final BSTab bsTab, final RepeatingViewWithDynamicallyVisibleContent repeatingViewWithDynamicallyVisibleContent) {
        super(id);

        this.bsTab = bsTab;
        buildGui(model, bsTab, repeatingViewWithDynamicallyVisibleContent);
    }

    /**
     * when tabs are rendered, they don't distinguish within the path hierarchy: even if on different tabs, the first
     * panel will have the same Wicket path hierarchy.  This property allows us to distinguish.
     */
    @Override
    public String getHintDisambiguator() {
        return bsTab.getName();
    }

    protected void buildGui(final UiObjectWkt model, final BSTab bsTab, final RepeatingViewWithDynamicallyVisibleContent rvIfAny) {

        final WebMarkupContainer div = new WebMarkupContainer(ID_TAB_PANEL);

        final RepeatingViewWithDynamicallyVisibleContent rv = rvIfAny != null ? rvIfAny : newRows(model, bsTab);
        div.add(rv);
        visible = visible || rv.isVisible();

        final WebMarkupContainer panel = this;
        if(visible) {
            Wkt.cssAppend(panel, bsTab.getCssClass());
            panel.add(div);
        } else {
            WktComponents.permanentlyHide(panel, div.getId());
        }

    }

    public static RepeatingViewWithDynamicallyVisibleContent newRows(final UiObjectWkt model, final BSTab bsTab) {
        final RepeatingViewWithDynamicallyVisibleContent rv = new RepeatingViewWithDynamicallyVisibleContent(ID_ROWS);

        for(final BSRow bsRow: bsTab.getRows()) {
            final String newChildId = rv.newChildId();
            final Row row = new Row(newChildId, model, bsRow);
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
