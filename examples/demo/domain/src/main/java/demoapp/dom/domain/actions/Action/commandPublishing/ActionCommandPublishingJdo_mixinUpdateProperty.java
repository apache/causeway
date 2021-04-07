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
package demoapp.dom.domain.actions.Action.commandPublishing;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.SemanticsOf;

//tag::class[]
@Action(
    commandPublishing = Publishing.ENABLED        // <.>
    , semantics = SemanticsOf.IDEMPOTENT
    , associateWith = "property"
)
@ActionLayout(
    named = "Mixin Update Property"
    , describedAs = "@Action(command = ENABLED)"
    , sequence = "2"
)
public class ActionCommandPublishingJdo_mixinUpdateProperty {
    // ...
//end::class[]

    private final ActionCommandPublishingJdo actionCommandJdo;

    public ActionCommandPublishingJdo_mixinUpdateProperty(ActionCommandPublishingJdo actionCommandJdo) {
        this.actionCommandJdo = actionCommandJdo;
    }

    public ActionCommandPublishingJdo act(final String value) {
        actionCommandJdo.setProperty(value);
        return actionCommandJdo;
    }
    public String default0Act() {
        return actionCommandJdo.getProperty();
    }
//tag::class[]
}
//end::class[]
