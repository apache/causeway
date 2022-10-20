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
package demoapp.dom.domain.actions.Action.associateWith;

import java.util.List;
import java.util.Objects;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;

import demoapp.dom.domain.actions.Action.associateWith.child.ActionAssociateWithChildVm;
import lombok.RequiredArgsConstructor;


//tag::class[]
@Action(
    choicesFrom = "favorites"                                   // <.>
)
@ActionLayout(
    describedAs =
            "@Action(choicesFrom = \"favorites\") " +
            "@ActionLayout(sequence = \"3\")"
    , sequence = "3"                                            // <.>
)
@RequiredArgsConstructor
public class ActionAssociateWithVm_noLongerFavorites {

    private final ActionAssociateWithVm actionAssociateWithVm;

    @MemberSupport public ActionAssociateWithVm act(final List<ActionAssociateWithChildVm> children) {
        children.forEach(childVm -> {
            actionAssociateWithVm.getFavorites()
                    .removeIf(y -> Objects.equals(childVm.getValue(), y.getValue()));
        });
        actionAssociateWithVm.getChildren().addAll(children);

        return actionAssociateWithVm;
    }
    // no choices or autoComplete required                      // <.>
}
//end::class[]
