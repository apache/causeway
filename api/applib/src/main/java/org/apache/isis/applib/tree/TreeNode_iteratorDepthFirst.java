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
package org.apache.isis.applib.tree;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

class TreeNode_iteratorDepthFirst<T> implements Iterator<TreeNode<T>> {

    private Stack<TreeNode<T>> stack = new Stack<>();
    private TreeNode<T> next;

    TreeNode_iteratorDepthFirst(TreeNode<T> treeNode) {
        next = treeNode;
    }

    @Override
    public boolean hasNext() {
        return next!=null;
    }

    @Override
    public TreeNode<T> next() {
        if(next==null) {
            throw new NoSuchElementException("Iterator has run out of elements.");
        }
        final TreeNode<T> result = next;
        next = fetchNext(next);
        return result;
    }

    // -- HELPER

    private TreeNode<T> fetchNext(TreeNode<T> current) {
        if(!current.isLeaf()) {
            pushChildrenToStackInReverseOrder(current);
        }
        return stack.isEmpty() ? null : stack.pop();
    }

    private Stack<TreeNode<T>> fifo = new Stack<>(); // declared as field only to reduce heap pollution

    private void pushChildrenToStackInReverseOrder(TreeNode<T> node) {

        node.streamChildren()
        .forEach(fifo::push);

        while(!fifo.isEmpty()) {
            stack.push(fifo.pop());
        }
    }


}
