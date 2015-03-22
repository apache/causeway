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
package org.apache.isis.core.runtime.services.i18n.po;

import java.util.List;
import javax.inject.Inject;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.IsisApplibModule;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.value.Clob;

@DomainService()
@DomainServiceLayout(
        named = "Prototyping",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY,
        menuOrder = "500.500"
)
public class TranslationServicePoMenu {

    public static abstract class ActionDomainEvent extends IsisApplibModule.ActionDomainEvent<TranslationServicePoMenu> {
        public ActionDomainEvent(final TranslationServicePoMenu source, final Identifier identifier) {
            super(source, identifier);
        }

        public ActionDomainEvent(final TranslationServicePoMenu source, final Identifier identifier, final Object... arguments) {
            super(source, identifier, arguments);
        }

        public ActionDomainEvent(final TranslationServicePoMenu source, final Identifier identifier, final List<Object> arguments) {
            super(source, identifier, arguments);
        }
    }

    // //////////////////////////////////////

    public static class DownloadPotFileDomainEvent extends ActionDomainEvent {
        public DownloadPotFileDomainEvent(final TranslationServicePoMenu source, final Identifier identifier, final Object... arguments) {
            super(source, identifier, arguments);
        }
    }

    @Action(
            domainEvent = DownloadPotFileDomainEvent.class,
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(
            cssClassFa = "fa-download"
    )
    @MemberOrder(sequence="500.500.1")
    public Clob downloadTranslations(
            @ParameterLayout(named = ".pot file name")
            final String potFileName) {
        final String chars = translationService.toPot();
        return new Clob(potFileName, "text/plain", chars);
    }

    public String default0DownloadTranslations() {
        return "translations.pot";
    }
    public boolean hideDownloadTranslations() {
        return translationService.getMode().isRead();
    }

    // //////////////////////////////////////

    public static class ResetTranslationCacheDomainEvent extends ActionDomainEvent {
        public ResetTranslationCacheDomainEvent(final TranslationServicePoMenu source, final Identifier identifier, final Object... arguments) {
            super(source, identifier, arguments);
        }
    }

    @Action(
            domainEvent = ResetTranslationCacheDomainEvent.class,
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(
            named="Clear translation cache",
            cssClassFa = "fa-trash"
    )
    @MemberOrder(sequence="500.500.2")
    public void resetTranslationCache() {
        translationService.clearCache();
    }
    public boolean hideResetTranslationCache() {
        return translationService.getMode().isWrite();
    }

    // //////////////////////////////////////

    public static class SwitchToReadingTranslationsDomainEvent extends ActionDomainEvent {
        public SwitchToReadingTranslationsDomainEvent(final TranslationServicePoMenu source, final Identifier identifier, final Object... arguments) {
            super(source, identifier, arguments);
        }
    }

    @Action(
            domainEvent = SwitchToReadingTranslationsDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT,
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(
            cssClassFa = "fa-book"
    )
    @MemberOrder(sequence="500.500.3")
    public void switchToReadingTranslations() {
        translationService.toggleMode();
    }
    public boolean hideSwitchToReadingTranslations() {
        return translationService.getMode().isRead();
    }

    // //////////////////////////////////////

    public static class SwitchToWritingTranslationsDomainEvent extends ActionDomainEvent {
        public SwitchToWritingTranslationsDomainEvent(final TranslationServicePoMenu source, final Identifier identifier, final Object... arguments) {
            super(source, identifier, arguments);
        }
    }

    @Action(
            domainEvent = SwitchToWritingTranslationsDomainEvent.class,
            semantics = SemanticsOf.IDEMPOTENT,
            restrictTo = RestrictTo.PROTOTYPING
    )
    @ActionLayout(
            cssClassFa = "fa-pencil"
    )
    @MemberOrder(sequence="500.500.4")
    public void switchToWritingTranslations() {
        translationService.toggleMode();
    }
    public boolean hideSwitchToWritingTranslations() {
        return translationService.getMode().isWrite();
    }

    // //////////////////////////////////////

    @Inject
    private TranslationServicePo translationService;

}
