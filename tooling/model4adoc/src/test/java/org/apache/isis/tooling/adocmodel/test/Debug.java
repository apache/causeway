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
package org.apache.isis.tooling.adocmodel.test;

import java.util.Optional;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.val;

final class Debug {
    
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

    private static void print(int level, String format, Object... args) {
        val indent = _Strings.padEnd("", level*2, ' ');
        System.out.println(String.format("%s%s", indent, String.format(format, args)));
    }
    
}
