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

package org.apache.isis.viewer.wicket.ui.components.bookmarkedpages;

import com.google.inject.Inject;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;
import org.apache.isis.viewer.wicket.model.models.BookmarkedPagesModel;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Links;

public class BookmarkedPagesPanel extends PanelAbstract<BookmarkedPagesModel> {

    private static final long serialVersionUID = 1L;
    
    private static final String BOOKMARKED_PAGE_LINK = "bookmarkedPageLink";
    private static final String BOOKMARKED_PAGE_ITEM = "bookmarkedPageItem";
    private static final String BOOKMARKED_PAGE_TITLE = "bookmarkedPageTitle";

    @Inject
    private PageClassRegistry pageClassRegistry;

    public BookmarkedPagesPanel(final String id, final BookmarkedPagesModel bookmarkedPagesModel) {
        super(id, bookmarkedPagesModel);
        buildGui();
    }

    private void buildGui() {
        final BookmarkedPagesModel bookmarkedPagesModel = getModel();
        final ListView<PageParameters> listView = new ListView<PageParameters>(BOOKMARKED_PAGE_ITEM, bookmarkedPagesModel) {

            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(ListItem<PageParameters> item) {
                final PageParameters pageParameters = item.getModelObject();
                
                final PageType pageType = PageParameterNames.PAGE_TYPE.getEnumFrom(pageParameters, PageType.class);
                final Class<? extends Page> pageClass = pageClassRegistry.getPageClass(pageType);
                
                final AbstractLink link = Links.newBookmarkablePageLink(BOOKMARKED_PAGE_LINK, pageParameters, pageClass);
                link.add(new Label(BOOKMARKED_PAGE_TITLE, BookmarkedPagesModel.titleFrom(pageParameters)));
                item.add(link);
                link.setEnabled(!bookmarkedPagesModel.isCurrent(pageParameters));
            }
        };
        add(listView);
    }
}
