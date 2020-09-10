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

import org.asciidoctor.ast.Cell;
import org.asciidoctor.ast.Column;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Row;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;

import org.apache.isis.tooling.model4adoc.ast.SimpleCell;
import org.apache.isis.tooling.model4adoc.ast.SimpleColumn;
import org.apache.isis.tooling.model4adoc.ast.SimpleDocument;
import org.apache.isis.tooling.model4adoc.ast.SimpleRow;
import org.apache.isis.tooling.model4adoc.ast.SimpleTable;

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

    public static Table table(StructuralNode parent) {
        val table = new SimpleTable();
        parent.getBlocks().add(table);
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
    

    
}
