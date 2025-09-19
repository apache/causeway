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
package org.apache.causeway.core.runtimeservices.render;

import java.util.Map;
import java.util.Optional;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.applib.services.render.PlaceholderRenderService;
import org.apache.causeway.commons.internal.html._BootstrapBadge;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

import org.jspecify.annotations.NonNull;

/**
 * Default implementation of {@link PlaceholderRenderService},
 * that (HTML) renders <a href="https://getbootstrap.com/">Bootstrap</a> styled placeholder badges.
 *
 * @since 2.0 {@index}
 */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".PlaceholderRenderServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class PlaceholderRenderServiceDefault
implements PlaceholderRenderService {

    @Inject private TranslationService translationService;

    @Override
    public String asText(
            final @NonNull PlaceholderLiteral placeholderLiteral,
            final @Nullable Map<String, String> vars) {
        return "(" + translateAndInterpolate(placeholderLiteral, vars) + ")";
    }

    @Override
    public String asHtml(
            final @NonNull PlaceholderLiteral placeholderLiteral,
            final @Nullable Map<String, String> vars) {

        var href = Optional.ofNullable(vars).map(map->map.get("href"));

        return _BootstrapBadge.builder()
                .caption(translateAndInterpolate(placeholderLiteral, vars))
                .cssClass("placeholder-literal-" + placeholderLiteral.name().toLowerCase())
                .href(href.orElse(null))
                .nestedCaption(href.map(__->"..").orElse(null))
                .build()
                .toHtml();
    }

    private String translateAndInterpolate(
            final PlaceholderLiteral placeholderLiteral, final Map<String, String> vars) {
        var translatedPlainText = translationService
                .translate(TranslationContext.empty(), placeholderLiteral.getLiteral());
        return PlaceholderRenderService.interpolate(translatedPlainText, vars);
    }

}
