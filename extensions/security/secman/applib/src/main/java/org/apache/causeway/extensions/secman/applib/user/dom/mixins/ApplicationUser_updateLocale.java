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
package org.apache.causeway.extensions.secman.applib.user.dom.mixins;

import java.util.List;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.locale.LocaleChoiceProvider;
import org.apache.causeway.extensions.secman.applib.CausewayModuleExtSecmanApplib;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser.Locale;
import org.apache.causeway.extensions.secman.applib.user.dom.mixins.ApplicationUser_updateLocale.DomainEvent;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 *
 * @since 2.0 {@index}
 */
@Action(
        domainEvent = DomainEvent.class,
        semantics = SemanticsOf.IDEMPOTENT
)
@ActionLayout(
        associateWith = "language",
        promptStyle = PromptStyle.INLINE_AS_IF_EDIT,
        sequence = "1"
)
@RequiredArgsConstructor
public class ApplicationUser_updateLocale {

    public static class DomainEvent
            extends CausewayModuleExtSecmanApplib.ActionDomainEvent<ApplicationUser_updateLocale> {}

    @Inject LocaleChoiceProvider localeChoiceProvider;

    private final ApplicationUser mixee;

    // typed tuple made of all the action parameters
    @lombok.Value @Accessors(fluent = true)
    public static class Parameters {
        final java.util.Locale language;
        final java.util.Locale numberFormat;
        final java.util.Locale timeFormat;
    }

    @MemberSupport public ApplicationUser act(
            @Locale final java.util.Locale language,
            @Locale final java.util.Locale numberFormat,
            @Locale final java.util.Locale timeFormat) {
        mixee.setLanguage(language);
        mixee.setNumberFormat(numberFormat);
        mixee.setTimeFormat(timeFormat);
        return mixee;
    }

    @MemberSupport public String disableAct() {
        return mixee.isForSelfOrRunAsAdministrator()? null: "Can only update your own user record.";
    }

    // -- LANGUAGE

    @MemberSupport public java.util.Locale defaultLanguage(final Parameters p) {
        return mixee.getLanguage();
    }
    @MemberSupport public List<java.util.Locale> choicesLanguage(final Parameters p) {
        return localeChoiceProvider.getAvailableLocales();
    }

    // -- NUMBER FORMAT

    @MemberSupport public java.util.Locale defaultNumberFormat(final Parameters p) {
        return mixee.getNumberFormat()!=null
                ? mixee.getNumberFormat()
                : p.language();
    }
    @MemberSupport public List<java.util.Locale> choicesNumberFormat(final Parameters p) {
        return localeChoiceProvider.getAvailableLocales();
    }

    // -- TIME FORMAT

    @MemberSupport public java.util.Locale defaultTimeFormat(final Parameters p) {
        return mixee.getTimeFormat()!=null
                ? mixee.getTimeFormat()
                : p.language();
    }
    @MemberSupport public List<java.util.Locale> choicesTimeFormat(final Parameters p) {
        return localeChoiceProvider.getAvailableLocales();
    }

}
