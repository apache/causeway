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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import org.apache.causeway.extensions.tabular.pdf.factory.internal.Cell;
import org.apache.causeway.extensions.tabular.pdf.factory.internal.Row;
import org.apache.causeway.extensions.tabular.pdf.factory.internal.image.Image;

record CellFactory(Row<?> row, Cell<?> template) {

    public Cell<?> createCell(final int i, final float width, final List<Object> rowData) {

        Object cellValue = null;
        if (rowData.size() >= i) {
            cellValue = rowData.get(i);
            if (cellValue instanceof String s) {
                cellValue = s.replaceAll("\n", "<br>");
            }
        }
        if(cellValue==null) cellValue = "";

        var cell = switch(cellValue.getClass().getSimpleName()) {
            case "BufferedImage" -> row.createImageCell(width, new Image((BufferedImage)cellValue));
            default -> row.createCell(width, toString(cellValue));
        };

        cell.copyCellStyle(template);

        return cell;
    }

    /**
     * @param cellValue not {@code null} nor {@link BufferedImage}
     */
    private String toString(final Object valueAsObj) {
        // String
        if(valueAsObj instanceof CharSequence value) {
            return value.toString();
        }

        // boolean
        if(valueAsObj instanceof Boolean value) {
            return value ? "✔" : "━";
        }

        // date
        if(valueAsObj instanceof Date value) {
            var dateTime = LocalDateTime.ofInstant(value.toInstant(), ZoneId.systemDefault());
            return DateTimeFormatter.ISO_DATE_TIME.format(dateTime);
        }
        if(valueAsObj instanceof LocalDate value) {
            return DateTimeFormatter.ISO_DATE.format(value);
        }
        if(valueAsObj instanceof LocalDateTime value) {
            return DateTimeFormatter.ISO_DATE_TIME.format(value);
        }
        if(valueAsObj instanceof OffsetDateTime value) {
            return DateTimeFormatter.ISO_DATE_TIME.format(value);
        }

        // number
        if(valueAsObj instanceof Double value) {
            value.toString();
        }
        if(valueAsObj instanceof Float value) {
            value.toString();
        }
        if(valueAsObj instanceof BigDecimal value) {
            value.toString();
        }
        if(valueAsObj instanceof BigInteger value) {
            value.toString();
        }
        if(valueAsObj instanceof Long value) {
            value.toString();
        }
        if(valueAsObj instanceof Integer value) {
            value.toString();
        }
        if(valueAsObj instanceof Short value) {
            value.toString();
        }
        if(valueAsObj instanceof Byte value) {
            value.toString();
        }

        // if all else fails fallback to value's toString method
        return valueAsObj.toString();
    }

}
