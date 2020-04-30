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

import java.util.Collection;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MinLength;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.incubator.model.applib.annotation.Model;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import lombok.experimental.Accessors;

@ActionLayout(named="Auto Complete", promptStyle = PromptStyle.DIALOG_MODAL)
@Action
@RequiredArgsConstructor
public class DependentArgsActionDemo_useAutoComplete {

    @Inject MessageService messageService;

    private final DependentArgsActionDemo holder;
    
    @Value @Accessors(fluent = true) // fluent so we can replace this with Java(14+) records later
    static class Parameters {
        Parity parity;
        DemoItem item1;
    }

    public DependentArgsActionDemo act(

            // PARAM 0
            @Parameter(optionality = Optionality.MANDATORY)
            Parity parity,

            // PARAM 1
            @Parameter(optionality = Optionality.MANDATORY)
            DemoItem item

            ) {

        messageService.informUser(item.getName());
        return holder;
    }

    // -- PARAM 0 (Parity)

    @Model
    public Parity default0Act() {
        return holder.getDialogParityDefault();
    }
    
    // -- PARAM 1 (DemoItem)

    @Model
    public Collection<DemoItem> autoComplete1Act(
            Parameters params, 
            @MinLength(3) String search) {
        
        val parity = params.parity(); // <-- the refining parameter from the dialog above

        if(parity == null) {
            return holder.getItems()
                    .stream()
                    .filter(item->item.getName().toLowerCase().contains(search.toLowerCase()))
                    .collect(Collectors.toList());
        }

        return holder.getItems()
                .stream()
                .filter(item->parity == item.getParity())
                .filter(item->item.getName().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());
    }


}

