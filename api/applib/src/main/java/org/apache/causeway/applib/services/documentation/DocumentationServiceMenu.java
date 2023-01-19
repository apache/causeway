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
package org.apache.causeway.applib.services.documentation;

import javax.inject.Named;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.value.Markup;

/**
 * Simply provides a UI to for the generation of a documentation (obtained from {@link DocumentationService}).
 *
 * @since 2.x {@index}
 */
@Named(DocumentationServiceMenu.LOGICAL_TYPE_NAME)
@DomainService()
@DomainServiceLayout(
        named = "Prototyping",
        menuBar = DomainServiceLayout.MenuBar.SECONDARY
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
public class DocumentationServiceMenu {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleApplib.NAMESPACE + ".DocumentationServiceMenu";

    public static abstract class ActionDomainEvent<T> extends CausewayModuleApplib.ActionDomainEvent<T> {}

    private final DocumentationService documentationService;

    public DocumentationServiceMenu(final DocumentationService DocumentationService) {
        this.documentationService = DocumentationService;
    }

    @Action(
            domainEvent = downloadDocumentation.ActionDomainEvent.class,
            semantics = SemanticsOf.NON_IDEMPOTENT, //disable client-side caching
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-download",
            named = "The application-level help & documentation",
            sequence="500.450.2")
    public class downloadDocumentation{

        public class ActionDomainEvent extends DocumentationServiceMenu.ActionDomainEvent<downloadDocumentation> {}

        @MemberSupport public Markup act() {
            final String html = documentationService.toDocumentationHtml();
            return new Markup(html);
        }

    }

}
