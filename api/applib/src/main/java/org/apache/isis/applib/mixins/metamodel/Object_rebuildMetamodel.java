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
package org.apache.isis.applib.mixins.metamodel;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.mixins.layout.LayoutMixinConstants;
import org.apache.isis.applib.services.metamodel.MetaModelService;

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
        restrictTo = RestrictTo.PROTOTYPING,
        associateWith = LayoutMixinConstants.METADATA_LAYOUT_GROUPNAME
)
@ActionLayout(
        cssClassFa = "fa-sync",
        position = ActionLayout.Position.PANEL,
        sequence = "800.1"
)
@RequiredArgsConstructor
public class Object_rebuildMetamodel {

    public static class ActionDomainEvent
    extends org.apache.isis.applib.IsisModuleApplib.ActionDomainEvent<Object_rebuildMetamodel> {}

    private final Object holder;

    public Object act() {
        metaModelService.rebuild(holder.getClass());
        return holder;
    }

    @Inject MetaModelService metaModelService;


}
