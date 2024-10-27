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
package org.apache.causeway.valuetypes.asciidoc.builder;

import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;

import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Refs;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.base._Text;
import org.apache.causeway.commons.internal.collections._Arrays;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.commons.io.TextUtils;
import org.apache.causeway.valuetypes.asciidoc.builder.ast.SimpleTable;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
final class NodeWriter implements StructuralNodeVisitor {

    private final Writer writer;

    // -- DOCUMENT

    private static final List<String> knownDocAttributes = List.of(
            "Notice");

    @Override
    public boolean documentHead(final Document doc, final int depth) {

        _Strings.nonEmpty(doc.getTitle())
        .ifPresent(title->printChapterTitle(title, depth+1));

        var attr = doc.getAttributes();
        if(!attr.isEmpty()) {
            for(var knownAttrKey : knownDocAttributes) {
                Optional.ofNullable(attr.get(knownAttrKey.toLowerCase()))
                .ifPresent(attrValue->printfln(":%s: %s", knownAttrKey, attrValue));
            }
        }

        return true; // continue visit
    }

//    @Override
//    public void documentTail(Document doc, int depth) {
//    }

    // -- KNOWN DIAGRAM TYPES

    private static Set<String> KNOWN_DIAGRAM_TYPES = _Sets.of(
        "a2s",
        "actdiag",
        "blockdiag",
        "bpmn",
        "bytefield",
        "ditaa",
        "dpic",
        "erd",
        "gnuplot",
        "graphviz",
        "meme",
        "mermaid",
        "msc",
        "nomnoml",
        "nwdiag",
        "packetdiag",
        "pikchr",
        "plantuml",
        "rackdiag",
        "seqdiag",
        "shaape",
        "smcat",
        "svgbob",
        "symbolator",
        "syntrax",
        "umlet",
        "vega",
        "vegalite",
        "wavedrom");

    // -- BLOCK

    @RequiredArgsConstructor
    private static enum Style {
        OPEN_BLOCK("open"::equals),
        LISTING_BLOCK("listing"::equals),
        CALLOUT_LIST("arabic"::equals),
        COLLAPSIBLE_BLOCK("example"::equals),
        SOURCE_BLOCK("source"::equals),
        PASSTHROUG_BLOCK("passthrough"::equals),
        DIAGRAM_BLOCK(KNOWN_DIAGRAM_TYPES::contains),
        ADMONITION_NOTE("NOTE"::equals),
        ADMONITION_TIP("TIP"::equals),
        ADMONITION_IMPORTANT("IMPORTANT"::equals),
        ADMONITION_CAUTION("CAUTION"::equals),
        ADMONITION_WARNING("WARNING"::equals),
        UNKNOWN(x->false)
        ;
        private final Predicate<String> matcher;
        public static Style parse(final StructuralNode node) {
            var styleAttribute = node.getStyle();
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
        public boolean isCalloutList() {
            return this==Style.CALLOUT_LIST;
        }
        public boolean isCollapsibleBlock() {
            return this==Style.COLLAPSIBLE_BLOCK;
        }
        public boolean isSourceBlock() {
            return this==Style.SOURCE_BLOCK;
        }
        public boolean isPassthroughBlock() {
            return this==Style.PASSTHROUG_BLOCK;
        }
        public boolean isDiagramBlock() {
            return this==Style.DIAGRAM_BLOCK;
        }
        public boolean isAdmonition() {
            return name().startsWith("ADMONITION_");
        }
    }

    @Override
    public boolean sectionHead(final Section section, final int depth) {
        println("");
        printChapterTitle(section.getTitle(), depth+1);
        return true; // continue visit
    }

    @Override
    public boolean blockHead(final Block block, final int depth) {

        var style = Style.parse(block);

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
        } else if(style.isCollapsibleBlock()) {
            println("[%collapsible]");
            _Strings.nonEmpty(block.getTitle())
                .ifPresent(this::printBlockTitle);
            println("====");
        } else if(style.isSourceBlock()) {
            var language = (String)block.getAttribute("language");
            if(_Strings.isNotEmpty(language)) {
                printfln("[source,%s]", language);
            } else {
                printfln("[source]");
            }
            _Strings.nonEmpty(block.getTitle())
                .ifPresent(this::printBlockTitle);
            println("----");
        } else if(style.isPassthroughBlock()) {
            println("++++");
        } else if(style.isDiagramBlock()) {

            var diagramTypeAndOptions = IntStream
                    .iterate(1, index->block.getAttribute(""+index)!=null, index->index+1)
                    .mapToObj(index->(String)block.getAttribute(""+index))
                    .collect(Collectors.joining(","));

            printfln("[%s]", diagramTypeAndOptions);

            _Strings.nonEmpty(block.getTitle())
                .ifPresent(this::printBlockTitle);
            println("----");
        }

