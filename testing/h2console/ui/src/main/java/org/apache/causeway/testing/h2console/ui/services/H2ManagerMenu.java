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
package org.apache.causeway.testing.h2console.ui.services;

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.NatureOfService;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.value.LocalResourcePath;
import org.apache.causeway.testing.h2console.ui.CausewayModuleTestingH2ConsoleUi;
import org.apache.causeway.testing.h2console.ui.webmodule.WebModuleH2Console;

/**
 * @since 2.0 {@index}
 */
@DomainService(
        nature = NatureOfService.VIEW
)
@Named(CausewayModuleTestingH2ConsoleUi.NAMESPACE + ".H2ManagerMenu")
@DomainServiceLayout(
        named = "Prototyping",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
public class H2ManagerMenu {

    private final WebModuleH2Console webModule;

    @Inject
    public H2ManagerMenu(final WebModuleH2Console webModule) {
        this.webModule = webModule;
    }

    public static class ActionDomainEvent extends CausewayModuleApplib.ActionDomainEvent<H2ManagerMenu>{}

    @Action(
            semantics = SemanticsOf.SAFE,
            restrictTo = RestrictTo.PROTOTYPING,
            domainEvent = ActionDomainEvent.class
            )
    @ActionLayout(
            named = "H2 Console",
            cssClassFa = "database",
            sequence = "500.800")
    public LocalResourcePath openH2Console() {
        return getPathToH2Console().orElse(null);
    }
    @MemberSupport public boolean hideOpenH2Console() {
        return getPathToH2Console().isEmpty();
    }

    // -- HELPER

    private Optional<LocalResourcePath> getPathToH2Console() {
        return Optional.ofNullable(webModule)
                .map(WebModuleH2Console::getLocalResourcePathIfEnabled);
    }

}
