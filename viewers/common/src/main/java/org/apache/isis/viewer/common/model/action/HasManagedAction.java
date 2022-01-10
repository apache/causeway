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
package org.apache.isis.viewer.common.model.action;

import java.util.Optional;
import java.util.function.Predicate;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.object.bookmarkpolicy.BookmarkPolicyFacet;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.common.model.decorator.disable.DisablingUiModel;
import org.apache.isis.viewer.common.model.decorator.icon.FontAwesomeUiModel;

import lombok.val;

/**
 * Functional UI mixin for {@link ManagedAction}.
 */
@FunctionalInterface
public interface HasManagedAction {

    ManagedAction getManagedAction();

    default ObjectAction getAction() {
        return getManagedAction().getAction();
    }

    /**
     * Action's owner.
     *
     * @apiNote for mixins this is not the target to use on mixin actions
     * instead the logic of resolving the target for action invocation is
     * encapsulated within the {@link ActionInteractionHead}
     */
    default ManagedObject getActionOwner() {
        return getManagedAction().getOwner();
    }

    /**
     * Action's friendly (translated) name.
     */
    default String getFriendlyName() {
        return getManagedAction().getFriendlyName();
    }

    default Optional<String> getDescription() {
        return getManagedAction().getDescription();
    }

    default boolean hasParameters() {
        return getAction().getParameterCount() > 0;
    }

    /**
     * Bookmarkable if the {@link ObjectAction action} has a {@link BookmarkPolicyFacet bookmark} policy
     * of {@link BookmarkPolicy#AS_ROOT root}, and has safe {@link ObjectAction#getSemantics() semantics}.
     */
    default boolean isBookmarkable() {
        val action = getAction();
        return action.getSemantics().isSafeInNature()
                && action.lookupFacet(BookmarkPolicyFacet.class)
                    .map(BookmarkPolicyFacet::value)
                    .map(bookmarkPolicy -> bookmarkPolicy == BookmarkPolicy.AS_ROOT)
                    .orElse(false);
    }

    default Identifier getFeatureIdentifier() {
        return getAction().getFeatureIdentifier();
    }

    // -- UI SPECIFICS

    default Optional<FontAwesomeUiModel> getFontAwesomeUiModel() {
        val managedAction = getManagedAction();
        return FontAwesomeUiModel.of(ObjectAction.Util.cssClassFaFactoryFor(
                managedAction.getAction(),
                managedAction.getOwner()));
    }

    default Optional<String> getAdditionalCssClass() {
        return getAction().lookupFacet(CssClassFacet.class)
                .map(cssClassFacet->cssClassFacet.cssClass(getManagedAction().getOwner()));
    }

    default ActionLayout.Position getPosition() {
        return ObjectAction.Util.actionLayoutPositionOf(getAction());
    }

    public static <T extends HasManagedAction> Predicate<T> isPositionedAt(
            final ActionLayout.Position position) {
        return a -> a.getPosition() == position;
    }

    default Optional<DisablingUiModel> getDisableUiModel() {
        return DisablingUiModel.of(getManagedAction().checkUsability()) ;
    }

}
