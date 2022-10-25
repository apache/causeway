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
package org.apache.causeway.core.runtimeservices.placeholder;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.applib.services.placeholder.PlaceholderRenderService;
import org.apache.causeway.commons.internal.html._BootstrapBadge;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

import lombok.NonNull;
import lombok.val;

/**
 * Default implementation of {@link PlaceholderRenderService},
 * that (HTML) renders <i>Bootstrap</i> styled placeholder badges.
 *
 * @since 2.0
 */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".PlaceholderRenderServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class PlaceholderRenderServiceDefault
implements PlaceholderRenderService {

    @Inject private TranslationService translationService;

    @Override
    public String asText(@NonNull final PlaceholderLiteral placeholderLiteral) {
        val translatedPlainText = translationService
                .translate(TranslationContext.empty(), placeholderLiteral.getLiteral());
        return "(" + translatedPlainText + ")";
    }

    @Override
    public String asHtml(@NonNull final PlaceholderLiteral placeholderLiteral) {
        return _BootstrapBadge.builder()
                .caption(asText(placeholderLiteral))
                .cssClass("placeholder-literal-" + placeholderLiteral.getLiteral())
                .build()
                .toHtml();
    }

}
