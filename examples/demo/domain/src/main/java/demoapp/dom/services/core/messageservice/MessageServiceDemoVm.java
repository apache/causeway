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

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.ActionLayout.Position;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.services.message.MessageService;

import demoapp.dom._infra.asciidocdesc.HasAsciiDocDescription;

@DomainObject(nature=Nature.VIEW_MODEL, objectType = "demo.MessageServiceDemoVm")
public class MessageServiceDemoVm implements HasAsciiDocDescription {

    @Inject private MessageService messageService;

    public String title() {
        return "Message Demo";
    }

    @ActionLayout(
            describedAs = "Presents an info style message.",
            cssClassFa="fa-sticky-note",
            position = Position.PANEL)
    @Action
    public MessageServiceDemoVm infoMessage(){
        System.err.println("EXEC");
        messageService.informUser("Demo Info Message.");
        return this;
    }

    @ActionLayout(
            describedAs = "Presents an warning style message.",
            cssClassFa="fa-sticky-note",
            position = Position.PANEL)
    @Action
    public MessageServiceDemoVm warnMessage(){
        messageService.warnUser("Demo Warning Message.");
        return this;
    }

    @ActionLayout(
            describedAs = "Presents an error style message.",
            cssClassFa="fa-sticky-note",
            position = Position.PANEL)
    @Action
    public MessageServiceDemoVm errorMessage(){
        messageService.raiseError("Demo Error Message.");
        return this;
    }


}
