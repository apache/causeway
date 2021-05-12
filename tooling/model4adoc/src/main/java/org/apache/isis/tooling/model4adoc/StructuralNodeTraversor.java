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
package org.apache.isis.tooling.model4adoc;

import org.asciidoctor.ast.StructuralNode;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.commons.internal.base._Refs.BooleanReference;

import lombok.val;

/**
 * Depth-first node traversing. Use to iterate through all nodes under and including the specified root node.
 */
final class StructuralNodeTraversor {

    /**
     * Start a depth-first traverse of the root and all of its descendants.
     * @param visitor Node visitor.
     * @param root the root node point to traverse.
     */
    public static void depthFirst(StructuralNodeVisitor visitor, StructuralNode root) {
        traverse(visitor, root, 0, _Refs.booleanRef(true));
    }

    // -- HELPER

    private static void traverse(
            final StructuralNodeVisitor visitor,
            final StructuralNode node,
            final int depth,
            final BooleanReference continueTraverse) {

        if(continueTraverse.isFalse()) {
            return;
        }

        val continueVisit = visitor.head(node, depth);
        if(!continueVisit) {
            continueTraverse.update(__->false);
        }

        val blocks = node.getBlocks();

        if(!_NullSafe.isEmpty(blocks)) {
            for(val subNode : blocks) {
                traverse(visitor, subNode, depth+1, continueTraverse);
                if(continueTraverse.isFalse()) {
                    break;
                }
            }
        }

        visitor.tail(node, depth);

    }

}
