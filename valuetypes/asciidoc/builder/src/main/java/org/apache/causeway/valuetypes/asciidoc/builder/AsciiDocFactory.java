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

import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.Column;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.Row;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Refs;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.base._Text;
import org.apache.causeway.commons.io.TextUtils;
import org.apache.causeway.valuetypes.asciidoc.builder.ast.SimpleBlock;
import org.apache.causeway.valuetypes.asciidoc.builder.ast.SimpleCell;
import org.apache.causeway.valuetypes.asciidoc.builder.ast.SimpleColumn;
import org.apache.causeway.valuetypes.asciidoc.builder.ast.SimpleDocument;
import org.apache.causeway.valuetypes.asciidoc.builder.ast.SimpleList;
import org.apache.causeway.valuetypes.asciidoc.builder.ast.SimpleListItem;
import org.apache.causeway.valuetypes.asciidoc.builder.ast.SimpleRow;
import org.apache.causeway.valuetypes.asciidoc.builder.ast.SimpleSection;
import org.apache.causeway.valuetypes.asciidoc.builder.ast.SimpleTable;

import org.jspecify.annotations.NonNull;

/**
 * Provides convenient factory methods to build a (AsciiDoc) Document Model.
 * @since Sep 10, 2020
 * @apiNote The <i>AsciiDoc<i> name is trademarked by the <i>Eclipse Foundation</i>.
 * <p>
    This project is <b>not</b> part of the specification effort for <i>AsciiDoc<i> under the
    <i>AsciiDoc Working Group</i>. See https://projects.eclipse.org/proposals/asciidoc-language
    and https://accounts.eclipse.org/mailing-list/asciidoc-wg. However, we are happy to
    help with transfer of source code, if any project (under the umbrella of the
    <i>AsciiDoc Working Group</i>) is willing to take over.
    </p>
 */
public class AsciiDocFactory {

    /**
     * @return a blank/empty document object
     */
    public static Document doc() {
        return new SimpleDocument();
    }

    /**
     * syntactic sugar
     */
    public static Document doc(final Consumer<Document> documentBuilder) {
        var doc = doc();
        documentBuilder.accept(doc);
        return doc;
    }

    /**
     * syntactic sugar
     */
    public static String toString(final Consumer<Document> documentBuilder) {
        var doc = doc();
        documentBuilder.accept(doc);
        return AsciiDocWriter.toString(doc);
    }

    // -- ATTRIBUTES

    public static void attrNotice(final Document node, final String value) {
        node.setAttribute("notice", value, true);
    }

    // -- ADMONITIONS

    //NOTE
    //TIP
    //IMPORTANT
    //CAUTION
    //WARNING

    public static Block note(final StructuralNode parent) {
        return admonition("Note", parent, null);
    }

    public static Block note(final StructuralNode parent, final String source) {
        return admonition("Note", parent, source);
    }

    public static Block tip(final StructuralNode parent) {
        return admonition("Tip", parent, null);
    }

    public static Block tip(final StructuralNode parent, final String source) {
        return admonition("Tip", parent, source);
    }

    public static Block important(final StructuralNode parent) {
        return admonition("Important", parent, null);
    }

    public static Block important(final StructuralNode parent, final String source) {
        return admonition("Important", parent, source);
    }

    public static Block caution(final StructuralNode parent) {
        return admonition("Caution", parent, null);
    }

    public static Block caution(final StructuralNode parent, final String source) {
        return admonition("Caution", parent, source);
    }

    public static Block warning(final StructuralNode parent) {
        return admonition("Warning", parent, null);
    }

    public static Block warning(final StructuralNode parent, final String source) {
        return admonition("Warning", parent, source);
    }

    public static Section section(final StructuralNode parent, final String title) {
        var section = new SimpleSection();
        section.setTitle(title);
        section.setLevel(parent.getLevel() + 1);
        parent.getBlocks().add(section);
        section.setParent(parent);
        return section;
    }

    // -- BLOCK

    public static Block block(final StructuralNode parent) {
        return block(parent, null);
    }

    public static Block block(final StructuralNode parent, final String source) {
        var block = new SimpleBlock();
        block.setSource(source);
        block.setLevel(parent.getLevel());
        parent.getBlocks().add(block);
        block.setParent(parent);
        return block;
    }

    public static Block openBlock(final ListItem listItem) {
        var openBlock = block(listItem);
        openBlock.setStyle("open");
        return openBlock;
    }

    public static Block listingBlock(final StructuralNode parent, final @NonNull String source) {
        var listingBlock = block(parent, source);
        listingBlock.setStyle("listing");
        return listingBlock;
    }

