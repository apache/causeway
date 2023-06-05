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

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.events.domain.ActionDomainEvent;

import lombok.RequiredArgsConstructor;


//tag::class[]
@Action(domainEvent = ActionDomainEventPage_updateText.DomainEvent.class)   // <.>
@ActionLayout(
    describedAs = "This action emits a custom domain event"
)
@RequiredArgsConstructor
public class ActionDomainEventPage_updateText {

    public static class DomainEvent                                         // <.>
            extends ActionDomainEvent<ActionDomainEventPage> {}
    // ...
//end::class[]

    private final ActionDomainEventPage page;

    @MemberSupport public ActionDomainEventPage act(final String text) {
        page.setText(text);
        return page;
    }
    @MemberSupport public String default0Act() {
        return page.getText();
    }
//tag::class[]
}
//end::class[]
