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

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.wicket.Component;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.publishing.spi.PageRenderSubscriber;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.commons.binding.Observable;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.object.ManagedObject;
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
    public void onRendering(final Can<PageRenderSubscriber> objectRenderSubscribers) {
        onRenderingOrRendered(objectRenderSubscribers, (pageRenderSubscriber, bookmarkListSupplier) -> {
            pageRenderSubscriber.onRenderingCollection(bookmarkListSupplier);
            return null;
        });
    }

    @Override
    public void onRendered(final Can<PageRenderSubscriber> objectRenderSubscribers) {
        onRenderingOrRendered(objectRenderSubscribers, (pageRenderSubscriber, bookmarkListSupplier) -> {
            pageRenderSubscriber.onRenderedCollection(bookmarkListSupplier);
            return null;
        });
    }

    private void onRenderingOrRendered(
            final Can<PageRenderSubscriber> objectRenderSubscribers,
            final BiFunction<PageRenderSubscriber, Supplier<List<Bookmark>>, Void> handler) {

        if(objectRenderSubscribers.isEmpty()) {
            return;
        }

        Supplier<List<Bookmark>> listSupplier = () -> {

            final var dataTableInteractive = collectionModel.getObject();
            Observable<Can<ManagedObject>> dataElements = dataTableInteractive.getDataElements();
            Can<ManagedObject> managedObjects = dataElements.getValue();

            final var bookmarks =
                managedObjects.stream()
                .map(ManagedObject::getBookmark)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toUnmodifiableList());
            return bookmarks;
        };

        objectRenderSubscribers
            .forEach(subscriber -> {
                handler.apply(subscriber, listSupplier);
            });
    }

}
