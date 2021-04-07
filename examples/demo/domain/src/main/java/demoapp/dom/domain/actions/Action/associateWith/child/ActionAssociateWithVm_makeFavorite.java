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

import java.util.List;
import java.util.Objects;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;

import lombok.RequiredArgsConstructor;

import demoapp.dom.domain.actions.Action.associateWith.ActionAssociateWithVm;


//tag::class[]
@Action(
    associateWith = "favorites"                                 // <.>
)
@ActionLayout(
    describedAs =
        "@Action(" +
            "associateWith = \"favorites\"" +
            ", associateWithSequence = \"1\")"
    , sequence = "1"                                            // <.>
)
@RequiredArgsConstructor
public class ActionAssociateWithVm_makeFavorite {

    private final ActionAssociateWithVm actionAssociateWithVm;

    public ActionAssociateWithVm act(ActionAssociateWithChildVm childVm) {
        actionAssociateWithVm.getFavorites().add(childVm);
        actionAssociateWithVm.getChildren().removeIf(x -> Objects.equals(x.getValue(), childVm.getValue()));
        return actionAssociateWithVm;
    }
    public List<ActionAssociateWithChildVm> choices0Act() {     // <.>
        return actionAssociateWithVm.getChildren();
    }
}
//end::class[]
