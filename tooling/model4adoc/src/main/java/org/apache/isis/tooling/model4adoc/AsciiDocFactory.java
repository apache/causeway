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

import javax.annotation.Nullable;

import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.Column;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ListItem;
import org.asciidoctor.ast.Row;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;

import org.apache.isis.commons.collections.Can;
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

    // -- ATTRIBUTES

    public static void attrNotice(Document node, String value) {
        node.setAttribute("notice", value, true);
    }

    // -- ADMONITIONS

    //NOTE
    //TIP
    //IMPORTANT
    //CAUTION
    //WARNING

    public static Block note(StructuralNode parent) {
        return admonition("Note", parent, null);
    }

    public static Block note(StructuralNode parent, String source) {
        return admonition("Note", parent, source);
    }

    public static Block tip(StructuralNode parent) {
        return admonition("Tip", parent, null);
    }

    public static Block tip(StructuralNode parent, String source) {
        return admonition("Tip", parent, source);
    }

    public static Block important(StructuralNode parent) {
        return admonition("Important", parent, null);
    }

    public static Block important(StructuralNode parent, String source) {
        return admonition("Important", parent, source);
    }

    public static Block caution(StructuralNode parent) {
        return admonition("Caution", parent, null);
    }

    public static Block caution(StructuralNode parent, String source) {
        return admonition("Caution", parent, source);
    }

    public static Block warning(StructuralNode parent) {
        return admonition("Warning", parent, null);
    }

    public static Block warning(StructuralNode parent, String source) {
        return admonition("Warning", parent, source);
    }

    // -- BLOCK

    public static Block block(StructuralNode parent) {
        return block(parent, null);
    }

    public static Block block(StructuralNode parent, String source) {
        val block = new SimpleBlock();
        block.setSource(source);
        block.setLevel(parent.getLevel());
        parent.getBlocks().add(block);
        block.setParent(parent);
        return block;
    }

    public static Block openBlock(ListItem listItem) {
        val openBlock = block(listItem);
        openBlock.setStyle("open");
        return openBlock;
    }

    public static Block listingBlock(StructuralNode parent, @NonNull String source) {
        val listingBlock = block(parent, source);
        listingBlock.setStyle("listing");
        return listingBlock;
    }

    // -- FOOTNOTES

    public static org.asciidoctor.ast.List footnotes(StructuralNode parent) {
        val footnoteList = list(parent);
        footnoteList.setStyle("arabic");
        return footnoteList;
    }

    public static ListItem footnote(org.asciidoctor.ast.List parent, @NonNull String source) {
        return listItem(parent, source);
    }

    // -- TABLE

    public static Table table(StructuralNode parent) {
        val table = new SimpleTable();
        parent.getBlocks().add(table);
        table.setParent(parent);
        return table;
    }

    public static Column col(Table table) {
        val column = new SimpleColumn();
        table.getColumns().add(column);
        column.setParent(table);
        return column;
    }

    public static Row row(Table table) {
        val row = new SimpleRow();
        table.getBody().add(row);
        return row;
    }

    public static Row headRow(Table table) {
        val row = new SimpleRow();
        table.getHeader().add(row);
        return row;
    }

    public static Row footRow(Table table) {
        val row = new SimpleRow();
        table.getFooter().add(row);
        return row;
    }

    public static Cell cell(Row row, Column column, String source) {
        val cell = new SimpleCell();
        row.getCells().add(cell);
        cell.setParent(column);
        cell.setSource(source);
        return cell;
    }

    public static Cell cell(Table table, Row row, String source) {
        val colIndex = row.getCells().size();
        val column = getOrCreateColumn(table, colIndex);
        return cell(row, column, source);
    }

    public static Cell cell(Table table, int rowIndex, int colIndex, String source) {
        val row = getOrCreateRow(table, rowIndex);
        val col = getOrCreateColumn(table, colIndex);
        return cell(row, col, source);
    }

    public static Cell headCell(Table table, int rowIndex, int colIndex, String source) {
        val row = getOrCreateHeadRow(table, rowIndex);
        val col = getOrCreateColumn(table, colIndex);
        return cell(row, col, source);
    }

    public static Cell footCell(Table table, int rowIndex, int colIndex, String source) {
        val row = getOrCreateFootRow(table, rowIndex);
        val col = getOrCreateColumn(table, colIndex);
        return cell(row, col, source);
    }

    public static org.asciidoctor.ast.List list(StructuralNode parent) {
        val list = new SimpleList();
        list.setLevel(parent.getLevel()+1);
        parent.getBlocks().add(list);
        list.setParent(parent);
        return list;
    }

    public static ListItem listItem(org.asciidoctor.ast.List parent) {
        return listItem(parent, null);
    }

    public static ListItem listItem(org.asciidoctor.ast.List parent, String source) {
        val listItem = new SimpleListItem();
        listItem.setLevel(parent.getLevel());
        parent.getItems().add(listItem);
        listItem.setParent(parent);
        listItem.setSource(source);
        return listItem;
    }

    public static class SourceFactory {

        public static String wrap(@NonNull String sourceType, @NonNull String source, @Nullable String title, Can<String> options) {
            val sb = new StringBuilder();
            if(_Strings.isNotEmpty(title)) {
                val trimmedTitle = title.trim();
                if(!trimmedTitle.isEmpty()) {
                    sb.append(".").append(trimmedTitle).append("\n");
                }
            }
            sb.append("[").append(sourceType);
            options.stream().map(String::trim).filter(_Strings::isNotEmpty).map(s->","+s).forEach(sb::append);
            sb.append("]\n").append("----\n");
            _Text.normalize(_Text.getLines(source))
            .forEach(line->sb.append(line).append("\n"));
            sb.append("----\n");
            return sb.toString();
        }

        public static String xml(@NonNull String xmlSource, @Nullable String title) {
            return wrap("source,xml", xmlSource, title, Can.empty());
        }

//        [source,java]
//        .title
//        ----
//        public class SomeClass extends SomeOtherClass {
//            ...
//        }
//        ----
        public static String java(@NonNull String javaSource, @Nullable String title) {
            return wrap("source,java", javaSource, title, Can.empty());
        }

        public static String json(@NonNull String jsonSource, @Nullable String title) {
            return wrap("source,json", jsonSource, title, Can.empty());
        }

        public static String yaml(@NonNull String yamlSource, @Nullable String title) {
            return wrap("source,yaml", yamlSource, title, Can.empty());
        }

//      [plantuml,c4-demo,png]
//      ----
//      @startuml
//      ...
//      @enduml
//      ----
        public static String plantuml(@NonNull String plantumlSource, @NonNull String diagramKey, @Nullable String title) {
            return wrap(String.format("plantuml,%s,png", diagramKey), plantumlSource, title, Can.of());
        }


    }

    // -- HELPER

    private static Column getOrCreateColumn(Table table, int colIndex) {
        int maxIndexAvailable = table.getColumns().size() - 1;
        int colsToBeCreated = colIndex - maxIndexAvailable;
        for(int i=0; i<colsToBeCreated; ++i) {
            col(table);
        }
        return table.getColumns().get(colIndex);
    }

    private static Row getOrCreateRow(Table table, int rowIndex) {
        int maxIndexAvailable = table.getBody().size() - 1;
        int rowsToBeCreated = rowIndex - maxIndexAvailable;
        for(int i=0; i<rowsToBeCreated; ++i) {
            row(table);
        }
        return table.getBody().get(rowIndex);
    }

    private static Row getOrCreateHeadRow(Table table, int rowIndex) {
        int maxIndexAvailable = table.getHeader().size() - 1;
        int rowsToBeCreated = rowIndex - maxIndexAvailable;
        for(int i=0; i<rowsToBeCreated; ++i) {
            headRow(table);
        }
        return table.getHeader().get(rowIndex);
    }

    private static Row getOrCreateFootRow(Table table, int rowIndex) {
        int maxIndexAvailable = table.getFooter().size() - 1;
        int rowsToBeCreated = rowIndex - maxIndexAvailable;
        for(int i=0; i<rowsToBeCreated; ++i) {
            footRow(table);
        }
        return table.getFooter().get(rowIndex);
    }

    private static Block admonition(String label, StructuralNode parent, String source) {
        val admonition = block(parent, source);
        admonition.setStyle(label.toUpperCase());
        admonition.setAttribute("textlabel", label, true);
        admonition.setAttribute("name", label.toLowerCase(), true);
        return admonition;
    }




}
