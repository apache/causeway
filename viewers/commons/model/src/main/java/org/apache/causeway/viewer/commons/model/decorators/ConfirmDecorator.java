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

import java.io.Serializable;
import java.util.Optional;

import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.core.config.messages.MessageRegistry;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.commons.model.layout.UiPlacementDirection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@FunctionalInterface
public interface ConfirmDecorator<T> {

    void decorate(T uiComponent, ConfirmDecorationModel decorationModel);

    // -- DECORATION MODEL

    @Getter @Accessors(fluent=true) //RECORD (java 16)
    @AllArgsConstructor
    public static class ConfirmDecorationModel implements Serializable {

        private static final long serialVersionUID = 1L;

        final @NonNull String title;
        final @NonNull Optional<String> message;
        final @NonNull String okLabel;
        final @NonNull String cancelLabel;
        final @NonNull UiPlacementDirection placement;

        public static ConfirmDecorationModel areYouSure(final UiPlacementDirection placement) {

            var translationService = MetaModelContext.translationServiceOrFallback();
            var context = TranslationContext.forClassName(MessageRegistry.class);
            
            var areYouSure = translationService.translate(context, MessageRegistry.MSG_ARE_YOU_SURE);
            var confirm = translationService.translate(context, MessageRegistry.MSG_CONFIRM);
            var cancel = translationService.translate(context, MessageRegistry.MSG_CANCEL);

            var message = Optional.<String>empty(); // not used yet

            return new ConfirmDecorationModel(areYouSure, message, confirm, cancel, placement);
        }

    }

}
