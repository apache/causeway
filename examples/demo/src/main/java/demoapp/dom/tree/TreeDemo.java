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
package demoapp.dom.tree;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.graph.tree.TreeNode;
import org.apache.isis.applib.graph.tree.TreePath;

import lombok.val;

import demoapp.utils.DemoStub;

@DomainObject(nature = Nature.VIEW_MODEL, objectType = "demo.Tree")
public class TreeDemo extends DemoStub {

    // this is the actual view model rendered by the framework
    private final static TreeNode<FileNode> createTree() {
        val root = FileNodeFactory.defaultRoot();
        val tree = TreeNode.lazy(root, FileSystemTreeAdapter.class);
        tree.expand(TreePath.of(0)); // expand the root node
        return tree;
    }


    /**
     * @return the demo tree view model as a property  
     */
    public TreeNode<FileNode> getFileSystemTree() {
        return createTree();
    }

    /**
     * 
     * @return the demo tree view model for standalone rendering (as action result)
     */
    @ActionLayout(cssClassFa="fa-tree")
    @Action
    public TreeNode<FileNode> standalone(){
        return createTree();
    }

    @Override
    public void initDefaults() {

    }

}