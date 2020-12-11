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

import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.base._Text;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.commons.internal.collections._Lists;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@RequiredArgsConstructor
final class NodeWriter implements StructuralNodeVisitor {

    private final Writer writer;

    @Override
    public void head(StructuralNode node, int depth) {
        StructuralNodeVisitor.super.head(node, depth);
    }
    
    @Override
    public void tail(StructuralNode node, int depth) {
        StructuralNodeVisitor.super.tail(node, depth);
        
    }
    
    // -- DOCUMENT

    private static final List<String> knownDocAttributes = _Lists.of(
            "Notice");
    
    @Override
    public void documentHead(Document doc, int depth) {

        _Strings.nonEmpty(doc.getTitle())
        .ifPresent(title->printChapterTitle(title, depth+1));
        
        val attr = doc.getAttributes();
        if(!attr.isEmpty()) {
            for(val knownAttrKey : knownDocAttributes) {
                Optional.ofNullable(attr.get(knownAttrKey))
                .ifPresent(attrValue->printfln(":%s: %s", knownAttrKey, attrValue));
            }
        }
    }

    @Override
    public void documentTail(Document doc, int depth) {
    }
    
    // -- BLOCK
    
    @Override
    public void blockHead(Block block, int depth) {
        
        val isOpenBlockStyle = "open".equals(block.getAttribute("style"));
        
        if(isOpenBlockStyle){
            println("+");
            println("--");
            isContinuation = true; // set continuation flag, so other blocks don't add newlines
        } else if(!isContinuation) {
            if(newLineCount<=1) {
                printNewLine();
            }    
        }
        
        for(val line : block.getLines()) {
            println(line);
        }
        
    }
    
    @Override
    public void blockTail(Block block, int depth) {
        val isOpenBlockStyle = "open".equals(block.getAttribute("style"));
        if(isOpenBlockStyle){
            println("--");
        }
    }
    
    // -_ LIST 

    @Override
    public void listHead(org.asciidoctor.ast.List list, int depth) {
        if(bulletCount==0) {
            if(newLineCount<=1) {
                printNewLine();
            }
        }
        bulletCount++;
        
        _Strings.nonEmpty(list.getTitle())
        .ifPresent(this::printBlockTitle);
    }
    
    @Override
    public void listTail(org.asciidoctor.ast.List list, int depth) {
        bulletCount--;
    }
    
    @Override
    public void listItemHead(ListItem listItem, int depth) {
        
        val isFootNoteRole = bulletCount==1 
                && listItem.getRoles().contains("footnote"); // non standard conform jack yet
        
        val bullets = isFootNoteRole
                ? "<.> "
                : _Strings.padEnd("", bulletCount, '*');
      
      _Strings.nonEmpty(listItem.getSource())
      .ifPresent(listItemSource->printfln("%s %s", bullets, listItemSource));

    }

    @Override
    public void listItemTail(ListItem listItem, int depth) {
    }

    // -- TABLE
    
    //  [cols="3m,2a", options="header"]
    //  |===
    //  |Name of Column 1 |Name of Column 2 |Name of Column 3 
    //
    //  |Cell in column 1, row 1
    //  |Cell in column 2, row 1
    //  |Cell in column 3, row 1
    //
    //  |Cell in column 1, row 2
    //  |Cell in column 2, row 2
    //  |Cell in column 3, row 2
    //  |===
    @Override
    public void tableHead(Table table, int depth) {
        
        _Strings.nonEmpty(table.getTitle())
        .ifPresent(this::printBlockTitle);
        
        printSingleLineMap("[%s]", formatedAttrMap(table), "%s=\"%s\"");
        
        println("|===");
        
        // table.getColumns() ... styles
        
        for(val headRow : table.getHeader()) {
            for(val cell : headRow.getCells()) {
                printf("|%s ", cell.getSource());    
            }
            printNewLine();
        }
        
        for(val row : table.getBody()) {
            printNewLine(); // empty line before each row
            for(val cell : row.getCells()) {
                //bypass newline tracking
                printfln("|%s", cell.getSource());    
            }
        }
        
        println("|===");
    }

    @Override
    public void tableTail(Table table, int depth) {
        
    }

    // -- HELPER
    
    private void printChapterTitle(final String title, final int symbolCount) {
        print(_Strings.padStart("", symbolCount, '='));
        printfln(" %s", title);
    }
    
    private void printBlockTitle(final String title) {
        printfln(".%s", title);
    }
    
    private void printSingleLineMap(String mapFormat, Map<String, String> map, String entryFormat) {
        if(map.isEmpty()) {
            return;
        }
        val elements = map.entrySet()
        .stream()
        .map(entry->String.format(entryFormat, entry.getKey(), entry.getValue()))
        .collect(Collectors.joining(", "));
        printfln(mapFormat, elements);
    }
    
    // -- HELPER - LOW LEVEL
    
    private static Map<String, String> formatedAttrMap(Table table) {
        final Map<String, String> formatedAttrMap = new LinkedHashMap<>();
        
        if(table.hasAttribute("cols")) {
            formatedAttrMap.put("cols", (String)table.getAttribute("cols"));
        }
        
        val options = table.getAttributes().entrySet()
                .stream()
                .filter(entry->entry.getKey().endsWith("-option"))
                .map(entry->entry.getKey().substring(0, entry.getKey().length()-7))
                .collect(Collectors.joining(","));
        
        if(!options.isEmpty()) {
            formatedAttrMap.put("options", options);
        }
        
        return formatedAttrMap;
    }
    
    private int bulletCount = 0;
    private int newLineCount = 0;

    private boolean hasWrittenAnythingYet = false;
    private boolean isContinuation = false;
    
    // -- PRINTING
    
    @SneakyThrows
    private void printNewLine() {
        if(!hasWrittenAnythingYet) {
            return;
        }
        writer.append("\n");
        newLineCount++;
    }
    
    @SneakyThrows
    private void print(final @NonNull String line) {
        
        if(line.contains("\n")) {
            val lineIter = _Text.normalize(_Text.getLines(line)).iterator();
            while(lineIter.hasNext()) {
                val nextLine = lineIter.next(); 
                writer.append(nextLine);
                if(!nextLine.isEmpty()) {
                    hasWrittenAnythingYet = true;
                    isContinuation = false; // clear continuation flag
                    newLineCount = 0;
                }
                if(lineIter.hasNext()) {
                    writer.append("\n");
                    newLineCount++;
                }
            }
            return;
        }
        if(!line.isEmpty()) {
            writer.append(line);
            hasWrittenAnythingYet = true;
            isContinuation = false; // clear continuation flag
            newLineCount = 0;
        }
    }
    
    private void println(final @NonNull String line) {
        print(line);
        printNewLine();
    }
    
    private void printf(final @NonNull String format, final Object... strings) {
        val formattedString = String.format(format, _Arrays.map(strings, this::nullToEmpty));
        print(formattedString);
    }
    
    private void printfln(final @NonNull String format, final Object... strings) {
        val formattedString = String.format(format, _Arrays.map(strings, this::nullToEmpty));
        print(formattedString);
        printNewLine();
    }
    
    private Object nullToEmpty(Object x) {
        if(x==null) {
            return "";
        }
        return x;
    }
    

}
