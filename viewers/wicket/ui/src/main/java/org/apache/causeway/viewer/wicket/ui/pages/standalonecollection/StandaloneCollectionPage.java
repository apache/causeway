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
package org.apache.causeway.viewer.wicket.ui.pages.standalonecollection;

import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.publishing.spi.PageRenderSubscriber;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.wicket.Component;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;

import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModelStandalone;
import org.apache.causeway.viewer.wicket.model.util.PageParameterUtils;
import org.apache.causeway.viewer.wicket.ui.pages.PageAbstract;

/**
 * Web page representing an action invocation.
 */
@AuthorizeInstantiation(UserMemento.AUTHORIZED_USER_ROLE)
public class StandaloneCollectionPage extends PageAbstract {

    private static final long serialVersionUID = 1L;

    private final EntityCollectionModelStandalone collectionModel;

    /**
     * For use with {@link Component#setResponsePage(org.apache.wicket.request.component.IRequestablePage)}
     */
    public StandaloneCollectionPage(final EntityCollectionModelStandalone collectionModel) {
        super(PageParameterUtils.newPageParameters(),
                collectionModel.getName(),
                UiComponentType.STANDALONE_COLLECTION);
        this.collectionModel = collectionModel;

        addChildComponents(themeDiv, collectionModel);
        addBookmarkedPages(themeDiv);
    }

    @Override
    public void onRendered(Can<PageRenderSubscriber> objectRenderSubscribers) {

        Supplier<List<Bookmark>> bookmarkSupplier = _Lazy.threadSafe(
                () -> {
                    val bookmarks = new ArrayList<Bookmark>();
                    collectionModel.getObject().getDataElements().getValue().forEach(x -> {
                        x.getBookmark().ifPresent(bookmarks::add);
                    });
                    return bookmarks;
                });

        objectRenderSubscribers
                .forEach(subscriber -> subscriber.onRenderedCollection(bookmarkSupplier));
    }

}
