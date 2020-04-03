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
package org.apache.isis.viewer.wicket.ui.util;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.Button;

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.config.messages.MessageRegistry;

import lombok.NonNull;
import lombok.val;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.confirmation.ConfirmationBehavior;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.confirmation.ConfirmationConfig;

public class Confirmations {

    public static void addConfirmationDialog(
            @Nullable final TranslationService translationService,
            @NonNull  final Component component,
            @NonNull  final TooltipConfig.Placement placement) {

        if(component instanceof Button) {
            // ensure dialog ok buttons receive the danger style as well
            // don't care if expressed twice
            addConfirmationStyle(component);
        }
        
        val confirmationConfig = getConfirmationConfig(translationService).withPlacement(placement);
        component.add(new ConfirmationBehavior(confirmationConfig));
    }
    
    public static void addConfirmationStyle(final Component component) {
        component.add(new CssClassAppender("btn-danger"));
    }
    
    private static ConfirmationConfig getConfirmationConfig(
            @Nullable final TranslationService translationService) {
        
        val confirmationConfig = new ConfirmationConfig();
        final String areYouSure = translate(translationService, MessageRegistry.MSG_ARE_YOU_SURE); 
        final String confirm = translate(translationService, MessageRegistry.MSG_CONFIRM);
        final String cancel = translate(translationService, MessageRegistry.MSG_CANCEL);

        confirmationConfig
        .withTitle(areYouSure)
        .withBtnOkLabel(confirm)
        .withBtnCancelLabel(cancel)
        .withBtnOkClass("btn btn-danger")
        .withBtnCancelClass("btn btn-default");
        
        return confirmationConfig;
    }

    private static String translate(TranslationService translationService, String msg) {
        if(translationService!=null) {
            return translationService.translate(MessageRegistry.class.getName(), msg);
        }
        return msg;
    }
    
}
