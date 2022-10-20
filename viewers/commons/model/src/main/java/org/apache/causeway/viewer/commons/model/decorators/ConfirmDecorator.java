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
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.core.config.messages.MessageRegistry;
import org.apache.causeway.viewer.commons.model.layout.UiPlacementDirection;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@FunctionalInterface
public interface ConfirmDecorator<T> {

    void decorate(T uiComponent, ConfirmDecorationModel decorationModel);

    // -- DECORATION MODEL

    @Getter
    @RequiredArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
    public static class ConfirmDecorationModel implements Serializable {

        private static final long serialVersionUID = 1L;

        final @NonNull String title;
        final @NonNull Optional<String> message;
        final @NonNull String okLabel;
        final @NonNull String cancelLabel;
        final @NonNull UiPlacementDirection placement;

        public static ConfirmDecorationModel areYouSure(
                final TranslationService translationService,
                final UiPlacementDirection placement) {

            val context = TranslationContext.forClassName(MessageRegistry.class);

            val areYouSure = translate(translationService, context, MessageRegistry.MSG_ARE_YOU_SURE);
            val confirm = translate(translationService, context, MessageRegistry.MSG_CONFIRM);
            val cancel = translate(translationService, context, MessageRegistry.MSG_CANCEL);

            val message = Optional.<String>empty(); // not used yet

            return of(areYouSure, message, confirm, cancel, placement);
        }

        // -- HELPER

        private static String translate(final TranslationService translationService, final TranslationContext context, final String msg) {
            if(translationService!=null) {
                return translationService.translate(context, msg);
            }
            return msg;
        }

    }

}
