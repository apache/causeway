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
package org.apache.isis.tooling.j2adoc;

import java.util.Stack;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.StructuralNode;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import org.apache.isis.tooling.model4adoc.AsciiDocFactory;

import lombok.SneakyThrows;
import lombok.val;

final class HtmlToAsciiDoc {
    
    @SneakyThrows
    public static Document body(Element body, int level) {
        
        val adoc = AsciiDocFactory.doc();
        adoc.setLevel(level);
        
        val stack = new BlockHelper(adoc);
        
        NodeTraversor.traverse(new NodeVisitor() {
            
            @Override
            public void head(Node node, int depth) {
                
                if(node instanceof TextNode) {
                    val text = ((TextNode)node).text().trim();
                    if(!text.isBlank()) {
                        stack.blockAppend(text);
                    }
                    return;
                } 
                
                switch(node.nodeName()) {
                case "ul":
                    stack.nextList();
                    return;
                case "p":
                    stack.pop();
                    stack.nextBlock();
                    return;
                case "li":
                    stack.nextListItem();
                    return;
                case "b":
                case "em":
                    stack.blockAppend(" *");
                    return;
                case "tt":
                    stack.blockAppend(" `");
                    return;
                case "i":
                    stack.blockAppend(" _");
                    return;
                }
            }
            
            @Override
            public void tail(Node node, int depth) {
                
                switch(node.nodeName()) {
                case "ul":
                    stack.popList();
                    return;
                case "p":
                    return;
                case "li":
                    stack.pop();
                    return;
                case "b":
                case "em":
                    stack.blockAppend("* ");
                    return;
                case "tt":
                    stack.blockAppend("` ");
                    return;
                case "i":
                    stack.blockAppend("_ ");
                    return;
                }
                
            }
            
            
        }, body);

        return adoc;
    }
    
    // -- HELPER
    
    private final static class BlockHelper {
        private final Stack<StructuralNode> stack = new Stack<StructuralNode>();
        private final Stack<org.asciidoctor.ast.List> listStack = new Stack<org.asciidoctor.ast.List>();

        BlockHelper(Document adoc){
            stack.push(adoc);
        }
        
        void pop() {
            stack.pop();
        }
        
        void popList() {
            stack.pop();
            listStack.pop();
        }
        
        Block nextBlock() {
            val block = AsciiDocFactory.block(stack.peek());
            stack.push(block);
            return block;
        }
        
        Block getBlock() {
            return (stack.peek() instanceof Block)
                    ? (Block) stack.peek()
                    : nextBlock();
        }
        
        void blockAppend(String source) {
            val block = getBlock();
            block.setSource(block.getSource()+source);
        }
        
        org.asciidoctor.ast.List nextList() {
            val nextList = AsciiDocFactory.list(stack.peek());
            stack.push(nextList);
            listStack.push(nextList);
            return nextList;
        }

        ListItem nextListItem() {
            val list = listStack.isEmpty()
                    ? nextList()
                    : listStack.peek();
            
            // pop until stack top points to list
            while(!list.equals(stack.peek())) {
                stack.pop();
            }
            val listItem = AsciiDocFactory.listItem(list);
            stack.push(listItem);
            return listItem;
        }
        
    }

}