    public static Block sourceBlock(final StructuralNode parent, final @Nullable String language, final @NonNull String source) {
        var sourceBlock = block(parent, source);
        sourceBlock.setStyle("source");
        if(_Strings.isNotEmpty(language)) {
            sourceBlock.setAttribute("language", language, true);
        }
        return sourceBlock;
    }

    public static Block htmlPassthroughBlock(final StructuralNode parent, final @NonNull String html) {
        var block = block(parent, html);
        block.setStyle("passthrough");
        return block;
    }

    public static Block diagramBlock(
            final StructuralNode parent,
            final @NonNull String diagramType,
            final @NonNull Can<String> diagramOptions,
            final @NonNull String source) {

        var diagramBlock = block(parent, source);

        _Strings.nonEmpty(diagramType)
            .ifPresent(diagramBlock::setStyle);

        var attributes = diagramOptions.add(0, diagramType);
        var attributeIndex = _Refs.intRef(1);

        // add options
        attributes.forEach(opt->{
            diagramBlock.setAttribute(""+attributeIndex.getValue(), opt, true);
            attributeIndex.incAndGet();
        });

        return diagramBlock;
    }

    // -- CALLOUTS

    public static org.asciidoctor.ast.List callouts(final StructuralNode parent) {
        var calloutList = list(parent);
        calloutList.setStyle("arabic");
        return calloutList;
    }

    public static ListItem callout(final org.asciidoctor.ast.List parent, final @NonNull String source) {
        return listItem(parent, source);
    }

    // -- COLLAPSIBLE

    public static Block collapsibleBlock(final StructuralNode parent, final @NonNull String source) {
        var collapsibleBlock = block(parent, source);
        collapsibleBlock.setStyle("example");
        collapsibleBlock.setAttribute("collapsible-option", "1", true);
        return collapsibleBlock;
    }

    // -- TABLE

    public static Table table(final StructuralNode parent) {
        var table = new SimpleTable();
        parent.getBlocks().add(table);
        table.setParent(parent);
        return table;
    }

    public static Column col(final Table table) {
        var column = new SimpleColumn();
        table.getColumns().add(column);
        column.setParent(table);
        return column;
    }

    public static Row row(final Table table) {
        var row = new SimpleRow();
        table.getBody().add(row);
        return row;
    }

    public static Row headRow(final Table table) {
        var row = new SimpleRow();
        table.getHeader().add(row);
        return row;
    }

    public static Row footRow(final Table table) {
        var row = new SimpleRow();
        table.getFooter().add(row);
        return row;
    }

    public static Cell cell(final Row row, final Column column, final String source) {
        var cell = new SimpleCell();
        row.getCells().add(cell);
        cell.setParent(column);
        cell.setSource(source);
        return cell;
    }

    public static Cell cell(final Table table, final Row row, final String source) {
        var colIndex = row.getCells().size();
        var column = getOrCreateColumn(table, colIndex);
        return cell(row, column, source);
    }

    public static Cell cell(final Table table, final int rowIndex, final int colIndex, final String source) {
        var row = getOrCreateRow(table, rowIndex);
        var col = getOrCreateColumn(table, colIndex);
        return cell(row, col, source);
    }

    public static Cell headCell(final Table table, final int rowIndex, final int colIndex, final String source) {
        var row = getOrCreateHeadRow(table, rowIndex);
        var col = getOrCreateColumn(table, colIndex);
        return cell(row, col, source);
    }

    public static Cell footCell(final Table table, final int rowIndex, final int colIndex, final String source) {
        var row = getOrCreateFootRow(table, rowIndex);
        var col = getOrCreateColumn(table, colIndex);
        return cell(row, col, source);
    }

    public static org.asciidoctor.ast.List list(final StructuralNode parent) {
        var list = new SimpleList();
        list.setLevel(parent.getLevel()+1);
        parent.getBlocks().add(list);
        list.setParent(parent);
        return list;
    }

    public static ListItem listItem(final org.asciidoctor.ast.List parent) {
        return listItem(parent, null);
    }

    public static ListItem listItem(final org.asciidoctor.ast.List parent, final String source) {
        var listItem = new SimpleListItem();
        listItem.setLevel(parent.getLevel());
        parent.getItems().add(listItem);
        listItem.setParent(parent);
        listItem.setSource(source);
        return listItem;
    }

    public static class SourceFactory {

