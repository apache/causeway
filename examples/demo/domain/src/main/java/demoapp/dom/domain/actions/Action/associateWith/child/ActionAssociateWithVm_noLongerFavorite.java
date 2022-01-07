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
package demoapp.dom.domain.actions.Action.associateWith.child;

import java.util.Objects;

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;

import lombok.RequiredArgsConstructor;

import demoapp.dom.domain.actions.Action.associateWith.ActionAssociateWithVm;


//tag::class[]
@Action(
    choicesFrom = "favorites"                                   // <.>
)
@ActionLayout(
    describedAs =
        "@Action(choicesFrom = \"favorites\") " +
        "@ActionLayout(sequence = \"2\")"
    , sequence = "2"                                            // <.>
)
@RequiredArgsConstructor
public class ActionAssociateWithVm_noLongerFavorite {

    private final ActionAssociateWithVm actionAssociateWithVm;

    public ActionAssociateWithVm act(ActionAssociateWithChildVm childVm) {
        actionAssociateWithVm.getFavorites()
                .removeIf(y -> Objects.equals(childVm.getValue(), y.getValue()));
        actionAssociateWithVm.getChildren().add(childVm);

        return actionAssociateWithVm;
    }
    // no choices or autoComplete required                      // <.>

}
//end::class[]
