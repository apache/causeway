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
package demoapp.dom.services.core.messageservice;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.ActionLayout.Position;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.message.MessageService;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;
import lombok.NoArgsConstructor;

@Named("demo.MessageServiceDemoPage")
@DomainObject(nature=Nature.VIEW_MODEL)
@DomainObjectLayout(cssClassFa="fa-sticky-note")
@NoArgsConstructor
public class MessageServiceDemoPage implements HasAsciiDocDescription {

    @Inject private MessageService messageService;

    @ObjectSupport public String title() {
        return "Message Demo";
    }

//tag::informUser[]
    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClass = "btn-info",
            cssClassFa="fa-sticky-note",
            position = Position.PANEL
    )
    public MessageServiceDemoPage informUser(){
        messageService.informUser("Demo Info Message.");
        return this;
    }
//end::informUser[]

//tag::warnUser[]
    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClass = "btn-warning",
            cssClassFa="fa-sticky-note",
            position = Position.PANEL
    )
    public MessageServiceDemoPage warnUser(){
        messageService.warnUser("Demo Warning Message.");
        return this;
    }
//end::warnUser[]

//tag::raiseError[]
    @Action(semantics = SemanticsOf.SAFE)
    @ActionLayout(
            cssClass = "btn-danger",
            cssClassFa="fa-sticky-note",
            position = Position.PANEL
    )
    public MessageServiceDemoPage raiseError(){
        messageService.raiseError("Demo Error Message.");
        return this;
    }
//end::raiseError[]

}
