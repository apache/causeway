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

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.SemanticsOf;

import demoapp.dom.domain.actions.Action.executionPublishing.jdo.ActionExecutionPublishingJdo;

//tag::class[]
@ActionExecutionPublishingDisabledMetaAnnotation     // <.>
@Action(
    executionPublishing = Publishing.ENABLED         // <.>
    , semantics = SemanticsOf.IDEMPOTENT
)
@ActionLayout(
    named = "Mixin Update Property"
    , describedAs =
        "@ActionPublishingDisabledMetaAnnotation " +
        "@Action(publishing = ENABLED)"
    , associateWith = "propertyMetaAnnotatedOverridden"
    , sequence = "2"
)
public class ActionExecutionPublishingEntity_mixinUpdatePropertyMetaAnnotationOverridden {
    // ...
//end::class[]

    private final ActionExecutionPublishingEntity actionPublishingEntity;

    public ActionExecutionPublishingEntity_mixinUpdatePropertyMetaAnnotationOverridden(final ActionExecutionPublishingJdo actionPublishingJdo) {
        this.actionPublishingEntity = actionPublishingJdo;
    }

//tag::class[]
    @MemberSupport public ActionExecutionPublishingEntity act(final String value) {
        actionPublishingEntity.setPropertyMetaAnnotatedOverridden(value);
        return actionPublishingEntity;
    }
    @MemberSupport public String default0Act() {
        return actionPublishingEntity.getPropertyMetaAnnotatedOverridden();
    }
}
//end::class[]
