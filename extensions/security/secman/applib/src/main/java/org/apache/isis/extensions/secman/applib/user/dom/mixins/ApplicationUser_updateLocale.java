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
package org.apache.isis.extensions.secman.applib.user.dom.mixins;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.MinLength;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.core.metamodel.valuesemantics.LocaleValueSemantics;
import org.apache.isis.extensions.secman.applib.IsisModuleExtSecmanApplib;
import org.apache.isis.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.isis.extensions.secman.applib.user.dom.mixins.ApplicationUser_updateLocale.DomainEvent;

import lombok.RequiredArgsConstructor;

@Action(
        domainEvent = DomainEvent.class,
        semantics = SemanticsOf.IDEMPOTENT
)
@ActionLayout(
        associateWith = "locale",
        promptStyle = PromptStyle.INLINE_AS_IF_EDIT,
        sequence = "1"
)
@RequiredArgsConstructor
public class ApplicationUser_updateLocale {

    public static class DomainEvent
            extends IsisModuleExtSecmanApplib.ActionDomainEvent<ApplicationUser_updateLocale> {}

    private final ApplicationUser target;

    @MemberSupport public ApplicationUser act(
            final Locale locale) {
        target.setLocale(locale);
        return target;
    }

    @MemberSupport public String disableAct() {
        return target.isForSelfOrRunAsAdministrator()? null: "Can only update your own user record.";
    }
    @MemberSupport public Locale defaultLocale() { return target.getLocale(); }

    @MemberSupport public List<Locale> autoCompleteLocale(@MinLength(1) final String search) {
        return LocaleValueSemantics.streamSupportedValues()
                .filter(locale->locale.toLanguageTag().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());
    }

}
