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

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;

import org.apache.isis.commons.internal.exceptions._Exceptions;

/**
 * Node visitor interface. Provide an implementing class to {@link StructuralNodeTraversor} to iterate through nodes.
 * <p>
 * This interface provides two methods, {@code head} and {@code tail}. The head method is called when the node is first
 * seen, and the tail method when all of the node's children have been visited. As an example, head can be used to
 * create a start tag for a node, and tail to create the end tag.
 * </p>
 */
interface StructuralNodeVisitor {

    // - HEAD
    
    /**
     * Callback for when a node is first visited.
     *
     * @param node the node being visited.
     * @param depth the depth of the node, relative to the root node. E.g., the root node has depth 0, and a child node
     * of that will have depth 1.
     */
    default void head(StructuralNode node, int depth) {
        if(node instanceof Document) {
            documentHead((Document)node, depth);
            return;
        }
        if(node instanceof Table) {
            tableHead((Table)node, depth);
            return;
        }
        if(node instanceof org.asciidoctor.ast.List) {
            listHead((org.asciidoctor.ast.List)node, depth);
            return;
        }
        if(node instanceof ListItem) {
            listItemHead((ListItem)node, depth);
            return;
        }
        if(node instanceof Block) {
            blockHead((Block)node, depth);
            return;
        }    
        throw _Exceptions.unsupportedOperation("node type not supported %s", node.getClass());
    }

    // -- HEAD SPECIALISATIONS
    
    void documentHead(Document doc, int depth);

    void blockHead(Block block, int depth);

    void listHead(org.asciidoctor.ast.List list, int depth);
    
    void listItemHead(ListItem listItem, int depth);

    void tableHead(Table table, int depth);
    
   // -- TAIL

    /**
     * Callback for when a node is last visited, after all of its descendants have been visited.
     *
     * @param node the node being visited.
     * @param depth the depth of the node, relative to the root node. E.g., the root node has depth 0, and a child node
     * of that will have depth 1.
     */
    default void tail(StructuralNode node, int depth) {
        if(node instanceof Document) {
            documentTail((Document)node, depth);
            return;
        }
        if(node instanceof Table) {
            tableTail((Table)node, depth);
            return;
        }
        if(node instanceof org.asciidoctor.ast.List) {
            listTail((org.asciidoctor.ast.List)node, depth);
            return;
        }
        if(node instanceof ListItem) {
            listItemTail((ListItem)node, depth);
            return;
        }
        if(node instanceof Block) {
            blockTail((Block)node, depth);
            return;
        }    
        throw _Exceptions.unsupportedOperation("node type not supported %s", node.getClass());
    }
    
    // -- TAIL SPECIALISATIONS
    
    void documentTail(Document doc, int depth);

    void blockTail(Block block, int depth);

    void listTail(org.asciidoctor.ast.List list, int depth);
    
    void listItemTail(ListItem listItem, int depth);

    void tableTail(Table table, int depth);
    
    
}