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
package demoapp.dom.annotDomain.Action.associateWith.child;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.services.message.MessageService;

import lombok.RequiredArgsConstructor;

import demoapp.dom.annotDomain.Action.associateWith.ActionAssociateWithVm;


//tag::class[]
@Action(
    associateWith = "mixinChildren"         // <.>
    , associateWithSequence = "1"
)
@ActionLayout(
    named = "Remove"                        // <.>
    , describedAs =
        "@Action(" +
            "associateWith = \"mixinChildren\"" +
            ", associateWithSequence = \"1\")"
)
@RequiredArgsConstructor
public class ActionAssociateWithVm_mixinChildrenAddChild {

    private final ActionAssociateWithVm actionAssociateWithVm;

    public ActionAssociateWithVm act(ActionAssociateWithChildVm childVm) {
        messageService.informUser(
                "This is a dummy action and doesn't actually do anything. " +
                "It's purpose is just to illustrate the 'associateWith' semantic");
        return actionAssociateWithVm;
    }

    @Inject
    MessageService messageService;
}
//end::class[]