        for(var line : block.getLines()) {
            println(line);
        }

        return true; // continue visit
    }

    @Override
    public void blockTail(final Block block, final int depth) {

        var style = Style.parse(block);

        if(style.isOpenBlock()){
            println("--");
            popWriter();
            bulletCount = bulletCountStack.pop();
        } else if(style.isAdmonition()){
            if(block.getBlocks().size()>0) {
                println("====");
            }
        } else if(style.isListingBlock()
                || style.isSourceBlock()
                || style.isDiagramBlock()) {
            println("----");
        } else if(style.isPassthroughBlock()) {
            println("++++");
        } else if(style.isCollapsibleBlock()) {
            println("====");
        }
    }

    // -_ LIST

    @Override
    public boolean listHead(final org.asciidoctor.ast.List list, final int depth) {
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
    public void listTail(final org.asciidoctor.ast.List list, final int depth) {
        bulletCount--;
    }

    @Override
    public boolean listItemHead(final ListItem listItem, final int depth) {

        var isCalloutStyle = Style.parse((org.asciidoctor.ast.List)(listItem.getParent()))
                .isCalloutList();

        var bullets = isCalloutStyle
                ? "<.>"
                : _Strings.padEnd("", bulletCount, '*');

        var listItemSource = _Strings.nullToEmpty(listItem.getSource()).trim();
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

        var isFixed = _Refs.booleanRef(false);

        StructuralNodeTraversor.depthFirst(new BlockVisitor(block->{
            var blockSource = _Strings.nullToEmpty(block.getSource()).trim();
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
    public void listItemTail(final ListItem listItem, final int depth) {
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
    public boolean tableHead(final Table table, final int depth) {

        printNewLine(); // empty line before table block starts

        _Strings.nonEmpty(table.getTitle())
        .ifPresent(this::printBlockTitle);

        printSingleLineMap("[%s]", formatedAttrMap(table), "%s=\"%s\"");

        println("|===");

        // table.getColumns() ... styles

        for(var headRow : table.getHeader()) {
            for(var cell : headRow.getCells()) {
                printf("|%s ", cell.getSource());
            }
            printNewLine();
        }

        for(var row : table.getBody()) {
            printNewLine(); // empty line before each row
            for(var cell : row.getCells()) {
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

    private void printSingleLineMap(final String mapFormat, final Map<String, String> map, final String entryFormat) {
        if(map.isEmpty()) {
            return;
        }
        var elements = map.entrySet()
                .stream()
                .map(entry->String.format(entryFormat, entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(", "));
        printfln(mapFormat, elements);
    }

    // -- HELPER - LOW LEVEL

    private static Map<String, String> formatedAttrMap(final Table table) {
        final Map<String, String> formatedAttrMap = new LinkedHashMap<>();

        if(table.hasAttribute(SimpleTable.COLS_ATTR)) {
            formatedAttrMap.put(SimpleTable.COLS_ATTR, (String)table.getAttribute(SimpleTable.COLS_ATTR));
        } else {
            // emit the [cols="1,1,.."] line unconditionally
            formatedAttrMap.put(SimpleTable.COLS_ATTR, IntStream.range(0, table.getColumns().size())
                    .mapToObj(i->"1")
                    .collect(Collectors.joining(",")));
        }
        if(table.hasAttribute(SimpleTable.FRAME_ATTR)) {
            formatedAttrMap.put(SimpleTable.FRAME_ATTR, (String)table.getAttribute(SimpleTable.FRAME_ATTR));
        }
        if(table.hasAttribute(SimpleTable.GRID_ATTR)) {
            formatedAttrMap.put(SimpleTable.GRID_ATTR, (String)table.getAttribute(SimpleTable.GRID_ATTR));
        }

        var options = table.getAttributes().entrySet()
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
        var sw = new StringWriter();
        currentWriter = sw;
        stringWriterStack.push(sw);
    }
    @SneakyThrows
    private void popWriter() {
        var sw = stringWriterStack.pop();
        currentWriter = stringWriterStack.isEmpty()
                ? writer
                : stringWriterStack.peek();
        var continuationBlockAsString = sw.toString();
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
            var lineIter = _Text.normalize(TextUtils.readLines(line)).iterator();
            while(lineIter.hasNext()) {
                var nextLine = lineIter.next();
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
        var formattedString = String.format(format, _Arrays.map(strings, this::nullToEmpty));
        print(formattedString);
    }

    private void printfln(final @NonNull String format, final Object... strings) {
        var formattedString = String.format(format, _Arrays.map(strings, this::nullToEmpty));
        print(formattedString);
        printNewLine();
    }

    private Object nullToEmpty(final Object x) {
        if(x==null) {
            return "";
        }
        return x;
    }

}
