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
package org.apache.causeway.commons.internal.debug.xray;

import java.util.Map;
import java.util.Optional;
import java.util.Stack;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.commons.internal.collections._Maps;

record XrayModelSimple(
        MutableTreeNode rootNode,
        Map<String, MutableTreeNode> nodesById,
        Map<String, Stack<MutableTreeNode>> nodeStacksById) implements XrayModel {

    XrayModelSimple(
            final MutableTreeNode rootNode){
        this(rootNode, _Maps.newConcurrentHashMap(), _Maps.newConcurrentHashMap());
    }

    @Override
    public MutableTreeNode addContainerNode(
            final @NonNull MutableTreeNode parent,
            final @NonNull String name,
            final @NonNull String id,
            final @NonNull Stickiness stickiness) {
        var newNode = new DefaultMutableTreeNode();
        newNode.setUserObject(new HasIdAndLabel() {
            @Override public String getId() { return id; }
            @Override public String getLabel() { return name; }
            @Override public Stickiness getStickiness() { return stickiness; }
        });
        ((DefaultMutableTreeNode)parent).add(newNode);
        nodesById.put(id, newNode);
        return newNode;
    }

    @Override
    public <T extends XrayDataModel> T addDataNode(
            final @NonNull MutableTreeNode parent,
            final @NonNull T dataModel) {
        var newNode = new DefaultMutableTreeNode();
        newNode.setUserObject(dataModel);
        ((DefaultMutableTreeNode)parent).add(newNode);
        nodesById.put(dataModel.getId(), newNode);
        return dataModel;
    }

    @Override
    public Optional<MutableTreeNode> lookupNode(final String id) {
        return Optional.ofNullable(nodesById.get(id)) ;
    }

    @Override
    public void remove(final MutableTreeNode node) {
        var hasId = (HasIdAndLabel) ((DefaultMutableTreeNode)node).getUserObject();
        nodesById.remove(hasId.getId());
    }

    @Override
    public Stack<MutableTreeNode> getNodeStack(final String id) {
        return nodeStacksById.computeIfAbsent(id, __->new Stack<MutableTreeNode>());
    }

}
