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
package org.apache.causeway.viewer.commons.model.decorators;

import java.util.Optional;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.viewer.commons.model.action.HasManagedAction;
import org.apache.causeway.viewer.commons.model.decorators.DisablingDecorator.DisablingDecorationModel;
import org.apache.causeway.viewer.commons.model.decorators.PrototypingDecorator.PrototypingDecorationModel;

import lombok.Builder;
import lombok.experimental.UtilityClass;

/**
 * Decorators for actions that appear in drop-down menus and as buttons.
 */
@UtilityClass
public class ActionDecorators {

    public enum ActionStyle {
        BUTTON,
        MENU_ITEM;
    }

    public enum VisualRank {
        DEFAULT,
        /**
         * With respect to UI visual hierarchy, actions that appear in the field-set header
         * are ranked higher than those that appear inside a field-set.
         * <p>
         * Consequently, viewers may reflect lower visual rank e.g. by rendering the latter action buttons as outlined.
         */
        LOWER,
    }

    public enum LabelIndent {
        NONE,
        /**
         * For menu items that are rendered in vertical sequence, some may have icons some may not.
         * For improved visual appearance, the latter can be forced to align with the others by means
         * of a blank icon, that just occupies the same amount of width as regular icons.
         */
        FORCE_ALIGNMENT_WITH_BLANK_ICON;
    }

    // -- DECORATION MODEL

    @Builder(builderMethodName = "builderInternal")
    public record ActionDecorationModel(
            ObjectAction action,
            ActionStyle actionStyle,
            VisualRank visualRank,
            LabelIndent labelIndent,
            Optional<DisablingDecorationModel> disabling,
            Optional<PrototypingDecorationModel> prototyping,
            Optional<FontAwesomeLayers> fontAwesomeLayers,
            Optional<String> describedAs,
            Optional<String> additionalCssClass) {

        public static ActionDecorationModel of(
                final HasManagedAction managedActionHolder, final ActionStyle actionStyle) {
            var managedAction = managedActionHolder.getManagedAction();
            var action = managedAction.getAction();
            var labelIndent = switch (actionStyle) {
                case BUTTON -> LabelIndent.NONE;
                case MENU_ITEM -> LabelIndent.FORCE_ALIGNMENT_WITH_BLANK_ICON;
            };
            return builderInternal()
                .action(action)
                .actionStyle(actionStyle)
                .visualRank(managedActionHolder.isPositionedInsideFieldSet()
                        || action.isPrototype()
                        ? VisualRank.LOWER
                        : VisualRank.DEFAULT)
                .labelIndent(labelIndent)
                .prototyping(action.isPrototype()
                        ? Optional.of(PrototypingDecorationModel.of(managedAction))
                        : Optional.empty())
                .describedAs(managedAction.getDescription())
                .disabling(DisablingDecorationModel.of(managedAction.checkUsability()))
                .additionalCssClass(managedActionHolder.getAdditionalCssClass())
                .fontAwesomeLayers(managedActionHolder.lookupFontAwesomeLayers(labelIndent))
                .build();
        }

        public Identifier featureIdentifier() {
            return action.getFeatureIdentifier();
        }

        public boolean isImmediateConfirmationRequired() {
            return action.isImmediateConfirmationRequired();
        }

        public Optional<String> disabledReason() {
            return disabling.map(DisablingDecorationModel::reason);
        }

        /**
         * @see VisualRank
         */
        public boolean isLowerVisualRank() {
            return visualRank==VisualRank.LOWER;
        }

        /**
         * @see LabelIndent
         */
        public boolean isForceAlignmentWithBlankIcon() {
            return labelIndent==LabelIndent.FORCE_ALIGNMENT_WITH_BLANK_ICON;
        }

        /**
         * @see ActionStyle
         */
        public boolean isMenuItem() {
            return actionStyle==ActionStyle.MENU_ITEM;
        }

        /**
         * Whether the underlying action will spawn a dialog.
         * Typically used to indicate the action with a tailing ellipsis.
         */
        public boolean isBoundToDialog() {
            return action.getParameterCount()>0
                    || action.isImmediateConfirmationRequired();
        }
    }

}
