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
package org.apache.causeway.extensions.docgen.help.menu;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.extensions.docgen.help.CausewayModuleExtDocgenHelp;
import org.apache.causeway.extensions.docgen.help.applib.HelpNode.HelpTopic;
import org.apache.causeway.extensions.docgen.help.helptree.HelpNodeVm;

import lombok.RequiredArgsConstructor;

/**
 * Provides entries for a <i>Documentation</i> sub-menu section.
 * <p>
 * Currently there is only one, namely (<i>help</i>), utilizing the {@link HelpTopic}.
 *
 * @see HelpTopic
 * @since 2.x {@index}
 */
@Named(CausewayModuleExtDocgenHelp.NAMESPACE + ".DocumentationMenu")
@DomainService
@DomainServiceLayout(
        menuBar = DomainServiceLayout.MenuBar.TERTIARY
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class DocumentationMenu {

    public static abstract class ActionDomainEvent<T> extends CausewayModuleApplib.ActionDomainEvent<T> {}

    private final HelpTopic rootHelpTopic;

    /** Returns a view-model that represents the application's primary help page. */
    @Action(
            commandPublishing = Publishing.DISABLED,
            domainEvent = help.ActionDomainEvent.class,
            executionPublishing = Publishing.DISABLED,
            semantics = SemanticsOf.NON_IDEMPOTENT //disable client-side caching
    )
    @ActionLayout(
            cssClassFa = "fa-regular fa-circle-question",
            named = "Help",
            sequence = "100"
    )
    public class help {

        public class ActionDomainEvent extends DocumentationMenu.ActionDomainEvent<help> {}

        @MemberSupport public HelpNodeVm act() {
            return HelpNodeVm.forRootTopic(rootHelpTopic);
        }

    }

}
