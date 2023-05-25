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

import java.util.List;
import java.util.Set;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Parameter;

import lombok.RequiredArgsConstructor;

import demoapp.dom.progmodel.actions.TvCharacter;

//tag::class[]
@Action
@RequiredArgsConstructor
public class ActionVetoPage_selectTvCharactersIfNotDisabled {

    private final ActionVetoPage page;

    @MemberSupport public ActionVetoPage act(
            @Parameter(optionality = Optionality.OPTIONAL)
            final List<TvCharacter> tvCharacters
    ) {
        page.getSelectedTvCharacters().addAll(tvCharacters);
        return page;
    }

    @MemberSupport public String disableAct() {         // <.>
        return page.isActionDisabled()
                ? "Action has been disabled"
                : null;
    }
    @MemberSupport public Set<TvCharacter> choicesTvCharacters() {
        return page.getTvCharacters();
    }
}
//end::class[]

