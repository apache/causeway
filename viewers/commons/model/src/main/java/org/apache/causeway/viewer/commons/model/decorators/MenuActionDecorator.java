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
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.viewer.commons.model.decorators.DisablingDecorator.DisablingDecorationModel;
import org.apache.causeway.viewer.commons.model.decorators.PrototypingDecorator.PrototypingDecorationModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.val;
import lombok.experimental.Accessors;

/**
 * Decorates Actions that appear in drop-down menus.
 * 
 * @param <T> primary UI component type to decorate
 * @param <U> secondary UI component type to decorate
 * @param <V> tertiary UI component type to decorate
 */
@FunctionalInterface
public interface MenuActionDecorator<T, U, V> {

    void decorate(T uiComponent1, U uiComponent2, V uiComponent3, MenuActionDecorationModel decorationModel);

    // -- DECORATION MODEL

    @Builder(builderMethodName = "builderInternal")
    @Getter @Accessors(fluent=true) //RECORD (java 16)
    @AllArgsConstructor
    public static class MenuActionDecorationModel {
        private final boolean isImmediateConfirmationRequired;
        private final Identifier featureIdentifier;
        private final Optional<DisablingDecorationModel> disabling;
        private final Optional<PrototypingDecorationModel> prototyping;
        private final Optional<FontAwesomeLayers> fontAwesomeLayers;
        private final Optional<String> describedAs;
        private final Optional<String> additionalCssClass;
        
        public static MenuActionDecorationModelBuilder builder(final ManagedAction managedAction) {
            val action = managedAction.getAction();
            return builderInternal()
                    .prototyping(managedAction.getAction().isPrototype() 
                            ? Optional.of(PrototypingDecorationModel.of(managedAction))
                            : Optional.empty())
                    .isImmediateConfirmationRequired(action.isImmediateConfirmationRequired());
        }
        
    }

}
