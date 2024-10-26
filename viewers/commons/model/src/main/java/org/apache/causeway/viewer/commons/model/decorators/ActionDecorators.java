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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.val;
import lombok.experimental.Accessors;
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
    
    public enum ButtonModifier {
        NONE,
        /**
         * To respect UI visual hierarchy, we render action buttons outlined, 
         * if they appear next to their association, (this is, not in the field-set header). 
         */
        OUTLINED,
    }
    
    public enum MenuItemModifier {
        NONE,
        /**
         * For menu items that are rendered in vertical sequence, some may have icons some may not.
         * For improved visual appearance, the latter can be force aligned with the others by means 
         * of a blank icon, that just occupies the same amount of width as regular icons.   
         */
        FORCE_ALIGNMENT_WITH_BLANK_ICON;
    }

    // -- DECORATION MODEL

    @Builder(builderMethodName = "builderInternal")
    @Getter @Accessors(fluent=true) //RECORD (java 16)
    @AllArgsConstructor
    public static class ActionDecorationModel {
        private final ObjectAction action;
        private final ActionStyle actionStyle;
        private final ButtonModifier buttonModifier;
        private final MenuItemModifier menuItemModifier;
        private final Optional<DisablingDecorationModel> disabling;
        private final Optional<PrototypingDecorationModel> prototyping;
        private final Optional<FontAwesomeLayers> fontAwesomeLayers;
        private final Optional<String> describedAs;
        private final Optional<String> additionalCssClass;
        
        public static ActionDecorationModelBuilder builder(
                HasManagedAction managedActionHolder) {
            var managedAction = managedActionHolder.getManagedAction();
            val action = managedAction.getAction();
            return builderInternal()
                .action(action)
                .buttonModifier(managedActionHolder.isRenderOutlined()
                        || action.isPrototype()
                        ? ButtonModifier.OUTLINED
                        : ButtonModifier.NONE)
                .menuItemModifier(MenuItemModifier.FORCE_ALIGNMENT_WITH_BLANK_ICON) // default
                .prototyping(action.isPrototype() 
                        ? Optional.of(PrototypingDecorationModel.of(managedAction))
                        : Optional.empty())
                .describedAs(managedAction.getDescription())
                .disabling(DisablingDecorationModel.of(managedAction.checkUsability()))
                .additionalCssClass(managedActionHolder.getAdditionalCssClass())
                .fontAwesomeLayers(managedActionHolder.lookupFontAwesomeLayers(true));
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
         * @see ButtonModifier
         */
        public boolean isRenderOutlined() {
            return buttonModifier==ButtonModifier.OUTLINED;
        }
        
        /**
         * @see MenuItemModifier
         */
        public boolean isForceAlignmentWithBlankIcon() {
            return menuItemModifier==MenuItemModifier.FORCE_ALIGNMENT_WITH_BLANK_ICON; 
        }

        /**
         * @see ActionStyle
         */
        public boolean isMenuItem() {
            return actionStyle==ActionStyle.MENU_ITEM;
        }
    }

}
