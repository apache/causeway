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

import java.util.Comparator;
import java.util.function.Predicate;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.ui.CollectionContentsAsFactory;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;

import lombok.experimental.UtilityClass;

@UtilityClass
class _Util {

    IModel<String> nameFor(final ComponentFactory componentFactory) {
        return _Casts.castTo(CollectionContentsAsFactory.class, componentFactory)
            .map(CollectionContentsAsFactory::getTitleLabel)
            .orElseGet(()->Model.of(componentFactory.getName()));
    }

    IModel<String> cssClassFor(final ComponentFactory componentFactory, final Label viewIcon) {
        return _Casts.castTo(CollectionContentsAsFactory.class, componentFactory)
            .map(CollectionContentsAsFactory::getCssClass)
            .map(cssClass->{
                viewIcon.setDefaultModelObject("");
                viewIcon.setEscapeModelStrings(true);
                return cssClass;
            })
            .orElseGet(()->{
                // Small hack: if there is no specific CSS class then we assume that background-image is used
                // the span.ViewItemLink should have some content to show it
                // FIX: find a way to do this with CSS (width and height don't seems to help)
                viewIcon.setDefaultModelObject("&#160;&#160;&#160;&#160;&#160;");
                viewIcon.setEscapeModelStrings(false);
                return Model.of(_Strings.asLowerDashed.apply(componentFactory.getName()));
            });
    }

    int orderOfAppearanceInUiDropdownFor(final ComponentFactory componentFactory) {
        return componentFactory instanceof CollectionContentsAsFactory
                ? ((CollectionContentsAsFactory) componentFactory).orderOfAppearanceInUiDropdown()
                : Integer.MAX_VALUE;
    }

    boolean isPageReloadRequiredOnTableViewActivation(final ComponentFactory componentFactory) {
        return componentFactory instanceof CollectionContentsAsFactory
                ? ((CollectionContentsAsFactory) componentFactory).isPageReloadRequiredOnTableViewActivation()
                : false;
    }

    Predicate<? super ComponentFactory> filterTablePresentations() {
        return f->f.getComponentType() == UiComponentType.COLLECTION_CONTENTS;
    }
    Predicate<? super ComponentFactory> filterTableExports() {
        return f->f.getComponentType() == UiComponentType.COLLECTION_CONTENTS_EXPORT;
    }

    Comparator<? super ComponentFactory> orderByOrderOfAppearanceInUiDropdown() {
        return (a, b)->Integer.compare(
                _Util.orderOfAppearanceInUiDropdownFor(a),
                _Util.orderOfAppearanceInUiDropdownFor(b));
    }



}
