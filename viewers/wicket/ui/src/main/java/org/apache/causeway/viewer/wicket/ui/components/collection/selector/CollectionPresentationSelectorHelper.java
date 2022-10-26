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
package org.apache.causeway.viewer.wicket.ui.components.collection.selector;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.springframework.lang.Nullable;

import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.hints.UiHintContainer;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModelParented;
import org.apache.causeway.viewer.wicket.model.util.ComponentHintKey;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.CollectionContentsAsAjaxTablePanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.multiple.CollectionContentsMultipleViewsPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.unresolved.CollectionContentsHiddenPanelFactory;

import lombok.Getter;

public class CollectionPresentationSelectorHelper implements Serializable {

    private static final long serialVersionUID = 1L;

    static final String UIHINT_EVENT_VIEW_KEY = EntityCollectionModelParented.HINT_KEY_SELECTED_ITEM;

    private final EntityCollectionModel collectionModel;

    @Getter
    private final List<ComponentFactory> componentFactories;
    private final ComponentHintKey componentHintKey;

    public CollectionPresentationSelectorHelper(
            final EntityCollectionModel collectionModel,
            final ComponentFactoryRegistry componentFactoryRegistry) {
        this(collectionModel, componentFactoryRegistry, ComponentHintKey.noop());
    }

    public CollectionPresentationSelectorHelper(
            final EntityCollectionModel collectionModel,
            final ComponentFactoryRegistry componentFactoryRegistry,
            final ComponentHintKey componentHintKey) {
        this.collectionModel = collectionModel;
        this.componentFactories = locateComponentFactories(componentFactoryRegistry);
        this.componentHintKey = componentHintKey != null
                ? componentHintKey
                : ComponentHintKey.noop();
    }

    private List<ComponentFactory> locateComponentFactories(
            final ComponentFactoryRegistry componentFactoryRegistry) {

        final List<ComponentFactory> ajaxFactoriesToEnd = _Lists.newArrayList();

        final List<ComponentFactory> componentFactories = componentFactoryRegistry
        .streamComponentFactories(ImmutableEnumSet.of(
                UiComponentType.COLLECTION_CONTENTS,
                UiComponentType.COLLECTION_CONTENTS_EXPORT),
                collectionModel)
        .filter(componentFactory ->
            componentFactory.getClass() != CollectionContentsMultipleViewsPanelFactory.class)
        .filter(componentFactory -> {
            if(componentFactory instanceof CollectionContentsAsAjaxTablePanelFactory) {
                ajaxFactoriesToEnd.add(componentFactory);
                return false;
            }
            return true;
        })
        .collect(Collectors.toList());

        componentFactories.addAll(ajaxFactoriesToEnd);

        return componentFactories;
    }

    public String honourViewHintElseDefault(final Component component) {
        // honour hints ...
        final UiHintContainer hintContainer = getUiHintContainer(component);
        if (hintContainer != null) {
            String viewStr = hintContainer.getHint(component, UIHINT_EVENT_VIEW_KEY);
            if (viewStr != null) {
                return viewStr;
            }
        }

        // ... else default
        String initialFactory = determineInitialFactory();
        if (hintContainer != null) {
            hintContainer.setHint(component, UIHINT_EVENT_VIEW_KEY, initialFactory);
            // don't broadcast (no AjaxRequestTarget, still configuring initial setup)
        }
        return initialFactory;
    }

    // -- helpers

    /**
     * return the index of {@link CollectionContentsHiddenPanelFactory unresolved panel} if present and not eager loading;
     * else the index of {@link org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.CollectionContentsAsAjaxTablePanelFactory ajax table} if present,
     * otherwise first factory.
     */
    private String determineInitialFactory() {

        // try to load from session, if can
        final Bookmark bookmark = collectionModel.parentedHintingBookmark().orElse(null);
        final String sessionAttribute = componentHintKey.get(bookmark);
        if(sessionAttribute != null) {
            return sessionAttribute;
        }

        // else grid layout hint
        final CollectionLayoutData layoutData = toParentedEntityCollectionModel(collectionModel)
                .map(EntityCollectionModelParented::getLayoutData)
                .orElse(null);

        if(layoutData != null) {
            final String defaultView = layoutData.getDefaultView();
            if(defaultView != null) {
                return defaultView;
            }
        }

        // else @CollectionLayout#defaultView attribute
        if (hasDefaultViewFacet(collectionModel)) {

            final String viewName = Facets.defaultViewName(collectionModel.getMetaModel())
                    .orElseThrow(); // null case guarded by if clause

            for (ComponentFactory componentFactory : componentFactories) {
                final String componentName = componentFactory.getName();
                if (componentName.equalsIgnoreCase(viewName)) {
                    return componentName;
                }
            }
        }

        // else honour @CollectionLayout#renderEagerly
        return hasRenderEagerlySemantics(collectionModel)
                || collectionModel.getVariant().isStandalone()
                    ? CollectionContentsAsAjaxTablePanelFactory.NAME
                    : CollectionContentsHiddenPanelFactory.NAME;

    }

    private static UiHintContainer getUiHintContainer(final Component component) {
        return UiHintContainer.Util.hintContainerOf(component, EntityCollectionModelParented.class);
    }

    private static boolean hasRenderEagerlySemantics(final IModel<?> model) {
        return toParentedEntityCollectionModel(model)
        .map(EntityCollectionModelParented::getMetaModel)
        .map(Facets::defaultViewIsTable)
        .orElse(false);
    }

    private static boolean hasDefaultViewFacet(final IModel<?> model) {
        return toParentedEntityCollectionModel(model)
        .map(EntityCollectionModelParented::getMetaModel)
        .map(Facets::defaultViewIsPresent)
        .orElse(false);
    }

    public ComponentFactory find(final String selected) {
        ComponentFactory componentFactory = doFind(selected);
        if (componentFactory != null) {
            return componentFactory;
        }

        final String fallback = collectionModel.getVariant().isParented()
                ? CollectionContentsHiddenPanelFactory.NAME
                : CollectionContentsAsAjaxTablePanelFactory.NAME;
        componentFactory = doFind(fallback);
        if(componentFactory == null) {
            throw new IllegalStateException(String.format(
                    "Could not locate '%s' (as the fallback collection panel)",
                    fallback));
        }
        return componentFactory;
    }

    private ComponentFactory doFind(final String selected) {
        for (ComponentFactory componentFactory : componentFactories) {
            if(selected.equals(componentFactory.getName())) {
                return componentFactory;
            }
        }
        return null;
    }

    public int lookup(final String view) {
        int i=0;
        for (ComponentFactory componentFactory : componentFactories) {
            if(view.equals(componentFactory.getName())) {
                return i;
            }
            i++;
        }
        return 0;
    }

    // -- HELPER

    private static Optional<EntityCollectionModelParented> toParentedEntityCollectionModel(
            final @Nullable IModel<?> model) {
        if (model instanceof EntityCollectionModelParented) {
            return Optional.of((EntityCollectionModelParented) model);
        }
        return Optional.empty();
    }


}
