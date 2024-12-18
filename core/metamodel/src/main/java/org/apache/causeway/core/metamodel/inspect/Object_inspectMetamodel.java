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
package org.apache.causeway.core.metamodel.inspect;

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.graph.tree.TreeNode;
import org.apache.causeway.applib.graph.tree.TreePath;
import org.apache.causeway.applib.layout.LayoutConstants;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.core.metamodel.inspect.model.MMNodeFactory;
import org.apache.causeway.core.metamodel.inspect.model.MMTreeAdapter;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import lombok.RequiredArgsConstructor;

@Action(
        commandPublishing = Publishing.DISABLED,
        domainEvent = Object_inspectMetamodel.ActionDomainEvent.class,
        executionPublishing = Publishing.DISABLED,
        restrictTo = RestrictTo.PROTOTYPING,
        semantics = SemanticsOf.SAFE
)
@ActionLayout(
        cssClassFa = "fa-sitemap",
        describedAs = "Open up a view of the metamodel of this object's domain class",
        fieldSetId = LayoutConstants.FieldSetId.METADATA,
        position = ActionLayout.Position.PANEL_DROPDOWN,
        sequence = "700.2.1"
)
//mixin's don't need a logicalTypeName
@RequiredArgsConstructor
public class Object_inspectMetamodel {

    @Inject private MessageService messageService;
    @Inject private SpecificationLoader specLoader;

    private final Object domainObject; // mixee

    public static class ActionDomainEvent
    extends org.apache.causeway.applib.CausewayModuleApplib.ActionDomainEvent<Object_inspectMetamodel> {}

    @MemberSupport public Object act() {

        var objSpec = specLoader.specForType(domainObject.getClass())
            .orElse(null);
        if(objSpec==null) {
            messageService.warnUser("Unknown class, unable to export");
            return null;
        }

        var root = MMNodeFactory.type(objSpec);

        return TreeNode.root(root, new MMTreeAdapter())
            .expand(TreePath.root()); // expand the root node
    }

}
