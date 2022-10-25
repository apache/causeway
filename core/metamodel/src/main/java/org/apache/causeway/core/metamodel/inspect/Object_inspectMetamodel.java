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

import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.graph.tree.TreeNode;
import org.apache.causeway.applib.graph.tree.TreePath;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.layout.LayoutConstants;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.applib.services.metamodel.Config;
import org.apache.causeway.applib.services.metamodel.MetaModelService;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.inspect.model.MMNodeFactory;
import org.apache.causeway.core.metamodel.inspect.model.MMTreeAdapter;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Action(
        domainEvent = Object_inspectMetamodel.ActionDomainEvent.class,
        semantics = SemanticsOf.SAFE,
        commandPublishing = Publishing.DISABLED,
        executionPublishing = Publishing.DISABLED,
        restrictTo = RestrictTo.PROTOTYPING
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

    private final Object domainObject; // mixee

    public static class ActionDomainEvent
    extends org.apache.causeway.applib.CausewayModuleApplib.ActionDomainEvent<Object_inspectMetamodel> {}

    @MemberSupport public Object act() {

        final Optional<LogicalType> logicalTypeIfAny = metaModelService
                .lookupLogicalTypeByClass(domainObject.getClass());
        if(!logicalTypeIfAny.isPresent()) {
            messageService.warnUser("Unknown class, unable to export");
            return null;
        }
        final String namespace = logicalTypeIfAny.get().getLogicalTypeName();

        val config = Config.builder()
                .ignoreFallbackFacets(true)
                .ignoreAbstractClasses(true)
                .ignoreInterfaces(true)
                .ignoreBuiltInValueTypes(true)
                .includeTitleAnnotations(true)
                .includeShadowedFacets(true)
                .build()
                .withNamespacePrefix(namespace);

        val metamodelDto = metaModelService.exportMetaModel(config);

        val className = domainObject.getClass().getName();

        val domainClassDto = metamodelDto.getDomainClassDto()
            .stream()
            .filter(classDto->Objects.equals(classDto.getId(), className))
            .findFirst()
            .orElseThrow(_Exceptions::noSuchElement);

        val root = MMNodeFactory.type(domainClassDto, null);

        val tree = TreeNode.lazy(root, MMTreeAdapter.class);

        // Initialize view-model nodes of the entire tree,
        // because as it stands, all the type information gets cleared,
        // after the jax-b model got de-serialized.
        tree.streamDepthFirst()
        .map(TreeNode::getValue)
        .forEach(node->node.title());

        tree.expand(TreePath.of(0)); // expand the root node
        return tree;
    }

    @Inject private MetaModelService metaModelService;
    @Inject private MessageService messageService;

}
