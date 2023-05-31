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
package demoapp.dom.domain.properties.PropertyLayout.navigable;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.graph.tree.TreeNode;

import lombok.RequiredArgsConstructor;

@SuppressWarnings("unused")
/* avoiding class-path scan bean-naming clash with the other FileNodeVm_tree
 * and also not using @Named here,
 * because this would trigger a meta-model warning when used on mixins */
@Component("demo.navigable.FileNodeVm_tree")
//tag::class[]
@Property
@RequiredArgsConstructor
public class FileNodeVm_tree {

    private final FileNodeVm fileNodeVm;

    public TreeNode<FileNodeVm> prop(){
        return fileTreeNodeService.sessionTree();
    }

    @Inject
    FileTreeNodeService fileTreeNodeService;
}
//end::class[]
