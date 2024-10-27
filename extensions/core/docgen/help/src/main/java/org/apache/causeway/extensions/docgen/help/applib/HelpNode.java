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
package org.apache.causeway.extensions.docgen.help.applib;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.graph.tree.TreePath;
import org.apache.causeway.valuetypes.asciidoc.applib.value.AsciiDoc;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Represents a node in the tree made of topics and {@link HelpPage}s.
 *
 * @since 2.x {@index}
 */
public interface HelpNode {

    public enum HelpNodeType {
        TOPIC,
        PAGE;
    }

    TreePath getPath();
    String getTitle();
    HelpNodeType getHelpNodeType();
    AsciiDoc getContent();

    // -- PARENT CHILD RELATIONS

    Optional<HelpTopic> getParent();
    int childCount();
    Stream<HelpNode> streamChildNodes();

    Optional<HelpNode> getChildNode(int index);

    // -- IMPLEMENTATIONS

    /**
     * Topic node of the tree, which may contain sub-{@link HelpTopic}s or {@link HelpPageNode}s.
     */
    @RequiredArgsConstructor
    public static final class HelpTopic
        implements HelpNode {

        public static HelpTopic root(final String topic) {
            return parented(null, topic);
        }

        public static HelpTopic parented(final @Nullable HelpTopic parentTopic, final String topic) {
            return new HelpTopic(parentTopic, nextChildPathOf(parentTopic), new ArrayList<>(), topic);
        }

        private final @Nullable HelpTopic parentTopic;

        @Getter(onMethod_={@Override})
        private final @NonNull TreePath path;

        @Getter
        private final List<HelpNode> childNodes;

        @Getter(onMethod_={@Override})
        private final String title;

        public HelpTopic subTopic(final String topic) {
            var childTopic = HelpTopic.parented(this, topic);
            childNodes.add(childTopic);
            return childTopic;
        }

        @Override
        public HelpNodeType getHelpNodeType() {
            return HelpNodeType.TOPIC;
        }

        @Override
        public AsciiDoc getContent() {
            return AsciiDoc.valueOf("todo: summarize children"); // TODO
        }

        public <T extends HelpPage> HelpTopic addPage(final T helpPage) {
            childNodes.add(new HelpPageNode(this, nextChildPathOf(this), helpPage));
            return this;
        }

        @Override
        public Optional<HelpTopic> getParent() {
            return Optional.ofNullable(parentTopic);
        }

        @Override
        public int childCount() {
            return childNodes.size();
        }

        @Override
        public Stream<HelpNode> streamChildNodes() {
            return childNodes.stream();
        }

        /**
         * Resolves given {@link TreePath} to its corresponding {@link HelpNode} if possible.
         */
        public Optional<HelpNode> lookup(final TreePath treePath) {
            var root = rootTopic();
            if(treePath.isRoot()) {
                return Optional.of(root);
            }

            var stack = new Stack<HelpNode>();
            stack.push(root);

            treePath.streamPathElements()
            .skip(1) // skip first path element which is always '0' and corresponds to the root, which we already handled above
            .forEach(pathElement->{
                if(stack.isEmpty()) return; // an empty stack corresponds to a not found state

                var currentNode = stack.peek();
                var child = currentNode.getChildNode(pathElement).orElse(null);

                if(child!=null) {
                    stack.push(child);
                } else {
                    stack.clear(); // not found
                }
            });

            return stack.isEmpty()
                    ? Optional.empty()
                    : Optional.of(stack.peek());
        }

        @Override
        public String toString() {
            return String.format("HelpTopic[%s, childCount=%s]", getTitle(), childCount());
        }

        // -- HELPER

        private HelpTopic rootTopic() {
            var node = this;
            while(node.getParent().isPresent()) {
                node = node.getParent().get();
            }
            return node;
        }

        private static @NonNull TreePath nextChildPathOf(final @Nullable HelpTopic parentTopic) {
            if(parentTopic==null) {
                return TreePath.root();
            }
            return parentTopic.getPath().append(parentTopic.childCount());
        }

        @Override
        public Optional<HelpNode> getChildNode(final int index) {
            return index<childCount()
                ? Optional.of(getChildNodes().get(index))
                : Optional.empty();
        }
    }

    /**
     * Leaf node of the tree, referencing a {@link HelpPage}.
     */
    @RequiredArgsConstructor
    public static final class HelpPageNode
        implements HelpNode {

        private final @NonNull HelpTopic parentTopic;

        @Getter(onMethod_={@Override})
        private final @NonNull TreePath path;

        @Getter
        private final HelpPage helpPage;

        @Override
        public HelpNodeType getHelpNodeType() {
            return HelpNodeType.PAGE;
        }

        @Override
        public String getTitle() {
            return helpPage.getTitle();
        }

        @Override
        public AsciiDoc getContent() {
            return helpPage.getContent();
        }

        @Override
        public Optional<HelpTopic> getParent() {
            return Optional.of(parentTopic);
        }

        @Override
        public int childCount() {
            return 0;
        }

        @Override
        public Stream<HelpNode> streamChildNodes() {
            return Stream.empty();
        }

        @Override
        public Optional<HelpNode> getChildNode(final int index) {
            return Optional.empty();
        }

        @Override
        public String toString() {
            return String.format("HelpPageNode[%s]", getTitle());
        }

    }

}
