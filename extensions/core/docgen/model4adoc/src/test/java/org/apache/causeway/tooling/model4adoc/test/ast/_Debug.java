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
package org.apache.causeway.tooling.model4adoc.test.ast;

import java.util.Optional;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.val;

final class _Debug {
    
    static void debug(Document node) {
        debug(node, 0);
    }
    
    static void debug(StructuralNode node, int level) {
        
        val simpleName = node.getClass().getSimpleName();
        
        print(level, "node type: %s", node.getClass());
        print(level, "%s title: %s", simpleName, node.getTitle());
        print(level, "%s style: %s", simpleName, node.getStyle());
        sourceFor(node).ifPresent(x->print(level, "%s source: %s", simpleName, x));
        print(level, "%s attributes: %d", simpleName, node.getAttributes().size());
        node.getAttributes()
        .forEach((k, v)->{
            print(level+1, " - %s->%s", k, v);
        });
        printAdditionalFor(node, level);
        
        if(node.getBlocks().size()>0) {
            print(level, "%s child blocks: %d ...", simpleName, node.getBlocks().size());
        }
        
        for(val subNode : node.getBlocks()) {
            debug(subNode, level+1);
        }
    }

    private static Optional<String> sourceFor(StructuralNode node) {
        if(node instanceof Document) {
            //((Document)node);
            return Optional.empty();
        }
        if(node instanceof Table) {
            //((Table)node);
            return Optional.empty();
        }
        if(node instanceof org.asciidoctor.ast.List) {
            //((org.asciidoctor.ast.List)node);
            return Optional.empty();
        }
        if(node instanceof ListItem) {
            return Optional.ofNullable(((ListItem)node).getSource());
        }
        if(node instanceof Block) {
            return Optional.ofNullable(((Block)node).getSource());
        }    
        throw _Exceptions.unsupportedOperation("node type not supported %s", node.getClass());
    }

    private static void printAdditionalFor(StructuralNode node, int level) {
        if(node instanceof Document) {
            //((Document)node);
            return;
        }
        if(node instanceof Table) {
            debug((Table)node, level);
            return;
        }
        if(node instanceof org.asciidoctor.ast.List) {
            debug((org.asciidoctor.ast.List)node, level);
            return;
        }
        if(node instanceof ListItem) {
            //Optional.ofNullable(((ListItem)node).getSource());
            return;
        }
        if(node instanceof Block) {
            return;
        }    
        throw _Exceptions.unsupportedOperation("node type not supported %s", node.getClass());
    }
    
    private static void debug(Table table, int level) {
        val refCol = table.getColumns().get(0);
        val refRow = table.getBody().get(0);
        val refCell = refRow.getCells().get(0);
        
        val refHead = table.getHeader().get(0);
        
        print(level, "tab caption: " + table.getCaption());
        
        print(level, "col attrib: " + refCol.getAttributes());
        print(level, "col context: " + refCol.getContext());
        print(level, "col id: " + refCol.getId());
        print(level, "col reftex: " + refCol.getReftext());
        print(level, "col nodeName: " + refCol.getNodeName());
        print(level, "col role: " + refCol.getRole());
       
        print(level, "cell source: " + refCell.getSource());
        
        print(level, "head source: " + refHead.getCells().get(0).getSource());
    }
    
    private static void debug(org.asciidoctor.ast.List list, int level) {
        print(level, "%d list blocks: ", list.getBlocks().size());
        print(level, "%d list items: ", list.getItems().size());
        print(level, "%d list level: ", list.getLevel());
    }
    
    private static void print(int level, String format, Object... args) {
        val indent = _Strings.of(level*2, ' ');
        System.out.println(String.format("%s%s", indent, String.format(format, args)));
    }
    
}
