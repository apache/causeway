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
package org.apache.isis.core.metamodel.services.i18n.po;

import java.util.List;
import javax.inject.Inject;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.IsisApplibModule;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.value.Clob;

@DomainService(
        nature = NatureOfService.VIEW_MENU_ONLY
)
@DomainServiceLayout(
        menuBar = DomainServiceLayout.MenuBar.SECONDARY,
        named = "Prototyping"
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
    public Clob downloadPotFile(
            @ParameterLayout(named = ".pot file name")
            final String potFileName) {
        final String chars = translationService.toPo();
        return new Clob(potFileName, "text/plain", chars);
    }

    public String default0DownloadPotFile() {
        return "translations.pot";
    }
    public boolean hideDownloadPotFile() {
        return translationService.getMode().isRead();
    }

    // //////////////////////////////////////


    @Inject
    private TranslationServicePo translationService;

}
