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

import java.util.function.Predicate;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.List;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.Table;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class BlockVisitor 
implements StructuralNodeVisitor {

    private final Predicate<Block> blockConsumer;
    private boolean continueVisit = true;
    
    @Override
    public void documentHead(Document doc, int depth) {
    }

    @Override
    public void blockHead(Block block, int depth) {
        if(!continueVisit) {
            return;
        }
        continueVisit = blockConsumer.test(block);
    }

    @Override
    public void listHead(List list, int depth) {
    }

    @Override
    public void listItemHead(ListItem listItem, int depth) {
    }

    @Override
    public void tableHead(Table table, int depth) {
    }

    @Override
    public void documentTail(Document doc, int depth) {
    }

    @Override
    public void blockTail(Block block, int depth) {
    }

    @Override
    public void listTail(List list, int depth) {
    }

    @Override
    public void listItemTail(ListItem listItem, int depth) {
    }

    @Override
    public void tableTail(Table table, int depth) {
    }

}
