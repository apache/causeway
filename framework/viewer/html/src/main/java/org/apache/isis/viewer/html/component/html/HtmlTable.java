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

package org.apache.isis.viewer.html.component.html;

import java.io.PrintWriter;

import org.apache.isis.viewer.html.PathBuilder;
import org.apache.isis.viewer.html.component.Component;
import org.apache.isis.viewer.html.component.ComponentComposite;
import org.apache.isis.viewer.html.component.Table;

public class HtmlTable extends ComponentComposite implements Table {
    private String summary;
    private final TableHeader header;
    private final int noColumns;
    private Row row;
    private int cellCount;
    private final boolean addSelector;

    public HtmlTable(final PathBuilder pathBuilder, final int noColumns, final boolean withSelectorColumn) {
        super(pathBuilder);
        this.noColumns = noColumns + (withSelectorColumn ? 1 : 0);
        addSelector = withSelectorColumn;
        header = new TableHeader(pathBuilder);
    }

    public Row newRow() {
        final Row row = new Row(pathBuilder);
        add(row);
        return row;
    }

    @Override
    public void setSummary(final String summary) {
        this.summary = summary;
    }

    @Override
    public void write(final PrintWriter writer) {
        writer.print("<table summary=\"" + summary + "\">");
        writer.print("<tr><th></th>");
        header.write(writer);
        writer.println("</tr>");
        super.write(writer);
        writer.println("</table>");
    }

    @Override
    protected void write(final PrintWriter writer, final Component component) {
        writer.print("<tr>");
        component.write(writer);
        if (addSelector) {
            writer.print("<td><input type=\"checkbox\" value=\"selected\"/></td>");
        }
        writer.println("</tr>");
    }

    @Override
    public void addCell(final String value, final boolean truncate) {
        row.addCell(value, truncate);
        cellCount++;
        if (cellCount > noColumns) {
            throw new HtmlException("Too many cells added: " + cellCount);
        }
    }

    @Override
    public void addEmptyCell() {
        addCell(new Span("empty-cell", "", null));
    }

    @Override
    public void addCell(final Component component) {
        row.add(component);
        cellCount++;
        if (cellCount > noColumns) {
            throw new HtmlException("Too many cells added: " + cellCount);
        }
    }

    @Override
    public void addColumnHeader(final String name) {
        header.addHeader(name);
        cellCount++;
        if (cellCount > noColumns) {
            throw new HtmlException("Too many cells added: " + cellCount);
        }
    }

    @Override
    public void addRowHeader(final Component component) {
        row = new Row(pathBuilder);
        add(row);
        cellCount = 0;
        row.addCell(component);
    }

}
