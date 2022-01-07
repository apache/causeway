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
package org.apache.isis.viewer.wicket.model.links;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.wicket.markup.html.link.AbstractLink;

import org.apache.isis.applib.annotations.ActionLayout.Position;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.viewer.common.model.action.HasManagedAction;
import org.apache.isis.viewer.common.model.mixin.HasUiComponent;
import org.apache.isis.viewer.wicket.model.models.ActionModel;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class LinkAndLabel
implements
    HasUiComponent<AbstractLink>,
    HasManagedAction,
    Serializable {

    private static final long serialVersionUID = 1L;

    public static LinkAndLabel of(
            final ActionModel actionModel,
            final ActionLinkUiComponentFactoryWkt uiComponentFactory) {
        return new LinkAndLabel(actionModel, uiComponentFactory);
    }

    private final ActionModel actionModel;
    protected final ActionLinkUiComponentFactoryWkt uiComponentFactory;

    @Override
    public ManagedAction getManagedAction() {
        return actionModel.getManagedAction();
    }

    /**
     * used when explicitly named (eg. menu bar layout file), otherwise {@code null}
     */
    @Getter private String named;

    // implements HasUiComponent<T>
    @Getter(lazy = true, onMethod_ = {@Override})
    private final AbstractLink uiComponent = uiComponentFactory
        .newActionLinkUiComponent(actionModel);

    @Override
    public String toString() {
        return Optional.ofNullable(named).orElse("") +
                " ~ " + actionModel.getAction().getFeatureIdentifier().getFullIdentityString();
    }

    // -- VISIBILITY

    public boolean isVisible() {

        // check whether action owner type is hidden
        if (actionModel.getActionOwner().getSpecification().isHidden()) {
            return false;
        }
        val visibilityVeto = getManagedAction().checkVisibility();
        return visibilityVeto.isEmpty();
    }


    // -- UTILITY

    public static Predicate<LinkAndLabel> isPositionedAt(final Position panel) {
        return HasManagedAction.isPositionedAt(panel);
    }

}
