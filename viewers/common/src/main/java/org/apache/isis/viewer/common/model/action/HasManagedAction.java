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

import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.common.model.decorator.disable.DisablingUiModel;
import org.apache.isis.viewer.common.model.decorator.icon.FontAwesomeUiModel;

import lombok.val;

@FunctionalInterface
public interface HasManagedAction {

    ManagedAction getManagedAction();

    default ObjectAction getAction() {
        return getManagedAction().getAction();
    }

    default String getFriendlyName() {
        return getManagedAction().getFriendlyName();
    }

    default Optional<String> getDescription() {
        return getManagedAction().getDescription();
    }

    default Optional<FontAwesomeUiModel> getFontAwesomeUiModel() {
        val managedAction = getManagedAction();
        return FontAwesomeUiModel.of(ObjectAction.Util.cssClassFaFactoryFor(
                managedAction.getAction(),
                managedAction.getOwner()));
    }

    default String getFeatureIdentifierForCss() {
        val identifier = getAction().getFeatureIdentifier();
        return identifier.getLogicalType().getLogicalTypeName().replace(".","-")
                + "-"
                + identifier.getMemberLogicalName();
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
        final Optional<String> usabiltiyVeto = getManagedAction()
                .checkUsability()
                .map(veto->veto.getReason());
        return DisablingUiModel.of(usabiltiyVeto.isPresent(), usabiltiyVeto.orElse(null)) ;
    }

}
