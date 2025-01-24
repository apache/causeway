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
package org.apache.causeway.extensions.tabular.pdf.factory;

import java.awt.image.BufferedImage;
import java.util.List;

import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.Row;
import be.quodlibet.boxable.image.Image;

record CellFactory(Row<?> row, Cell<?> template) {

    public Cell<?> createCell(int i, float width, List<Object> rowData) {
        
        Object cellValue = null;
        if (rowData.size() >= i) {
            cellValue = rowData.get(i);
            if (cellValue instanceof String s) {
                cellValue = s.replaceAll("\n", "<br>");    
            }
        }
        if(cellValue==null) cellValue = "";
        
        var cell = switch(cellValue.getClass().getSimpleName()) {
            case "String" -> row.createCell(width, (String)cellValue);
            case "BufferedImage" -> row.createImageCell(width, new Image((BufferedImage)cellValue));
            default -> row.createCell(width, "Unsupported value type: " + cellValue.getClass().getName());
        };
        
        cell.copyCellStyle(template);
        
        return cell;
    }
    
}
