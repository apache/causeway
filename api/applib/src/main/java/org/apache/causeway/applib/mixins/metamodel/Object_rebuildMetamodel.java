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
package org.apache.causeway.applib.mixins.metamodel;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.layout.LayoutConstants;
import org.apache.causeway.applib.services.metamodel.MetaModelService;

import lombok.RequiredArgsConstructor;

/**
 *  Provides the ability to discard the current internal metamodel data for
 *  the domain class of the rendered object, and recreate from code and other
 *  sources (most notably, layout XML data).
 *
 * @since 1.x {@index}
 */
@Action(
        domainEvent = Object_rebuildMetamodel.ActionDomainEvent.class,
        semantics = SemanticsOf.IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        executionPublishing = Publishing.DISABLED,
        restrictTo = RestrictTo.PROTOTYPING
)
@ActionLayout(
        cssClassFa = "fa-sync",
        describedAs = "Rebuilds the framework's internal metamodel of this object's domain class",
        fieldSetId = LayoutConstants.FieldSetId.METADATA,
        position = ActionLayout.Position.PANEL,
        sequence = "800.1"
)
//mixin's don't need a logicalTypeName
@RequiredArgsConstructor
public class Object_rebuildMetamodel {

    public static class ActionDomainEvent
    extends org.apache.causeway.applib.CausewayModuleApplib.ActionDomainEvent<Object_rebuildMetamodel> {}

    private final Object holder;

    @MemberSupport public Object act() {
        metaModelService.rebuild(holder.getClass());
        return holder;
    }

    @Inject MetaModelService metaModelService;


}
