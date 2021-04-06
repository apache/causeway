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
package org.apache.isis.viewer.common.model.decorator.confirm;

import java.io.Serializable;
import java.util.Optional;

import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.config.messages.MessageRegistry;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@Getter
@RequiredArgsConstructor(staticName = "of")
public class ConfirmUiModel implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public enum Placement {
        TOP, BOTTOM, RIGHT, LEFT;
    }
    
    @NonNull final String title;
    @NonNull final Optional<String> message;
    @NonNull final String okLabel;
    @NonNull final String cancelLabel;
    @NonNull final Placement placement;
    
    public static ConfirmUiModel ofAreYouSure(TranslationService translationService, Placement placement) {
        
    	TranslationContext context = TranslationContext.ofClass(MessageRegistry.class);
    	
        val areYouSure = translate(translationService, context, MessageRegistry.MSG_ARE_YOU_SURE); 
        val confirm = translate(translationService, context, MessageRegistry.MSG_CONFIRM);
        val cancel = translate(translationService, context, MessageRegistry.MSG_CANCEL);
        
        val message = Optional.<String>empty(); // not used yet
        
        return of(areYouSure, message, confirm, cancel, placement);
    }
    
    // -- HELPER
    
    private static String translate(TranslationService translationService, TranslationContext context, String msg) {
        if(translationService!=null) {        	
            return translationService.translate(context, msg);
        }
        return msg;
    }
    
}
