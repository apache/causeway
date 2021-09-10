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

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.services.message.MessageService;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

@ActionLayout(named="Hide", promptStyle = PromptStyle.DIALOG_MODAL)
@Action
@RequiredArgsConstructor
public class DependentArgsActionDemo_useHide {

    @Inject MessageService messageService;

    private final DependentArgsActionDemo holder;

    @Value @Accessors(fluent = true) // fluent so we can replace this with Java(14+) records later
    static class Parameters {
        boolean hideMessageField;
        String message;
    }

    @MemberSupport public DependentArgsActionDemo act(

            // PARAM 0
            @ParameterLayout(named = "Hide Message Field") final
            boolean hideMessageField,

            // PARAM 1
            @Parameter(optionality = Optionality.MANDATORY)
            @ParameterLayout(named = "Message") final
            String message

            ) {

        messageService.informUser(message);
        return holder;
    }

    // -- PARAM 0 (boolean hideMessageField)

    @MemberSupport public boolean default0Act() {
        return holder.isDialogCheckboxDefault();
    }

    // -- PARAM 1 (String message)

    @MemberSupport public boolean hide1Act(final boolean hideMessageField) {
        return hideMessageField;
    }


    public boolean hide1Act(final Parameters params) {
        return params.hideMessageField();
    }


}

