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
package demoapp.dom.domain.actions.Action.executionPublishing;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.SemanticsOf;

//tag::class[]
@Action(
    executionPublishing = Publishing.ENABLED         // <.>
    , semantics = SemanticsOf.IDEMPOTENT
    , associateWith = "property"
)
@ActionLayout(
    named = "Mixin Update Property"
    , describedAs = "@Action(publishing = ENABLED)"
    , sequence = "2"
)
public class ActionExecutionPublishingJdo_mixinUpdateProperty {
    // ...
//end::class[]

    private final ActionExecutionPublishingJdo actionPublishingJdo;

    public ActionExecutionPublishingJdo_mixinUpdateProperty(ActionExecutionPublishingJdo actionPublishingJdo) {
        this.actionPublishingJdo = actionPublishingJdo;
    }

//tag::class[]
    public ActionExecutionPublishingJdo act(final String value) {
        actionPublishingJdo.setProperty(value);
        return actionPublishingJdo;
    }
    public String default0Act() {
        return actionPublishingJdo.getProperty();
    }
}
//end::class[]
