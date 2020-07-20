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
package demoapp.dom.annotLayout.PropertyLayout.navigable;

import java.nio.file.FileSystems;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.graph.tree.TreeNode;
import org.apache.isis.applib.graph.tree.TreePath;

import lombok.val;

//tag::sessionTree[]
@Service
@Named("demo.FileTreeNodeService")
public class FileTreeNodeService {

    public TreeNode<FileNodeVm> sessionTree() {
        TreeNode<FileNodeVm> tree = (TreeNode<FileNodeVm>) httpSessionProvider.get().getAttribute(TreeNode.class.getName());
        if(tree == null) {
            tree = newTree();
            httpSessionProvider.get().setAttribute(TreeNode.class.getName(), tree);
        }
        return tree;
    }
//end::sessionTree[]

//tag::newTree[]
    private static TreeNode<FileNodeVm> newTree() {
        TreeNode<FileNodeVm> tree;
        val rootFile = FileSystems.getDefault().getRootDirectories().iterator().next().toFile();
        val rootNode = new FileNodeVm(rootFile);
        tree = TreeNode.lazy(rootNode, FileSystemTreeAdapter.class);
        tree.expand(TreePath.of(0)); // expand the root node
        return tree;
    }
//end::newTree[]


//tag::sessionTree[]

    // ...

    @Inject
    private Provider<HttpSession> httpSessionProvider;
}
//end::sessionTree[]
