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

import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.Column;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.Row;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;
import org.springframework.lang.Nullable;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.base._Text;
import org.apache.isis.tooling.model4adoc.ast.SimpleBlock;
import org.apache.isis.tooling.model4adoc.ast.SimpleCell;
import org.apache.isis.tooling.model4adoc.ast.SimpleColumn;
import org.apache.isis.tooling.model4adoc.ast.SimpleDocument;
import org.apache.isis.tooling.model4adoc.ast.SimpleList;
import org.apache.isis.tooling.model4adoc.ast.SimpleListItem;
import org.apache.isis.tooling.model4adoc.ast.SimpleRow;
import org.apache.isis.tooling.model4adoc.ast.SimpleTable;

import lombok.NonNull;
import lombok.val;

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
        val doc = doc();
        documentBuilder.accept(doc);
        return doc;
    }

    /**
     * syntactic sugar
     */
    public static String toString(final Consumer<Document> documentBuilder) {
        val doc = doc();
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

    // -- BLOCK

    public static Block block(final StructuralNode parent) {
        return block(parent, null);
    }

    public static Block block(final StructuralNode parent, final String source) {
        val block = new SimpleBlock();
        block.setSource(source);
        block.setLevel(parent.getLevel());
        parent.getBlocks().add(block);
        block.setParent(parent);
        return block;
    }

    public static Block openBlock(final ListItem listItem) {
        val openBlock = block(listItem);
        openBlock.setStyle("open");
        return openBlock;
    }

    public static Block listingBlock(final StructuralNode parent, @NonNull final String source) {
        val listingBlock = block(parent, source);
        listingBlock.setStyle("listing");
        return listingBlock;
    }

    public static Block sourceBlock(final StructuralNode parent, @Nullable final String language, @NonNull final String source) {
        val sourceBlock = block(parent, source);
        sourceBlock.setStyle("source");
        if(_Strings.isNotEmpty(language)) {
            sourceBlock.setAttribute("language", language, true);
        }
        return sourceBlock;
    }

    public static Block diagramBlock(
            final StructuralNode parent,
            @NonNull final String diagramType,
            @NonNull final Can<String> diagramOptions,
            @NonNull final String source) {

        val diagramBlock = block(parent, source);

        _Strings.nonEmpty(diagramType)
            .ifPresent(diagramBlock::setStyle);

        val attributes = diagramOptions.add(0, diagramType);
        val attributeIndex = _Refs.intRef(1);

        // add options
        attributes.forEach(opt->{
            diagramBlock.setAttribute(""+attributeIndex.getValue(), opt, true);
            attributeIndex.incAndGet();
        });

        return diagramBlock;
    }

    // -- CALLOUTS

    public static org.asciidoctor.ast.List callouts(final StructuralNode parent) {
        val calloutList = list(parent);
        calloutList.setStyle("arabic");
        return calloutList;
    }

    public static ListItem callout(final org.asciidoctor.ast.List parent, @NonNull final String source) {
        return listItem(parent, source);
    }

    // -- COLLAPSIBLE

    public static Block collapsibleBlock(final StructuralNode parent, @NonNull final String source) {
        val collapsibleBlock = block(parent, source);
        collapsibleBlock.setStyle("example");
        collapsibleBlock.setAttribute("collapsible-option", "1", true);
        return collapsibleBlock;
    }

    // -- TABLE

    public static Table table(final StructuralNode parent) {
        val table = new SimpleTable();
        parent.getBlocks().add(table);
        table.setParent(parent);
        return table;
    }

    public static Column col(final Table table) {
        val column = new SimpleColumn();
        table.getColumns().add(column);
        column.setParent(table);
        return column;
    }

    public static Row row(final Table table) {
        val row = new SimpleRow();
        table.getBody().add(row);
        return row;
    }

    public static Row headRow(final Table table) {
        val row = new SimpleRow();
        table.getHeader().add(row);
        return row;
    }

    public static Row footRow(final Table table) {
        val row = new SimpleRow();
        table.getFooter().add(row);
        return row;
    }

    public static Cell cell(final Row row, final Column column, final String source) {
        val cell = new SimpleCell();
        row.getCells().add(cell);
        cell.setParent(column);
        cell.setSource(source);
        return cell;
    }

    public static Cell cell(final Table table, final Row row, final String source) {
        val colIndex = row.getCells().size();
        val column = getOrCreateColumn(table, colIndex);
        return cell(row, column, source);
    }

    public static Cell cell(final Table table, final int rowIndex, final int colIndex, final String source) {
        val row = getOrCreateRow(table, rowIndex);
        val col = getOrCreateColumn(table, colIndex);
        return cell(row, col, source);
    }

    public static Cell headCell(final Table table, final int rowIndex, final int colIndex, final String source) {
        val row = getOrCreateHeadRow(table, rowIndex);
        val col = getOrCreateColumn(table, colIndex);
        return cell(row, col, source);
    }

    public static Cell footCell(final Table table, final int rowIndex, final int colIndex, final String source) {
        val row = getOrCreateFootRow(table, rowIndex);
        val col = getOrCreateColumn(table, colIndex);
        return cell(row, col, source);
    }

    public static org.asciidoctor.ast.List list(final StructuralNode parent) {
        val list = new SimpleList();
        list.setLevel(parent.getLevel()+1);
        parent.getBlocks().add(list);
        list.setParent(parent);
        return list;
    }

    public static ListItem listItem(final org.asciidoctor.ast.List parent) {
        return listItem(parent, null);
    }

    public static ListItem listItem(final org.asciidoctor.ast.List parent, final String source) {
        val listItem = new SimpleListItem();
        listItem.setLevel(parent.getLevel());
        parent.getItems().add(listItem);
        listItem.setParent(parent);
        listItem.setSource(source);
        return listItem;
    }

    public static class SourceFactory {

        public static Block sourceBlock(
                @NonNull final Document doc,
                @NonNull final String languageAndOptions,
                @NonNull final String source,
                @Nullable final String title) {

            val sourceBlock = AsciiDocFactory.sourceBlock(doc,
                    languageAndOptions,

                    _Text.normalize(_Text.getLines(source))
                    .stream()
                    .collect(Collectors.joining("\n")));

            _Strings.nonEmpty(title)
                .ifPresent(sourceBlock::setTitle);

            return sourceBlock;
        }

        public static String asAdocSource() {

            val doc = AsciiDocFactory.doc();
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
                @NonNull final Document doc,
                @NonNull final String javaSource,
                @Nullable final String title) {
            return sourceBlock(doc, "java", javaSource, title);
        }

        public static Block json(
                @NonNull final Document doc,
                @NonNull final String jsonSource,
                @Nullable final String title) {
            return sourceBlock(doc, "json", jsonSource, title);
        }

        public static Block xml(
                @NonNull final Document doc,
                @NonNull final String xmlSource,
                @Nullable final String title) {
            return sourceBlock(doc, "xml", xmlSource, title);
        }

        public static Block yaml(
                @NonNull final Document doc,
                @NonNull final String yamlSource,
                @Nullable final String title) {
            return sourceBlock(doc, "yaml", yamlSource, title);
        }

    }

    public static class DiagramFactory {

        public static Block diagramBlock(
                @NonNull final Document doc,
                @NonNull final String diagramType,
                @NonNull final Can<String> diagramOptions,
                @NonNull final String source,
                @Nullable final String title) {

            val sourceBlock = AsciiDocFactory.diagramBlock(doc,
                    diagramType,
                    diagramOptions,
                    _Text.normalize(_Text.getLines(source))
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
                @NonNull final Document doc,
                @NonNull final String plantumlSource,
                @NonNull final String diagramKey,
                @Nullable final String title) {
            return diagramBlock(doc, "plantuml", Can.of(diagramKey, "png"), plantumlSource, title);
        }

        public static Block plantumlSvg(
                @NonNull final Document doc,
                @NonNull final String plantumlSource,
                @NonNull final String diagramKey,
                @Nullable final String title) {
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
        val admonition = block(parent, source);
        admonition.setStyle(label.toUpperCase());
        admonition.setAttribute("textlabel", label, true);
        admonition.setAttribute("name", label.toLowerCase(), true);
        return admonition;
    }


}
