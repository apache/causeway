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

import java.nio.file.FileSystems;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.graph.tree.TreeNode;
import org.apache.causeway.applib.graph.tree.TreePath;
import org.apache.causeway.applib.services.factory.FactoryService;

import lombok.RequiredArgsConstructor;

@SuppressWarnings("unchecked")
//tag::sessionTree[]
@Service
@Named("demo.FileTreeNodeService")
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class FileTreeNodeService {

    final Provider<HttpSession> httpSessionProvider;
    final FactoryService factoryService;

    public TreeNode<FileNodeVm> sessionTree() {
        var session = httpSessionProvider.get();
        var cacheKey = TreeNode.class.getName();
        var tree = (TreeNode<FileNodeVm>) session.getAttribute(cacheKey);
        if(tree == null) {
            tree = newTree(factoryService);
            session.setAttribute(cacheKey, tree);
        }
        return tree;

    }
//end::sessionTree[]

//tag::newTree[]
    private static TreeNode<FileNodeVm> newTree(final FactoryService factoryService) {
        TreeNode<FileNodeVm> tree;
        var rootFile = FileSystems.getDefault().getRootDirectories().iterator().next().toFile();
        var rootNode = new FileNodeVm(rootFile);
        tree = TreeNode.root(rootNode, FileSystemTreeAdapter.class, factoryService);
        tree.expand(TreePath.of(0)); // expand the root node
        return tree;
    }
//end::newTree[]

//tag::sessionTree[]
    // ...
}
//end::sessionTree[]
