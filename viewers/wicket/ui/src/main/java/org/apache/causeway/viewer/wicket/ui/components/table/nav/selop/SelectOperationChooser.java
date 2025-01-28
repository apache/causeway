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
package org.apache.causeway.viewer.wicket.ui.components.table.nav.selop;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.wicket.model.tableoption.SelectOperationChoice;
import org.apache.causeway.viewer.wicket.ui.components.table.DataTableWithPagesAndFilter;
import org.apache.causeway.viewer.wicket.ui.components.widgets.links.AjaxLinkNoPropagate;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktLinks;
import org.apache.causeway.viewer.wicket.ui.util.WktTooltips;

import lombok.Getter;
import org.jspecify.annotations.NonNull;

public class SelectOperationChooser extends Panel {
    private static final long serialVersionUID = 1L;

    private static final String ID_SELECT_OPERATION_BUTTON = "selectOperationButton";
    private static final String ID_SELECT_OPERATION_CHOICE = "selectOperationChoice";
    private static final String ID_SELECT_OPERATION_CHOICES = "selectOperationChoices";

    private static final String ID_VIEW_ITEM_TITLE = "viewItemTitle";
    private static final String ID_VIEW_ITEM_ICON = "viewItemIcon";

    @Getter final DataTableWithPagesAndFilter<?, ?> table;

    public SelectOperationChooser(final String id, final DataTableWithPagesAndFilter<?, ?> table) {
        super(id);
        this.table = table;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        buildGui();
    }

    // -- HELPER

    private void buildGui() {

        var selectOperationChoices = table.getSelectOperationChoices();

        var button = Wkt.add(this, new Button(ID_SELECT_OPERATION_BUTTON));

        Wkt.listViewAdd(this, ID_SELECT_OPERATION_CHOICES, selectOperationChoices, item->{
            var link = Wkt.linkAdd(item, ID_SELECT_OPERATION_CHOICE, target->{
                var selectOperationChoice = item.getModelObject();
                table.executeSelectOperation(selectOperationChoice);
                target.add(table);
            });
            // add title and icon to the link
            addIconAndTitle(item, link);

            Wkt.ajaxEnable(link);
        });

        // hide the drop-down menu, if empty
        if(selectOperationChoices.isEmpty()) {
            this.setVisible(false);
        } else {
            WktTooltips.addTooltip(button, translate("Select operations"));
        }

    }

    private String translate(final String text) {
        return MetaModelContext.translationServiceOrFallback()
                .translate(TranslationContext.named("Table"), text);
    }

    private static void addIconAndTitle(
            final @NonNull ListItem<SelectOperationChoice> item,
            final @NonNull AjaxLinkNoPropagate link) {
        WktLinks.listItemAsDropdownLink(item, link,
                ID_VIEW_ITEM_TITLE, pagesizeChoice->Model.of(pagesizeChoice.translatedTitle()),
                ID_VIEW_ITEM_ICON, pagesizeChoice->FontAwesomeLayers.fromQuickNotation(pagesizeChoice.faIconCss()));
    }

}
