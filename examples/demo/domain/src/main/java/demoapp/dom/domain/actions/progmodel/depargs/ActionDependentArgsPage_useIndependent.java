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

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.services.message.MessageService;

import demoapp.dom.domain.actions.progmodel.TvCharacter;
import demoapp.dom.domain.actions.progmodel.TvShow;
import lombok.RequiredArgsConstructor;
import lombok.val;

@ActionLayout(named="Independent Args", promptStyle = PromptStyle.DIALOG_MODAL)
@Action
@RequiredArgsConstructor
public class ActionDependentArgsPage_useIndependent {

    @Inject MessageService messageService;

    private final ActionDependentArgsPage holder;

    @MemberSupport public ActionDependentArgsPage act(

            // PARAM 0
            @Parameter(optionality = Optionality.MANDATORY) final
            TvShow tvShow,

            // PARAM 1
            @Parameter(optionality = Optionality.MANDATORY) final
            TvCharacter item1,

            // PARAM 2
            @Parameter(optionality = Optionality.MANDATORY) final
            TvCharacter item2

            ) {

        val message = String.format("got %s %s %s", tvShow, item1.getTvShow(), item2.getTvShow());

        messageService.informUser(message);
        return holder;
    }

    // -- PARAM 0 (Parity)

    @MemberSupport public TvShow default0Act() {
        return holder.getFirstParamDefault();
    }

    // -- PARAM 1 (DemoItem item1)

    @MemberSupport public Collection<TvCharacter> choices1Act() {
        return holder.getItems();
    }

    // -- PARAM 2 (DemoItem item2)

    @MemberSupport public Collection<TvCharacter> choices2Act() {
        return holder.getItems();
    }


}

