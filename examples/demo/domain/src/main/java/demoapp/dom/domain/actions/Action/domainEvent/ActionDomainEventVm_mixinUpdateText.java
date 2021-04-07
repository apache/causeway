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
package demoapp.dom.domain.actions.Action.domainEvent;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.events.domain.ActionDomainEvent;

import lombok.RequiredArgsConstructor;


//tag::class[]
@Action(
    semantics = SemanticsOf.SAFE
    , domainEvent =
        ActionDomainEventVm_mixinUpdateText.DomainEvent.class           // <.>
    , associateWith = "text"
)
@ActionLayout(
    describedAs =
        "@Action(domainEvent = ActionDomainEventVm_mixinUpdateText.DomainEvent.class)"
        , sequence = "2"
)
@RequiredArgsConstructor
public class ActionDomainEventVm_mixinUpdateText {

    public static class DomainEvent                                     // <.>
            extends ActionDomainEvent<ActionDomainEventVm> {}

    private final ActionDomainEventVm actionDomainEventVm;

    public ActionDomainEventVm act(final String text) {
        actionDomainEventVm.setText(text);
        return actionDomainEventVm;
    }
    public String default0Act() {
        return actionDomainEventVm.getText();
    }
}
//end::class[]
