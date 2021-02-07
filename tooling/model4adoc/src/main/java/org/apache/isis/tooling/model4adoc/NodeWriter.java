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

import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Refs;
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

    // -- DOCUMENT

    private static final List<String> knownDocAttributes = _Lists.of(
            "Notice");

    @Override
    public boolean documentHead(Document doc, int depth) {

        _Strings.nonEmpty(doc.getTitle())
        .ifPresent(title->printChapterTitle(title, depth+1));

        val attr = doc.getAttributes();
        if(!attr.isEmpty()) {
            for(val knownAttrKey : knownDocAttributes) {
                Optional.ofNullable(attr.get(knownAttrKey.toLowerCase()))
                .ifPresent(attrValue->printfln(":%s: %s", knownAttrKey, attrValue));
            }
        }

        return true; // continue visit
    }

//    @Override
//    public void documentTail(Document doc, int depth) {
//    }

    // -- BLOCK

    @RequiredArgsConstructor
    private static enum Style {
        OPEN_BLOCK("open"::equals),
        LISTING_BLOCK("listing"::equals),
        FOOTNOTE_LIST("arabic"::equals),
        ADMONITION_NOTE("NOTE"::equals),
        ADMONITION_TIP("TIP"::equals),
        ADMONITION_IMPORTANT("IMPORTANT"::equals),
        ADMONITION_CAUTION("CAUTION"::equals),
        ADMONITION_WARNING("WARNING"::equals),
        UNKNOWN(x->false)
        ;
        private final Predicate<String> matcher;
        public static Style parse(StructuralNode node) {
            val styleAttribute = node.getStyle();
            return Stream.of(Style.values())
                    .filter(style->style.matcher.test(styleAttribute))
                    .findFirst()
                    .orElse(UNKNOWN);
        }
        public boolean isOpenBlock() {
            return this==Style.OPEN_BLOCK;
        }
        public boolean isListingBlock() {
            return this==Style.LISTING_BLOCK;
        }
        public boolean isFootnoteList() {
            return this==Style.FOOTNOTE_LIST;
        }
        public boolean isAdmonition() {
            return name().startsWith("ADMONITION_");
        }
    }

    @Override
    public boolean blockHead(Block block, int depth) {

        val style = Style.parse(block);

        if(style.isOpenBlock()) {
            pushNewWriter(); // write the open block to a StringWriter, such that can handle empty blocks
            println("+");
            println("--");
            isContinuation = true; // set continuation flag, so other blocks don't add newlines
            bulletCountStack.push(bulletCount);
            bulletCount = 0;
        } else if(!isContinuation) {
            if(newLineCount<=1) {
                printNewLine();
            }
        }

        if(style.isAdmonition()) {
            if(block.getBlocks().size()>0) {
                printfln("[%s]", block.getStyle());
                println("====");
                isContinuation = true; // set continuation flag, so other blocks don't add newlines
            } else {
                printf("%s: ", block.getStyle());
            }
        } else if(style.isListingBlock()) {
            println("----");
        }

        for(val line : block.getLines()) {
            println(line);
        }

        return true; // continue visit
    }

    @Override
    public void blockTail(Block block, int depth) {

        val style = Style.parse(block);

        if(style.isOpenBlock()){
            println("--");
            popWriter();
            bulletCount = bulletCountStack.pop();
        } else if(style.isAdmonition()){
            if(block.getBlocks().size()>0) {
                println("====");
            }
        } else if(style.isListingBlock()) {
            println("----");
        }
    }

    // -_ LIST

    @Override
    public boolean listHead(org.asciidoctor.ast.List list, int depth) {
        if(bulletCount==0) {
            if(newLineCount<=1) {
                printNewLine();
            }
        }
        bulletCount++;

        _Strings.nonEmpty(list.getTitle())
        .ifPresent(this::printBlockTitle);

        return true; // continue visit
    }

    @Override
    public void listTail(org.asciidoctor.ast.List list, int depth) {
        bulletCount--;
    }

    @Override
    public boolean listItemHead(ListItem listItem, int depth) {

        val isFootnoteStyle = Style.parse((org.asciidoctor.ast.List)(listItem.getParent()))
                .isFootnoteList();

        val bullets = isFootnoteStyle
                ? "<.>"
                : _Strings.padEnd("", bulletCount, '*');

        val listItemSource = _Strings.nullToEmpty(listItem.getSource()).trim();
        if(!listItemSource.isEmpty()) {
            printfln("%s %s", bullets, listItemSource);
            return true; // continue visit
        }

        if(_NullSafe.isEmpty(listItem.getBlocks())) {
            printfln("%s _missing listitem text_", bullets);
            return true; // continue visit
        }

        //there is a special case, if source is blank
        //the first block replaces the source

        //find the first block that has a source, use it and blank it out, so is not written twice

        val isFixed = _Refs.booleanRef(false);

        StructuralNodeTraversor.depthFirst(new BlockVisitor(block->{
            val blockSource = _Strings.nullToEmpty(block.getSource()).trim();
            if(!blockSource.isEmpty()) {
                block.setSource(null);
                printfln("%s %s", bullets, blockSource);
                isFixed.setValue(true);
                return false; // terminate the visit
            }
            return true; // continue the visit
        }), listItem);

        if(isFixed.isFalse()) {
            printfln("%s _missing listitem text_", bullets);
        }

        return true; // continue visit
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
    public boolean tableHead(Table table, int depth) {

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

        return true; // continue visit
    }

//    @Override
//    public void tableTail(Table table, int depth) {
//
//    }

    // -- HELPER

    private void printChapterTitle(final String title, final int symbolCount) {
        print(_Strings.of(symbolCount, '='));
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
    private final Stack<Integer> bulletCountStack = new Stack<>();


    // -- EMPTY CONTINUATION BLOCK HANDLING

    private final static int EMPTY_CONTINUATION_BLOCK_SIZE = 8;

    private final Stack<StringWriter> stringWriterStack = new Stack<>();

    private Writer currentWriter;
    private Writer currentWriter() {
        if(currentWriter == null) {
            this.currentWriter = writer;
        }
        return currentWriter;
    }
    private void pushNewWriter() {
        val sw = new StringWriter();
        currentWriter = sw;
        stringWriterStack.push(sw);
    }
    @SneakyThrows
    private void popWriter() {
        val sw = stringWriterStack.pop();
        currentWriter = stringWriterStack.isEmpty()
                ? writer
                : stringWriterStack.peek();
        val continuationBlockAsString = sw.toString();
        if(continuationBlockAsString.length()>EMPTY_CONTINUATION_BLOCK_SIZE) {
            writer.append(continuationBlockAsString); // write directly to the current writer, no side-effects wanted
        }
    }

    // -- PRINTING

    @SneakyThrows
    private void printNewLine() {
        if(!hasWrittenAnythingYet) {
            return;
        }
        currentWriter().append("\n");
        newLineCount++;
    }

    @SneakyThrows
    private void print(final @NonNull String line) {

        if(line.contains("\n")) {
            val lineIter = _Text.normalize(_Text.getLines(line)).iterator();
            while(lineIter.hasNext()) {
                val nextLine = lineIter.next();
                currentWriter().append(nextLine);
                if(!nextLine.isEmpty()) {
                    hasWrittenAnythingYet = true;
                    isContinuation = false; // clear continuation flag
                    newLineCount = 0;
                }
                if(lineIter.hasNext()) {
                    currentWriter().append("\n");
                    newLineCount++;
                }
            }
            return;
        }
        if(!line.isEmpty()) {
            currentWriter().append(line);
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
