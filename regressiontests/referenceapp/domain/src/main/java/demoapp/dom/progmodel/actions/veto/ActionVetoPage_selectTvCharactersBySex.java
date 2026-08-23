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
package demoapp.dom.progmodel.actions.veto;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;

import demoapp.dom.progmodel.actions.TvCharacter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.Accessors;

//tag::class[]
@Action
@RequiredArgsConstructor
public class ActionVetoPage_selectTvCharactersBySex {

    private final ActionVetoPage page;

    @MemberSupport public ActionVetoPage act(
            @Parameter(optionality = Optionality.MANDATORY)
            final TvCharacter.Sex sex,                                              // <.>
            @Parameter(optionality = Optionality.OPTIONAL)
            final List<TvCharacter> maleTvCharacters,
            @Parameter(optionality = Optionality.OPTIONAL)
            final List<TvCharacter> femaleTvCharacters
    ) {
        page.getSelectedTvCharacters().addAll(maleTvCharacters);
        page.getSelectedTvCharacters().addAll(femaleTvCharacters);
        return page;
    }

    @Value @Accessors(fluent = true)                                                // <1>
    static class Parameters {
        final TvCharacter.Sex sex;
        final List<TvCharacter> maleTvCharacters;
        final List<TvCharacter> femaleTvCharacters;
    }

    @MemberSupport public String disableMaleTvCharacters(Parameters parameters) {   // <.>
        return parameters.sex() == null ? "Sex not yet selected" : null;
    }
    @MemberSupport public String disableFemaleTvCharacters(Parameters parameters) { // <2>
        return parameters.sex() == null ? "Sex not yet selected" : null;
    }
    @MemberSupport public boolean hideMaleTvCharacters(Parameters parameters) {     // <.>
        return parameters.sex() == TvCharacter.Sex.FEMALE;
    }
    @MemberSupport public boolean hideFemaleTvCharacters(Parameters parameters) {   // <3>
        return parameters.sex() == TvCharacter.Sex.MALE;
    }
    @MemberSupport public java.util.Collection<TvCharacter> choicesMaleTvCharacters() {
        return pageTvCharacters(TvCharacter.Sex.MALE);
    }
    @MemberSupport public Collection<TvCharacter> choicesFemaleTvCharacters() {
        return pageTvCharacters(TvCharacter.Sex.FEMALE);
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
