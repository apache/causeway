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
package org.apache.isis.core.runtimeservices.i18n.po;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.value.Clob;

@Named(TranslationServicePoMenu.OBJECT_TYPE)
@DomainService(objectType = TranslationServicePoMenu.OBJECT_TYPE)
@DomainServiceLayout(
        named = "Prototyping",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
public class TranslationServicePoMenu {

    public static final String OBJECT_TYPE = IsisModuleApplib.NAMESPACE + ".TranslationServicePoMenu";

    @Inject private TranslationServicePo translationService;

    public static abstract class ActionDomainEvent extends IsisModuleApplib.ActionDomainEvent<TranslationServicePoMenu> {}

    // //////////////////////////////////////

    public static class DownloadPotFileDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = DownloadPotFileDomainEvent.class,
            semantics = SemanticsOf.NON_IDEMPOTENT, //disable client-side caching
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-download",
            sequence="500.700.1")
    public Clob downloadTranslations(
            @ParameterLayout(named = ".pot file name")
            final String potFileName) {

        return translationService.toPot()
                .map(chars->new Clob(Util.withSuffix(potFileName, "pot"), "text/plain", chars))
                .orElse(null);
    }
    public String default0DownloadTranslations() {
        return "translations.pot";
    }
    public String disableDownloadTranslations() {
        return !translationService.getMode().isWrite()
                ? notAvailableForCurrentMode()
                : null;
    }

    // //////////////////////////////////////

    public static class ResetTranslationCacheDomainEvent extends ActionDomainEvent { }

    @Action(
            domainEvent = ResetTranslationCacheDomainEvent.class,
            semantics = SemanticsOf.NON_IDEMPOTENT, //disable client-side caching
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            named="Clear translation cache",
            cssClassFa = "fa-trash",
            sequence="500.700.2")
    public void resetTranslationCache() {
        translationService.clearCache();
    }
    public String disableResetTranslationCache() {
        return !translationService.getMode().isRead()
                ? notAvailableForCurrentMode()
                : null;
    }


    // //////////////////////////////////////

    public static class SwitchToReadingTranslationsDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = SwitchToReadingTranslationsDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT,
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-book",
            sequence="500.700.2")
    public void switchToReadingTranslations() {
        translationService.toggleMode();
    }
    public String disableSwitchToReadingTranslations() {
        return !translationService.getMode().isWrite()
                ? notAvailableForCurrentMode()
                : null;
    }

    // //////////////////////////////////////

    public static class SwitchToWritingTranslationsDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = SwitchToWritingTranslationsDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT,
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-pencil-alt",
            sequence="500.700.3")
    public void switchToWritingTranslations() {
        translationService.toggleMode();
    }
    public String disableSwitchToWritingTranslations() {
        return !translationService.getMode().isRead()
                ? notAvailableForCurrentMode()
                : null;
    }

    // -- HELPER

    private String notAvailableForCurrentMode() {
        return String.format("Not available for Translation Mode '%s'.",
                translationService.getMode().name().toLowerCase());
    }

}
