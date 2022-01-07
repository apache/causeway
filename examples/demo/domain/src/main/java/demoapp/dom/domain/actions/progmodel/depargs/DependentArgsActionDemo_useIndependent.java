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

import java.util.Collection;

import javax.inject.Inject;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.MemberSupport;
import org.apache.isis.applib.annotations.Optionality;
import org.apache.isis.applib.annotations.Parameter;
import org.apache.isis.applib.annotations.PromptStyle;
import org.apache.isis.applib.services.message.MessageService;

import lombok.RequiredArgsConstructor;
import lombok.val;

@ActionLayout(named="Independent Args", promptStyle = PromptStyle.DIALOG_MODAL)
@Action
@RequiredArgsConstructor
public class DependentArgsActionDemo_useIndependent {

    @Inject MessageService messageService;

    private final DependentArgsActionDemo holder;

    @MemberSupport public DependentArgsActionDemo act(

            // PARAM 0
            @Parameter(optionality = Optionality.MANDATORY) final
            Parity parity,

            // PARAM 1
            @Parameter(optionality = Optionality.MANDATORY) final
            DemoItem item1,

            // PARAM 2
            @Parameter(optionality = Optionality.MANDATORY) final
            DemoItem item2

            ) {

        val message = String.format("got %s %s %s", parity, item1.getParity(), item2.getParity());

        messageService.informUser(message);
        return holder;
    }

    // -- PARAM 0 (Parity)

    @MemberSupport public Parity default0Act() {
        return holder.getDialogParityDefault();
    }

    // -- PARAM 1 (DemoItem item1)

    @MemberSupport public Collection<DemoItem> choices1Act() {
        return holder.getItems();
    }

    // -- PARAM 2 (DemoItem item2)

    @MemberSupport public Collection<DemoItem> choices2Act() {
        return holder.getItems();
    }


}

