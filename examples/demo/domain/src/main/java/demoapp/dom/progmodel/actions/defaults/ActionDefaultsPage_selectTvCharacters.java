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
package demoapp.dom.progmodel.actions.defaults;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

import demoapp.dom.progmodel.actions.TvCharacter;

//tag::class[]
@Action(semantics = SemanticsOf.IDEMPOTENT)
@RequiredArgsConstructor
public class ActionDefaultsPage_selectTvCharacters {

    private final ActionDefaultsPage page;

    @MemberSupport public ActionDefaultsPage act(
        @Parameter(optionality = Optionality.MANDATORY)
        final List<TvCharacter> tvCharacters                                        // <.>
    ) {
        page.getSelectedTvCharacters().clear();
        page.getSelectedTvCharacters().addAll(tvCharacters);
        return page;
    }

    @MemberSupport public Collection<TvCharacter> choicesTvCharacters() {
        return page.getTvCharacters();
    }
    @MemberSupport public List<TvCharacter> defaultTvCharacters() {                 // <1>
        TvCharacter.Sex preselectCharacterSex = page.getPreselectCharacterSex();    // <.>
        return choicesTvCharacters().stream()
                .filter(tvCharacter -> preselectCharacterSex == null ||
                                       tvCharacter.getSex() == preselectCharacterSex)
                .collect(Collectors.toList());
    }
}
//end::class[]

