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
package org.apache.isis.core.metamodel.inspect;

import java.util.Objects;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.graph.tree.TreeNode;
import org.apache.isis.applib.graph.tree.TreePath;
import org.apache.isis.applib.mixins.layout.LayoutMixinConstants;
import org.apache.isis.applib.services.metamodel.Config;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.inspect.model.MMNodeFactory;
import org.apache.isis.core.metamodel.inspect.model.MMTreeAdapter;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Action(
        domainEvent = Object_inspectMetamodel.ActionDomainEvent.class,
        semantics = SemanticsOf.SAFE,
        restrictTo = RestrictTo.PROTOTYPING)
@ActionLayout(
        cssClassFa = "fa-sitemap",
        position = ActionLayout.Position.PANEL_DROPDOWN)
@RequiredArgsConstructor
public class Object_inspectMetamodel {

    @Inject private MetaModelService metaModelService;
    //@Inject private SpecificationLoader specificationLoader;

    private final Object holder;

    public static class ActionDomainEvent
    extends org.apache.isis.applib.IsisModuleApplib.ActionDomainEvent<Object_inspectMetamodel> {}

    @MemberOrder(name = LayoutMixinConstants.METADATA_LAYOUT_GROUPNAME, sequence = "700.2.1")
    public Object act() {

        val pkg = holder.getClass().getPackage().getName();

        val config =
                new Config()
                .withIgnoreNoop()
                .withIgnoreAbstractClasses()
                .withIgnoreInterfaces()
                .withIgnoreBuiltInValueTypes()
                .withPackagePrefix(pkg);

        val metamodelDto = metaModelService.exportMetaModel(config);

        val className = holder.getClass().getName();

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


}
