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
package org.apache.isis.runtime.services.i18n.po;

import org.apache.isis.applib.IsisApplibModule;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.value.Clob;

@DomainService(
        nature = NatureOfService.VIEW,
        objectType = "isisApplib.TranslationServicePoMenu"
        )
@DomainServiceLayout(
        named = "Prototyping",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
        )
public class TranslationServicePoMenu {

    public static abstract class ActionDomainEvent extends IsisApplibModule.ActionDomainEvent<TranslationServicePoMenu> {
    }

    // //////////////////////////////////////

    public static class DownloadPotFileDomainEvent extends ActionDomainEvent {
    }

    @Action(
            domainEvent = DownloadPotFileDomainEvent.class,
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-download"
            )
    @MemberOrder(sequence="500.700.1")
    public Clob downloadTranslations(
            @ParameterLayout(named = ".pot file name")
            final String potFileName) {
        final String chars = translationService.toPot();
        return new Clob(Util.withSuffix(potFileName, "pot"), "text/plain", chars);
    }

    public String default0DownloadTranslations() {
        return "translations.pot";
    }

    // //////////////////////////////////////

    public static class ResetTranslationCacheDomainEvent extends ActionDomainEvent { }

    @Action(
            domainEvent = ResetTranslationCacheDomainEvent.class,
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            named="Clear translation cache",
            cssClassFa = "fa-trash"
            )
    @MemberOrder(sequence="500.700.2")
    public void resetTranslationCache() {
        translationService.clearCache();
    }
    public boolean hideResetTranslationCache() {
        return translationService.getMode().isWrite();
    }

    // //////////////////////////////////////

    public static class SwitchToReadingTranslationsDomainEvent extends ActionDomainEvent { }

    @Action(
            domainEvent = SwitchToReadingTranslationsDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT,
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-book"
            )
    @MemberOrder(sequence="500.700.2")
    public void switchToReadingTranslations() {
        translationService.toggleMode();
    }
    public boolean hideSwitchToReadingTranslations() {
        return translationService.getMode().isRead();
    }

    // //////////////////////////////////////

    public static class SwitchToWritingTranslationsDomainEvent extends ActionDomainEvent {
    }

    @Action(
            domainEvent = SwitchToWritingTranslationsDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT,
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-pencil"
            )
    @MemberOrder(sequence="500.700.3")
    public void switchToWritingTranslations() {
        translationService.toggleMode();
    }
    public boolean hideSwitchToWritingTranslations() {
        return translationService.getMode().isWrite();
    }

    // //////////////////////////////////////

    @javax.inject.Inject
    private TranslationServicePo translationService;

}
