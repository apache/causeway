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
package org.apache.causeway.viewer.wicket.ui.components.bookmarkedpages;

import java.util.List;
import java.util.Optional;

import jakarta.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptReferenceHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.util.string.Strings;

import org.apache.causeway.applib.exceptions.unrecoverable.ObjectNotFoundException;
import org.apache.causeway.viewer.wicket.model.models.BookmarkTreeNode;
import org.apache.causeway.viewer.wicket.model.models.BookmarkedPagesModel;
import org.apache.causeway.viewer.wicket.model.models.PageType;
import org.apache.causeway.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;
import org.apache.causeway.viewer.wicket.ui.util.WktLinks;

class BookmarkedPagesPanel
extends PanelAbstract<List<BookmarkTreeNode>, BookmarkedPagesModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_BOOKMARK_LIST = "bookmarkList";
    private static final String ID_BOOKMARKS_HELP_TEXT = "helpText";
    private static final String ID_BOOKMARKED_PAGE_LINK = "bookmarkedPageLink";
    private static final String ID_CLEAR_BOOKMARK_LINK = "clearBookmarkLink";
    private static final String ID_BOOKMARKED_PAGE_ITEM = "bookmarkedPageItem";
    private static final String ID_BOOKMARKED_PAGE_TITLE = "bookmarkedPageTitle";

    private static final String ID_BOOKMARKED_PAGE_ICON = "bookmarkedPageImage";
    private static final String ID_BOOKMARKED_PAGE_ICON_FA = "bookmarkedPageFontAwesome";

    private static final String ID_CLEAR_ALL = "clearBookmarks";

    private static final JavaScriptResourceReference SLIDE_PANEL_JS = new JavaScriptResourceReference(BookmarkedPagesPanel.class, "slide-panel.js");

    @Inject
    private PageClassRegistry pageClassRegistry;

    public BookmarkedPagesPanel(final String id, final BookmarkedPagesModel bookmarkedPagesModel) {
        super(id, bookmarkedPagesModel);
        buildGui();
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
    }

    private BookmarkedPagesModel bookmarkedPagesModel() {
        return getModel();
    }

    private void buildGui() {

        var bookmarkedPagesModel = bookmarkedPagesModel();

        addOrReplace(helpTextLabel(bookmarkedPagesModel
            .map(x->bookmarkedPagesModel.isEmpty()
                ? "You have no bookmarks!"
                : "")));

        final WebMarkupContainer container = new WebMarkupContainer(ID_BOOKMARK_LIST) {
            private static final long serialVersionUID = 1L;
            @Override
            public void renderHead(final IHeaderResponse response) {
                response.render(CssHeaderItem.forReference(new CssResourceReference(BookmarkedPagesPanel.class, "BookmarkedPagesPanel.css")));
                response.render(JavaScriptReferenceHeaderItem.forReference(SLIDE_PANEL_JS));
            }
        };
        // allow to be updated by AjaxLink
        Wkt.ajaxEnable(this);
        Wkt.ajaxEnable(container);
        add(container);

        var clearAllBookmarksLink = Wkt.linkAdd(this, ID_CLEAR_ALL, target->{
            BookmarkedPagesPanel.this.getModel().clear();
            setEnabled(false);
            target.add(container, this);
        });
        clearAllBookmarksLink.setOutputMarkupId(true);

        if(getModel().isEmpty()) {
            clearAllBookmarksLink.setVisible(false);
        }

        Wkt.listViewAdd(container, ID_BOOKMARKED_PAGE_ITEM, bookmarkedPagesModel, item->{
            final BookmarkTreeNode bookmarkNode = item.getModelObject();
            try {
                var clearBookmarkLink = Wkt.linkAdd(item, ID_CLEAR_BOOKMARK_LINK, target->{
                    bookmarkedPagesModel.remove(bookmarkNode);
                    // [CAUSEWAY-3881] does not work, that is, it does not hide the "Clear all' link.
                    // Instead, we keep the link and its associated behavior setup up, such that we don't
                    // run into an exception later, where Wicket cannot find the behavior.
                    // ...
                    //if(bookmarkedPagesModel.isEmpty()) {
                    //    WktComponents.permanentlyHide(BookmarkedPagesPanel.this, ID_CLEAR_ALL);
                    //}
                    target.add(container, clearAllBookmarksLink);
                });

                if(bookmarkNode.getDepth() == 0) {
                    Wkt.cssAppend(clearBookmarkLink, "clearBookmark");
                } else {
                    clearBookmarkLink.setEnabled(true);
                }

                var link = Wkt.add(item, WktLinks.newBookmarkablePageLink(ID_BOOKMARKED_PAGE_LINK,
                                bookmarkNode.getPageParameters(),
                                pageClassRegistry.getPageClass(PageType.DOMAIN_OBJECT)));

                bookmarkNode.eitherIconOrFaClass()
                .accept(
                        iconResourceRef->{
                            Optional.ofNullable(iconResourceRef)
                            .ifPresent(icon->
                                Wkt.imageAddCachable(link, ID_BOOKMARKED_PAGE_ICON, icon));
                            WktComponents.permanentlyHide(link, ID_BOOKMARKED_PAGE_ICON_FA);
                        },
                        faLayers->{
                            WktComponents.permanentlyHide(link, ID_BOOKMARKED_PAGE_ICON);
                            Wkt.faIconLayersAdd(link, ID_BOOKMARKED_PAGE_ICON_FA, faLayers);
                        });

                Wkt.labelAdd(link, ID_BOOKMARKED_PAGE_TITLE, bookmarkNode.getTitle());

//XXX seems broken when there is only one bookmark entry;
// an alternative idea would be to render the item differently eg. bold, but don't disable it
//                    if(bookmarkedPagesModel.isCurrent(pageParameters)) {
//                        item.add(new CssClassAppender("disabled"));
//                    }
                Wkt.cssAppend(item, "bookmarkDepth" + bookmarkNode.getDepth());
            } catch(ObjectNotFoundException ex) {
                // ignore
                // this is a partial fix for an infinite redirect loop.
                // should be a bit smarter here, though; see CAUSEWAY-596.
            }
        });
    }

    protected Component helpTextLabel(final IModel<String> helpTextModel) {
        return new Label(ID_BOOKMARKS_HELP_TEXT, helpTextModel) {
            private static final long serialVersionUID = 1;

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(!Strings.isEmpty(getDefaultModelObjectAsString()));
                this.setOutputMarkupPlaceholderTag(true);
            }
        };
    }

}
