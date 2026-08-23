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
package demoapp.dom.progmodel.actions.autocomplete;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.MinLength;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.SemanticsOf;

import demoapp.dom.progmodel.actions.TvCharacter;
import lombok.RequiredArgsConstructor;

//tag::class[]
@Action(semantics = SemanticsOf.IDEMPOTENT)
@RequiredArgsConstructor
public class ActionAutoCompletePage_selectTvCharacters {

    private final ActionAutoCompletePage page;

    @MemberSupport public ActionAutoCompletePage act(
        @Parameter(optionality = Optionality.MANDATORY)
        final List<TvCharacter> tvCharacters                                // <.>
    ) {
        page.getSelectedTvCharacters().clear();
        page.getSelectedTvCharacters().addAll(tvCharacters);
        return page;
    }

    @MemberSupport public Collection<TvCharacter> autoCompleteTvCharacters( // <1>
        @MinLength(1)
        final String search                                                 // <.>
    ) {
        return page.getTvCharacters()
                .stream()
                .filter(x -> x.getName().contains(search))                  // <2>
                .collect(Collectors.toList());
    }
}
//end::class[]

