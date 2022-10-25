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
package org.apache.causeway.viewer.wicket.model.links;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.wicket.ajax.markup.html.AjaxLink;

import org.apache.causeway.applib.annotation.ActionLayout.Position;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.commons.model.action.HasManagedAction;
import org.apache.causeway.viewer.commons.model.mixin.HasUiComponent;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class LinkAndLabel
implements
    HasUiComponent<AjaxLink<ManagedObject>>,
    HasManagedAction,
    Serializable {

    private static final long serialVersionUID = 1L;

    public static LinkAndLabel of(
            final ActionModel actionModel,
            final ActionLinkUiComponentFactoryWkt uiComponentFactory) {
        _Assert.assertNotNull(actionModel.getAction(), "LinkAndLabel requires an Action");
        return new LinkAndLabel(actionModel, uiComponentFactory);
    }

    @Getter private final ActionModel actionModel;
    protected final ActionLinkUiComponentFactoryWkt uiComponentFactory;

    @Override
    public ManagedAction getManagedAction() {
        return actionModel.getManagedAction();
    }

    @Override
    public  ObjectAction getAction() {
        return actionModel.getAction();
    }

    /**
     * used when explicitly named (eg. menu bar layout file), otherwise {@code null}
     */
    @Getter private String named;

    // implements HasUiComponent<T>
    @Getter(lazy = true, onMethod_ = {@Override})
    private final AjaxLink<ManagedObject> uiComponent = uiComponentFactory
        .newActionLinkUiComponent(actionModel);

    @Override
    public String toString() {
        return Optional.ofNullable(named).orElse("") +
                " ~ " + getAction().getFeatureIdentifier().getFullIdentityString();
    }

    // -- RULE CHECKING SHORTCUTS

    public boolean isVisible() {
        return actionModel.getVisibilityConsent().isAllowed();
    }

    public boolean isEnabled() {
        return actionModel.getUsabilityConsent().isAllowed();
    }

    // -- UTILITY

    public static Predicate<LinkAndLabel> isPositionedAt(final Position panel) {
        return HasManagedAction.isPositionedAt(panel);
    }

    public boolean isRenderOutlined() {
        return isPositionedAt(Position.BELOW)
                .or(isPositionedAt(Position.RIGHT))
                        .test(this);
    }

}
