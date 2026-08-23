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
package demoapp.dom.progmodel.actions.validate;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.SemanticsOf;

import demoapp.dom.progmodel.actions.TvCharacter;
import lombok.RequiredArgsConstructor;

//tag::class[]
@Action(semantics = SemanticsOf.IDEMPOTENT)
@RequiredArgsConstructor
public class ActionValidatePage_selectTvCharacters {

    private final ActionValidatePage page;

    @MemberSupport public ActionValidatePage act(                   // <.>
        @Parameter(optionality = Optionality.MANDATORY)
        final List<TvCharacter> maleTvCharacters,
        @Parameter(optionality = Optionality.MANDATORY)
        final List<TvCharacter> femaleTvCharacters
    ) {
        // ...
//end::class[]
        page.getSelectedTvCharacters().clear();
        page.getSelectedTvCharacters().addAll(maleTvCharacters);
        page.getSelectedTvCharacters().addAll(femaleTvCharacters);
        return page;
//tag::class[]
    }

    @MemberSupport public Collection<TvCharacter> choicesMaleTvCharacters() {
        return pageTvCharacters(TvCharacter.Sex.MALE);
    }
    @MemberSupport public Collection<TvCharacter> choicesFemaleTvCharacters() {
        return pageTvCharacters(TvCharacter.Sex.FEMALE);
    }
    @MemberSupport public String validateAct(                       // <1>
            final List<TvCharacter> maleTvCharacters,
            final List<TvCharacter> femaleTvCharacters
    ) {
        return maleTvCharacters.size() == femaleTvCharacters.size() // <.>
                ? null
                : "Must have the same number of male and female characters";
    }
    // ...
//end::class[]
    private List<TvCharacter> pageTvCharacters(TvCharacter.Sex sex) {
        return page.getTvCharacters().stream()
                .filter(x -> x.getSex() == sex).collect(Collectors.toList());
    }
//tag::class[]
}
//end::class[]

