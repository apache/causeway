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
package org.apache.isis.commons.internal.debug.xray;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;

import javax.swing.JTextArea;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.functional.IndexedConsumer;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.debug.xray.XrayDataModel.LogEntry;
import org.apache.isis.commons.internal.debug.xray.graphics.CallStackDiagram;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
final class _CallStackMerger {

    private final Can<LogEntry> logEntries;
    private final AtomicBoolean initialized = new AtomicBoolean(false);
    private CallStackDiagram callStackDiagram;

    void render(final JTextArea textArea) {
        if(!initialized.get()) {
            initialize();
            initialized.set(true);
        }
        callStackDiagram.render(textArea);
    }

    static interface IntTreeVisitor {
        void accept(int level, int value);
    }

    @RequiredArgsConstructor
    static class IntTreeNode {
        final int value;
        final int level;
        final IntTreeNode parent;
        final List<IntTreeNode> children = new ArrayList<>();
        IntTreeNode addChild(final int value) {
            IntTreeNode child;
            children.add(child = new IntTreeNode(value, level+1, this));
            return child;
        }
        static IntTreeNode newRoot(final int value) {
            return new IntTreeNode(value, 0, null);
        }
        void visitDepthFirst(final IntTreeVisitor visitor) {
            visitor.accept(level, value);
            for(val child : children) {
                child.visitDepthFirst(visitor);
            }
        }
        void visitBreadthFirst(final IntTreeVisitor visitor) {
            val queue = new ArrayDeque<IntTreeNode>();
            queue.add(this);
            IntTreeNode currentNode;
            while (!queue.isEmpty()) {
                currentNode = queue.remove();
                visitor.accept(currentNode.level, currentNode.value);
                queue.addAll(currentNode.children);
            }
        }

        @Override
        public String toString() {
            return print(i->""+i).toString();
        }

        private StringBuilder print(final IntFunction<String> valueMapper) {
            val sb = new StringBuilder();
            print(valueMapper, sb, "", "");
            return sb;
        }

        private void print(final IntFunction<String> valueMapper,
                final StringBuilder buffer, final String prefix, final String childrenPrefix) {
            buffer.append(prefix);
            buffer.append(valueMapper.apply(value));
            buffer.append('\n');
            for (Iterator<IntTreeNode> it = children.iterator(); it.hasNext();) {
                IntTreeNode next = it.next();
                if (it.hasNext()) {
                    next.print(valueMapper, buffer, childrenPrefix + "├─ ", childrenPrefix + "│  ");
                } else {
                    next.print(valueMapper, buffer, childrenPrefix + "└─ ", childrenPrefix + "   ");
                }
            }
        }

    }

    private void initialize() {

        val executionNodeSet = _Sets.<String>newHashSet(); // temporary helper
        val executionNodeMap = _Maps.<Integer, String>newHashMap(); // StackStraceElement by unique id

        val executionLanes = new ArrayList<int[]>();

        logEntries.forEach(logEntry->{
            //System.err.printf("joining %s%n", logEntry.getLabel());

            val executionLane = new int[logEntry.getData().size()];
            executionLanes.add(executionLane);

            Can.ofCollection(logEntry.getData()).reverse().stream()
            .map(StackTraceElement::toString).forEach(IndexedConsumer.zeroBased((index, se)->{
                val isNew = executionNodeSet.add(se);
                if(isNew) {
                    final int id = executionNodeSet.size();
                    executionNodeMap.put(id, se);
                    executionLane[index] = id;
                } else {
                    final int id = executionNodeMap.entrySet().stream()
                    .filter(entry->entry.getValue().equals(se))
                    .mapToInt(entry->(int)entry.getKey())
                    .findAny()
                    .orElseThrow();
                    executionLane[index] = id;
                }
            }));
        });

        val root = merge(executionLanes);
        callStackDiagram = new CallStackDiagram(root.print(id->{
            return _Exceptions.abbreviate(
                    executionNodeMap.getOrDefault(id, "root"),
                    "org.apache.isis");
        }).toString());
    }

    /**
     * executionLanes look like
     * [1, 2, 3, 4, 5, 6]
     * [1, 2, 3, 7, 8, 6, 9]
     * [1, 2, 3, 4, 8]
     * ...
     */
    static IntTreeNode merge(final List<int[]> executionLanes) {
        val root = IntTreeNode.newRoot(-1);
        executionLanes.forEach(lane->{
            var node = root;
            for(int id : lane) {
                val equalNode = node.children.stream().filter(child->child.value==id).findAny();
                if(!equalNode.isPresent()) {
                    node = node.addChild(id);
                } else {
                    node = equalNode.get();
                }
            }
        });
        return root;
    }

}
