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
package demoapp.dom.actions.depargs;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.incubator.model.applib.annotation.Model;

import lombok.RequiredArgsConstructor;

@Mixin
@RequiredArgsConstructor
public class DependentArgsActionDemo_useDefault {

    @Inject MessageService messageService;


    private final DependentArgsActionDemo holder;

    @ActionLayout(named="Default", promptStyle = PromptStyle.DIALOG_MODAL)
    @Action(semantics = SemanticsOf.SAFE)
    public DependentArgsActionDemo $$(

            // PARAM 0
            @Parameter(optionality = Optionality.MANDATORY)
            Parity parity,

            // PARAM 1
            @Parameter(optionality = Optionality.MANDATORY)
            @ParameterLayout(named = "Message")
            String message

            ) {

        messageService.informUser(message);
        return holder;
    }

    // -- PARAM 1 (String message)

    //@Supporting
    public String default1$$(Parity parity) {
        if(parity == null) {
            return "no parity selected";
        }
        return parity.name();
    }

}

