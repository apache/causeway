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

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;
import org.apache.causeway.applib.annotation.SemanticsOf;

import lombok.RequiredArgsConstructor;

import demoapp.dom.progmodel.actions.TvCharacter;
import demoapp.dom.progmodel.actions.TvShow;

//tag::class[]
@Action(semantics = SemanticsOf.IDEMPOTENT)
@RequiredArgsConstructor
public class ActionValidatePage_selectTvCharacterByShow {

    private final ActionValidatePage page;

    @MemberSupport public ActionValidatePage act(
        @Parameter(optionality = Optionality.MANDATORY)
        final TvCharacter tvCharacter                       // <.>
    ) {
        page.getSelectedTvCharacters().clear();
        page.getSelectedTvCharacters().add(tvCharacter);
        return page;
    }

    @MemberSupport public Collection<TvCharacter> choices0Act() {
        return page.getTvCharacters();
    }
    @MemberSupport public String validate0Act(              // <1>
        final TvCharacter tvCharacter
    ) {
        TvShow selectedTvShow = page.getSelectedTvShow();
        return selectedTvShow == null ||
               tvCharacter.getTvShow() == selectedTvShow
                ? null
                : "TV character doesn't appear in that show";
    }
}
//end::class[]

