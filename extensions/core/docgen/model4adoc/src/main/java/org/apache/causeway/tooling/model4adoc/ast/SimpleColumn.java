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
package org.apache.causeway.tooling.model4adoc.ast;

import org.asciidoctor.ast.Column;
import org.asciidoctor.ast.Table;

import lombok.Getter;
import lombok.Setter;

public class SimpleColumn extends ContentNodeAbstract implements Column {

    @Getter @Setter private String style;

    @Override
    public Table getTable() {
        return (Table) getParent();
    }

    @Override
    public int getColumnNumber() {
        Number columnNumber = (Number) getAttribute("colnumber");
        return columnNumber == null ? -1 : columnNumber.intValue();
    }

    @Override
    public int getWidth() {
        Number width = (Number) getAttribute("width");
        return width == null ? 0 : width.intValue();
    }

    @Override
    public void setWidth(int width) {
        setAttribute("width", width, true);
    }

    @Override
    public Table.HorizontalAlignment getHorizontalAlignment() {
        return Table.HorizontalAlignment.valueOf(((String) getAttribute("halign", "left")).toUpperCase());
    }

    @Override
    public void setHorizontalAlignment(Table.HorizontalAlignment halign) {
        setAttribute("halign", halign.name().toLowerCase(), true);
    }

    @Override
    public Table.VerticalAlignment getVerticalAlignment() {
        return Table.VerticalAlignment.valueOf(((String) getAttribute("valign", "top")).toUpperCase());
    }

    @Override
    public void setVerticalAlignment(Table.VerticalAlignment valign) {
        setAttribute("valign", valign.name().toLowerCase(), true);
    }

}
