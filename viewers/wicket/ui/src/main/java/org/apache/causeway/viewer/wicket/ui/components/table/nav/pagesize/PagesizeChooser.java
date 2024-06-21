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
package org.apache.causeway.viewer.wicket.ui.components.table.nav.pagesize;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.causeway.viewer.wicket.ui.components.table.DataTableWithPagesAndFilter;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktLinks;

import lombok.Getter;
import lombok.NonNull;

public class PagesizeChooser extends Panel {
    private static final long serialVersionUID = 1L;

    private static final String ID_PAGESIZE_CHOICE = "pagesizeChoice";
    private static final String ID_PAGESIZE_CHOICES = "pagesizeChoices";

    private static final String ID_VIEW_ITEM_TITLE = "viewItemTitle";
    private static final String ID_VIEW_ITEM_ICON = "viewItemIcon";
    private static final String ID_VIEW_ITEM_CHECKMARK = "viewItemCheckmark"; // indicator for the selected item

    @Getter
    final DataTableWithPagesAndFilter<?, ?> table;

    public PagesizeChooser(final String id, final DataTableWithPagesAndFilter<?, ?> table) {
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
        var choices = List.of(
                new LinkEntry("10", 10L),
                new LinkEntry("25", 25L),
                new LinkEntry("100", 100L),
                new LinkEntry("1000", 1000L),
                new LinkEntry("All", Long.MAX_VALUE));

        Wkt.listViewAdd(this, ID_PAGESIZE_CHOICES, choices, item->{
            var link = Wkt.linkAdd(item, ID_PAGESIZE_CHOICE, target->{
                var linkItem = item.getModelObject();
                table.setItemsPerPage(linkItem.getItemsPerPage());
                table.setPageSizeHintAndBroadcast(target);
                target.add(table);
            });
            // add title and icon to the link
            LinkEntry.addIconAndTitle(item, link);
            Wkt.ajaxEnable(link);
        });

    }

    @lombok.Value
    static class LinkEntry implements Serializable {
        private static final long serialVersionUID = 1L;
        // -- CONSTRUCTION
        final String title;
        final long itemsPerPage;
        final String cssClass = ""; // for future use
        // -- UTILITY
        static void addIconAndTitle(
                final @NonNull ListItem<LinkEntry> item,
                final @NonNull MarkupContainer link) {
            WktLinks.listItemAsDropdownLink(item, link,
                    ID_VIEW_ITEM_TITLE, LinkEntry::nameFor,
                    ID_VIEW_ITEM_ICON, LinkEntry::cssClassFor,
                    null);
        }
        // -- HELPER
        private static IModel<String> nameFor(final LinkEntry linkEntry) {
            return Model.of(linkEntry.getTitle());
        }
        private static IModel<String> cssClassFor(final LinkEntry linkEntry) {
            return Model.of(linkEntry.getCssClass());
        }
    }

}