        public static Block sourceBlock(
                final @NonNull Document doc,
                final @NonNull String languageAndOptions,
                final @NonNull String source,
                final @Nullable String title) {

            var sourceBlock = AsciiDocFactory.sourceBlock(doc,
                    languageAndOptions,

                    _Text.normalize(TextUtils.readLines(source))
                    .stream()
                    .collect(Collectors.joining("\n")));

            _Strings.nonEmpty(title)
                .ifPresent(sourceBlock::setTitle);

            return sourceBlock;
        }

        public static String asAdocSource() {

            var doc = AsciiDocFactory.doc();
            return AsciiDocWriter.toString(doc);
        }

//        [source,java]
//        .title
//        ----
//        public class SomeClass extends SomeOtherClass {
//            ...
//        }
//        ----
        public static Block java(
                final @NonNull Document doc,
                final @NonNull String javaSource,
                final @Nullable String title) {
            return sourceBlock(doc, "java", javaSource, title);
        }

        public static Block json(
                final @NonNull Document doc,
                final @NonNull String jsonSource,
                final @Nullable String title) {
            return sourceBlock(doc, "json", jsonSource, title);
        }

        public static Block xml(
                final @NonNull Document doc,
                final @NonNull String xmlSource,
                final @Nullable String title) {
            return sourceBlock(doc, "xml", xmlSource, title);
        }

        public static Block yaml(
                final @NonNull Document doc,
                final @NonNull String yamlSource,
                final @Nullable String title) {
            return sourceBlock(doc, "yaml", yamlSource, title);
        }

    }

    public static class DiagramFactory {

        public static Block diagramBlock(
                final @NonNull Document doc,
                final @NonNull String diagramType,
                final @NonNull Can<String> diagramOptions,
                final @NonNull String source,
                final @Nullable String title) {

            var sourceBlock = AsciiDocFactory.diagramBlock(doc,
                    diagramType,
                    diagramOptions,
                    _Text.normalize(TextUtils.readLines(source))
                    .stream()
                    .collect(Collectors.joining("\n")));

            _Strings.nonEmpty(title)
                .ifPresent(sourceBlock::setTitle);

            return sourceBlock;
        }

//      [plantuml,c4-demo,png]
//      ----
//      @startuml
//      ...
//      @enduml
//      ----
        public static Block plantumlPng(
                final @NonNull Document doc,
                final @NonNull String plantumlSource,
                final @NonNull String diagramKey,
                final @Nullable String title) {
            return diagramBlock(doc, "plantuml", Can.of(diagramKey, "png"), plantumlSource, title);
        }

        public static Block plantumlSvg(
                final @NonNull Document doc,
                final @NonNull String plantumlSource,
                final @NonNull String diagramKey,
                final @Nullable String title) {
            return diagramBlock(doc, "plantuml", Can.of(diagramKey, "svg"), plantumlSource, title);
        }

    }

    // -- HELPER

    private static Column getOrCreateColumn(final Table table, final int colIndex) {
        int maxIndexAvailable = table.getColumns().size() - 1;
        int colsToBeCreated = colIndex - maxIndexAvailable;
        for(int i=0; i<colsToBeCreated; ++i) {
            col(table);
        }
        return table.getColumns().get(colIndex);
    }

    private static Row getOrCreateRow(final Table table, final int rowIndex) {
        int maxIndexAvailable = table.getBody().size() - 1;
        int rowsToBeCreated = rowIndex - maxIndexAvailable;
        for(int i=0; i<rowsToBeCreated; ++i) {
            row(table);
        }
        return table.getBody().get(rowIndex);
    }

    private static Row getOrCreateHeadRow(final Table table, final int rowIndex) {
        int maxIndexAvailable = table.getHeader().size() - 1;
        int rowsToBeCreated = rowIndex - maxIndexAvailable;
        for(int i=0; i<rowsToBeCreated; ++i) {
            headRow(table);
        }
        return table.getHeader().get(rowIndex);
    }

    private static Row getOrCreateFootRow(final Table table, final int rowIndex) {
        int maxIndexAvailable = table.getFooter().size() - 1;
        int rowsToBeCreated = rowIndex - maxIndexAvailable;
        for(int i=0; i<rowsToBeCreated; ++i) {
            footRow(table);
        }
        return table.getFooter().get(rowIndex);
    }

    private static Block admonition(final String label, final StructuralNode parent, final String source) {
        var admonition = block(parent, source);
        admonition.setStyle(label.toUpperCase());
        admonition.setAttribute("textlabel", label, true);
        admonition.setAttribute("name", label.toLowerCase(), true);
        return admonition;
    }

}
