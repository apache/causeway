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
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

import org.apache.wicket.model.IModel;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.ui.CollectionContentsAsFactory;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.app.registry.ComponentFactoryKey;

record CollectionPresentationChoice(
    ComponentFactoryKey factoryKey,
    String label,
    int orderOfAppearanceInUiDropdown,
    boolean isPageReloadRequiredOnTableViewActivation,
    String cssClass)
implements Serializable {

    static CollectionPresentationChoice of(final ComponentFactory componentFactory) {
        return componentFactory instanceof CollectionContentsAsFactory choice
            ? new CollectionPresentationChoice(
                new ComponentFactoryKey(componentFactory),
                /*label*/
                Optional.ofNullable(choice.getTitleLabel())
                    .map(IModel::getObject)
                    .orElseGet(()->componentFactory.getName()),
                choice.orderOfAppearanceInUiDropdown(),
                choice.isPageReloadRequiredOnTableViewActivation(),
                /*cssClass*/
                Optional.ofNullable(choice.getCssClass())
                    .map(IModel::getObject)
                    .orElseGet(()->_Strings.asLowerDashed.apply(componentFactory.getName())))
            : new CollectionPresentationChoice(
                new ComponentFactoryKey(componentFactory),
                    componentFactory.getName(),
                    Integer.MAX_VALUE,
                    false,
                    null);
    }

    static Comparator<? super CollectionPresentationChoice> orderByOrderOfAppearanceInUiDropdown() {
        return (a, b)->Integer.compare(
            a.orderOfAppearanceInUiDropdown(),
            b.orderOfAppearanceInUiDropdown());
    }

    boolean isPresenter() {
        return factoryKey().componentType() == UiComponentType.COLLECTION_CONTENTS;
    }

    boolean isExporter() {
        return factoryKey().componentType() == UiComponentType.COLLECTION_CONTENTS_EXPORT;
    }

    String id() {
        return factoryKey.id();
    }

    ComponentFactory componentFactory() {
        return factoryKey.componentFactory();
    }

    @Override
    public final boolean equals(final Object obj) {
        return obj instanceof CollectionPresentationChoice other
            ? Objects.equals(this.id(), other.id())
            : false;
    }

    @Override
    public final int hashCode() {
        return id().hashCode();
    }

}