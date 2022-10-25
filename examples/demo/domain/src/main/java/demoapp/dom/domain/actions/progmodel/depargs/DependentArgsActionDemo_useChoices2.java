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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Lists;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import lombok.experimental.Accessors;

@Action(semantics = SemanticsOf.SAFE)
@ActionLayout(
        named="MultiChoices",
        promptStyle = PromptStyle.DIALOG_MODAL)
@RequiredArgsConstructor
public class DependentArgsActionDemo_useChoices2 {

    @Inject MessageService messageService;

    private final DependentArgsActionDemo holder;

    @Value @Accessors(fluent = true) // fluent so we can replace this with Java(14+) records later
    static class Parameters {
        List<Parity> parities;
        List<DemoItem> items;
    }

    @MemberSupport public DependentArgsActionDemo act(

            // PARAM 0
            @Parameter(optionality = Optionality.MANDATORY)
            List<Parity> parities,

            // PARAM 1
            @Parameter(optionality = Optionality.MANDATORY)
            List<DemoItem> items

            ) {

        _NullSafe.stream(items)
        .forEach(item->messageService.informUser(item.getName()));

        return holder;
    }

    // -- PARAM 0 (Parities)

    @MemberSupport public List<Parity> defaultParities(Parameters params) {
        return _Lists.of(holder.getDialogParityDefault());
    }

    // -- PARAM 1 (DemoItem)

    @MemberSupport public List<DemoItem> defaultItems(Parameters params) {

        return choicesItems(params); // <-- fill in all that are possible based on the first param from the UI dialog
    }

    @MemberSupport public List<DemoItem> choicesItems(Parameters params) {

        val paritiesFromDialog = params.parities(); // <-- the refining parameter from the dialog above

        if(_NullSafe.isEmpty(paritiesFromDialog)) {
            return Collections.emptyList();
        }
        return holder.getItems()
                .stream()
                .filter(item->paritiesFromDialog.contains(item.getParity()))
                .collect(Collectors.toList());
    }


}

