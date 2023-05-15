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

import jakarta.inject.Inject;

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

import demoapp.dom.domain.actions.progmodel.TvCharacter;
import demoapp.dom.domain.actions.progmodel.TvShow;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import lombok.experimental.Accessors;

@Action(semantics = SemanticsOf.SAFE)
@ActionLayout(
        named="MultiChoices",
        promptStyle = PromptStyle.DIALOG_MODAL)
@RequiredArgsConstructor
public class ActionDependentArgsPage_useChoicesMulti {

    @Inject MessageService messageService;

    private final ActionDependentArgsPage holder;

    @Value @Accessors(fluent = true) // fluent so we can replace this with Java(14+) records later
    static class Parameters {
        List<TvShow> tvShows;
        List<TvCharacter> tvCharacters;
    }

    @MemberSupport public ActionDependentArgsPage act(
        @Parameter(optionality = Optionality.MANDATORY) List<TvShow> tvShows,
        @Parameter(optionality = Optionality.MANDATORY) List<TvCharacter> tvCharacters
    ) {
        _NullSafe.stream(tvCharacters)
            .forEach(item->messageService.informUser(item.getName()));
        return holder;
    }

    @MemberSupport public List<TvShow> defaultTvShows(Parameters params) {
        return _Lists.of(holder.getFirstParamDefault());
    }

    @MemberSupport public List<TvCharacter> defaultTvCharacters(Parameters params) {
        return choicesTvCharacters(params);         // <.> <.> fill in all that are possible based on the first param from the UI dialog
    }

    @MemberSupport public List<TvCharacter> choicesTvCharacters(Parameters params) {
        val tvShowsSelected = params.tvShows();     // <.> <.> as selected in first param
        if(_NullSafe.isEmpty(tvShowsSelected)) {
            return Collections.emptyList();
        }
        return holder.getItems()
                .stream()
                .filter(item->tvShowsSelected.contains(item.getTvShow()))
                .collect(Collectors.toList());
    }
}

