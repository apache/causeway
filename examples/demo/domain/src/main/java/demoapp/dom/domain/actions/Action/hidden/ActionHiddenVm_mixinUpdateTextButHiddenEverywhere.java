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
package demoapp.dom.domain.actions.Action.hidden;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.annotation.Where;

import lombok.RequiredArgsConstructor;


//tag::class[]
@Action(
    semantics = SemanticsOf.SAFE
    , hidden = Where.EVERYWHERE     // <.>
)
@ActionLayout(
    describedAs =
        "@Action(hidden = Where.EVERYWHERE)"
    , associateWith = "otherText"
    , sequence = "4"
)
@RequiredArgsConstructor
public class ActionHiddenVm_mixinUpdateTextButHiddenEverywhere {

    private final ActionHiddenVm actionHiddenVm;

    @MemberSupport public ActionHiddenVm act(final String text) {
        actionHiddenVm.setOtherText(text);
        return actionHiddenVm;
    }
    @MemberSupport public String default0Act() {
        return actionHiddenVm.getOtherText();
    }
}
//end::class[]
