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

import org.apache.isis.applib.annotations.Action;
import org.apache.isis.applib.annotations.ActionLayout;
import org.apache.isis.applib.annotations.MemberSupport;
import org.apache.isis.applib.annotations.Publishing;
import org.apache.isis.applib.annotations.SemanticsOf;

import demoapp.dom.domain.actions.Action.executionPublishing.jdo.ActionExecutionPublishingJdo;

//tag::class[]
@Action(
    executionPublishing = Publishing.ENABLED         // <.>
    , semantics = SemanticsOf.IDEMPOTENT
)
@ActionLayout(
    named = "Mixin Update Property"
    , describedAs = "@Action(publishing = ENABLED)"
    , associateWith = "property"
    , sequence = "2"
)
public class ActionExecutionPublishingEntity_mixinUpdateProperty {
    // ...
//end::class[]

    private final ActionExecutionPublishingEntity actionPublishingEntity;

    public ActionExecutionPublishingEntity_mixinUpdateProperty(final ActionExecutionPublishingJdo actionPublishingJdo) {
        this.actionPublishingEntity = actionPublishingJdo;
    }

//tag::class[]
    @MemberSupport public ActionExecutionPublishingEntity act(final String value) {
        actionPublishingEntity.setProperty(value);
        return actionPublishingEntity;
    }
    @MemberSupport public String default0Act() {
        return actionPublishingEntity.getProperty();
    }
}
//end::class[]
