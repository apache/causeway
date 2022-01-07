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
package demoapp.dom.domain.actions.progmodel.depargs;

import javax.inject.Inject;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.MemberSupport;
import org.apache.isis.applib.annotations.Optionality;
import org.apache.isis.applib.annotations.Parameter;
import org.apache.isis.applib.annotations.ParameterLayout;
import org.apache.isis.applib.annotations.PromptStyle;
import org.apache.isis.applib.services.message.MessageService;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import lombok.experimental.Accessors;

@ActionLayout(named="Default", promptStyle = PromptStyle.DIALOG_MODAL)
@Action
@RequiredArgsConstructor
public class DependentArgsActionDemo_useDefault {

    @Inject MessageService messageService;

    private final DependentArgsActionDemo mixee;

    @Value @Accessors(fluent = true) // fluent so we can replace this with Java(14+) records later
    static class Parameters {
        Parity parity;
        String message;
    }

    public DependentArgsActionDemo act(

            // PARAM 0
            @Parameter(optionality = Optionality.MANDATORY) final
            Parity parity,

            // PARAM 1
            @Parameter(optionality = Optionality.MANDATORY)
            @ParameterLayout(named = "Message") final
            String message

            ) {

        messageService.informUser(message);
        return mixee;
    }

    // -- PARAM 0 (Parity)

    @MemberSupport public Parity defaultParity(final Parameters params) {

        return mixee.getDialogParityDefault();
    }

    // -- PARAM 1 (String message)

    @MemberSupport public String defaultMessage(final Parameters params) {

        val parityFromDialog = params.parity(); // <-- the refining parameter from the dialog above

        if(parityFromDialog == null) {
            return "no parity selected";
        }
        return parityFromDialog.name();
    }

}

