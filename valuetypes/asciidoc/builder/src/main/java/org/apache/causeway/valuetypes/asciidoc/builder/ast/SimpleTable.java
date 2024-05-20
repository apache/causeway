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
package org.apache.causeway.valuetypes.asciidoc.builder.ast;

import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.ast.Column;
import org.asciidoctor.ast.Row;
import org.asciidoctor.ast.Table;

import lombok.Getter;

public class SimpleTable extends SimpleStructuralNode implements Table {

    @Getter private String frame = "all";
    @Getter private String grid  = "all";
    @Getter private final List<Column> columns = new ArrayList<>();
    @Getter private final List<Row> header = new ArrayList<>();
    @Getter private final List<Row> body = new ArrayList<>();
    @Getter private final List<Row> footer = new ArrayList<>();

    public static final String COLS_ATTR = "cols";
    public static final String FRAME_ATTR = "frame";
    public static final String GRID_ATTR = "grid";

    @Override
    public boolean hasHeaderOption() {
        return false;
    }

    @Override
    public void setFrame(final String frame) {
        setAttribute(FRAME_ATTR, this.frame = frame, true);
    }

    @Override
    public void setGrid(final String grid) {
        setAttribute(GRID_ATTR, this.grid = grid, true);
    }

    @Override
    public void assignColumnWidths() {
        // no-op
    }

}
