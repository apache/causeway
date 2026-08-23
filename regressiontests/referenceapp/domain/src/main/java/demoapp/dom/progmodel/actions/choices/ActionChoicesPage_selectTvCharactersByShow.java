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
package demoapp.dom.progmodel.actions.choices;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.SemanticsOf;

import demoapp.dom.progmodel.actions.TvCharacter;
import demoapp.dom.progmodel.actions.TvShow;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import lombok.experimental.Accessors;

//tag::class[]
@Action(semantics = SemanticsOf.SAFE)
@RequiredArgsConstructor
public class ActionChoicesPage_selectTvCharactersByShow {

    private final ActionChoicesPage page;

    @MemberSupport public ActionChoicesPage act(
        @Parameter(optionality = Optionality.MANDATORY)
        final TvShow tvShow,                                        // <.>
        @Parameter(optionality = Optionality.MANDATORY)
        final List<TvCharacter> tvCharacters
    ) {
        page.getSelectedTvCharacters().clear();
        page.getSelectedTvCharacters().addAll(tvCharacters);
        return page;
    }

    @Value @Accessors(fluent = true)
    static class Parameters {                                       // <.>
        TvShow tvShow;
        List<TvCharacter> tvCharacters;
    }

    @MemberSupport public List<TvCharacter> choicesTvCharacters(
            final Parameters params                                 // <2>
    ) {
        var tvShowSelected = params.tvShow();                       // <.>
        return page.getTvCharacters()
                .stream()
                .filter(tvCharacter -> tvShowSelected == tvCharacter.getTvShow())
                .collect(Collectors.toList());
    }
}
//end::class[]
