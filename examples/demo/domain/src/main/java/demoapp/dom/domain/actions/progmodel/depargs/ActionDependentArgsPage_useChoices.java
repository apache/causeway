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

import demoapp.dom.domain.actions.progmodel.TvCharacter;
import demoapp.dom.domain.actions.progmodel.TvShow;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import lombok.experimental.Accessors;

@ActionLayout(named="Choices", promptStyle = PromptStyle.DIALOG_MODAL)
@Action(semantics = SemanticsOf.SAFE)
@RequiredArgsConstructor
public class ActionDependentArgsPage_useChoices {

    @Inject MessageService messageService;

    private final ActionDependentArgsPage holder;

    @Value @Accessors(fluent = true) // fluent so we can replace this with Java(14+) records later
    static class Parameters {
        TvShow tvShow;
        TvCharacter item1;
    }

    @MemberSupport public ActionDependentArgsPage act(
        @Parameter(optionality = Optionality.MANDATORY) final TvShow tvShow,
        @Parameter(optionality = Optionality.MANDATORY) final TvCharacter item
    ) {
        messageService.informUser(item.getName());
        return holder;
    }

    // -- PARAM 0 (Parity)

    @MemberSupport public TvShow default0Act() {
        return holder.getFirstParamDefault();
    }

    // -- PARAM 1 (DemoItem)

    @MemberSupport public TvCharacter default1Act(final Parameters params) {
        // fill in first that is possible based on the first param from the UI dialog
        return params.tvShow()==null
                ? null
                : choices1Act(params).stream().findFirst().orElse(null);
    }

    @MemberSupport public Collection<TvCharacter> choices1Act(final Parameters params) {

        val parity = params.tvShow(); // <-- the refining parameter from the dialog above

        if(parity == null) {
            return holder.getItems();
        }
        return holder.getItems()
                .stream()
                .filter(item->parity == item.getTvShow())
                .collect(Collectors.toList());
    }


}

