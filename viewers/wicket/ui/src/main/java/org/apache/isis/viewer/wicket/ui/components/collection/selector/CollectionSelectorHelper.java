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
package org.apache.isis.viewer.wicket.ui.components.collection.selector;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.springframework.lang.Nullable;

import org.apache.isis.applib.layout.component.CollectionLayoutData;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.facets.collections.collection.defaultview.DefaultViewFacet;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModelParented;
import org.apache.isis.viewer.wicket.model.util.ComponentHintKey;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.CollectionContentsAsAjaxTablePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.multiple.CollectionContentsMultipleViewsPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.unresolved.CollectionContentsHiddenPanelFactory;

import lombok.Getter;
import lombok.val;

public class CollectionSelectorHelper implements Serializable {

    private static final long serialVersionUID = 1L;

    static final String UIHINT_EVENT_VIEW_KEY = EntityCollectionModelParented.HINT_KEY_SELECTED_ITEM;

    private final EntityCollectionModel collectionModel;

    @Getter
    private final List<ComponentFactory> componentFactories;
    private final ComponentHintKey componentHintKey;

    public CollectionSelectorHelper(
            final EntityCollectionModel collectionModel,
            final ComponentFactoryRegistry componentFactoryRegistry) {
        this(collectionModel, componentFactoryRegistry, ComponentHintKey.noop());
    }

    public CollectionSelectorHelper(
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
                ComponentType.COLLECTION_CONTENTS,
                ComponentType.COLLECTION_CONTENTS_EXPORT),
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
     * else the index of {@link org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.CollectionContentsAsAjaxTablePanelFactory ajax table} if present,
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
            DefaultViewFacet defaultViewFacet = collectionModel.getMetaModel().getFacet(DefaultViewFacet.class);
            for (ComponentFactory componentFactory : componentFactories) {
                final String componentName = componentFactory.getName();
                final String viewName = defaultViewFacet.value();
                if (componentName.equalsIgnoreCase(viewName)) {
                    return componentName;
                }
            }
        }

        // else honour @CollectionLayout#renderEagerly
        return hasRenderEagerlyFacet(collectionModel)
                || collectionModel.getVariant().isStandalone()
                    ? CollectionContentsAsAjaxTablePanelFactory.NAME
                    : CollectionContentsHiddenPanelFactory.NAME;

    }

    private static UiHintContainer getUiHintContainer(final Component component) {
        return UiHintContainer.Util.hintContainerOf(component, EntityCollectionModelParented.class);
    }

    private static boolean hasRenderEagerlyFacet(final IModel<?> model) {
        return toParentedEntityCollectionModel(model)
        .map(EntityCollectionModelParented::getMetaModel)
        .map(CollectionSelectorHelper::isRenderEagerly)
        .orElse(false);
    }

    private static boolean isRenderEagerly(final OneToManyAssociation collectionMetaModel) {
        final DefaultViewFacet defaultViewFacet = collectionMetaModel.getFacet(DefaultViewFacet.class);
        return defaultViewFacet != null && Objects.equals(defaultViewFacet.value(), "table");
    }


    private static boolean hasDefaultViewFacet(final IModel<?> model) {
        val entityCollectionModel = toParentedEntityCollectionModel(model).orElse(null);
        if (entityCollectionModel == null) {
            return false;
        }
        final OneToManyAssociation collection = entityCollectionModel.getMetaModel();
        DefaultViewFacet defaultViewFacet = collection.getFacet(DefaultViewFacet.class);
        return defaultViewFacet != null;
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
