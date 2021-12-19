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
package org.apache.isis.subdomains.excel.applib.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

final class _CellMarshaller {

    private final CellStyle dateCellStyle;
    private final CellStyle defaultCellStyle;
    private final BookmarkService bookmarkService;

    _CellMarshaller(
            final BookmarkService bookmarkService,
            final CellStyle dateCellStyle,
            final CellStyle defaultCellStyle){
        this.bookmarkService = bookmarkService;
        this.dateCellStyle = dateCellStyle;
        this.defaultCellStyle = defaultCellStyle;
    }

    void setCellValue(
            final ManagedObject objectAdapter,
            final OneToOneAssociation otoa,
            final Cell cell) {

        final ManagedObject propertyAdapter = otoa.get(objectAdapter);

        // null
        if (propertyAdapter == null) {
            cell.setBlank();
            cell.setCellStyle(defaultCellStyle);
            return;
        }

        final ObjectSpecification propertySpec = otoa.getElementType();
        final Object propertyAsObj = propertyAdapter.getPojo();
        final String propertyAsTitle = propertyAdapter.titleString();

        // value types
        if(propertySpec.isValue()) {
            if(setCellValue(cell, propertyAsObj)) {
                return;
            }
        }

        // reference types
        if(!propertySpec.isNonScalar()) {
            setCellValueForBookmark(cell, propertyAsObj, propertyAsTitle, defaultCellStyle);
            return;
        }

        // fallback, best effort
        setCellValueForString(cell, propertyAsTitle, defaultCellStyle);
        return;
    }

    void setCellValueForHyperlink(
            final ManagedObject objectAdapter,
            final OneToOneAssociation otoa,
            final Cell cell) {

        final ManagedObject propertyAdapter = otoa.get(objectAdapter);

        // null
        if (propertyAdapter == null) {
            cell.setBlank();
            cell.setCellStyle(defaultCellStyle);
            return;
        }

        // only String type expected
        if(propertyAdapter.getPojo() instanceof String) {

            String stringValue = (String) propertyAdapter.getPojo();

            cell.setCellValue(stringValue);

            CreationHelper createHelper = cell.getSheet().getWorkbook().getCreationHelper();
            XSSFHyperlink link = (XSSFHyperlink)createHelper.createHyperlink(HyperlinkType.URL);
            link.setAddress(stringValue);
            cell.setHyperlink(link);

            cell.setCellStyle(defaultCellStyle);

        } else {
            // silently ignore annotation and fall back
            setCellValue(objectAdapter, otoa, cell);
        }

    }

    private boolean setCellValue(final Cell cell, final Object valueAsObj) {
        if(valueAsObj == null) {
            cell.setBlank();
            cell.setCellStyle(defaultCellStyle);
            return true;
        }

        // string
        if(valueAsObj instanceof String) {
            String value = (String) valueAsObj;
            setCellValueForString(cell, value, defaultCellStyle);
            return true;
        }

        // boolean
        if(valueAsObj instanceof Boolean) {
            Boolean value = (Boolean) valueAsObj;
            cell.setCellValue(value);
            cell.setCellStyle(defaultCellStyle);
            return true;
        }

        // date
        if(valueAsObj instanceof Date) {
            Date value = (Date) valueAsObj;
            setCellValueForDate(cell, value, dateCellStyle);
            return true;
        }
//        if(valueAsObj instanceof org.apache.isis.applib.value.Date) {
//            org.apache.isis.applib.value.Date value = (org.apache.isis.applib.value.Date) valueAsObj;
//            Date dateValue = value.dateValue();
//            setCellValueForDate(cell, dateValue, dateCellStyle);
//            return true;
//        }
//        if(valueAsObj instanceof org.apache.isis.applib.value.DateTime) {
//            org.apache.isis.applib.value.DateTime value = (org.apache.isis.applib.value.DateTime) valueAsObj;
//            Date dateValue = value.dateValue();
//            setCellValueForDate(cell, dateValue, dateCellStyle);
//            return true;
//        }
        if(valueAsObj instanceof LocalDate) {
            LocalDate value = (LocalDate) valueAsObj;
            Date date = value.toDateTimeAtStartOfDay().toDate();
            setCellValueForDate(cell, date, dateCellStyle);
            return true;
        }
        if(valueAsObj instanceof LocalDateTime) {
            LocalDateTime value = (LocalDateTime) valueAsObj;
            Date date = value.toDate();
            setCellValueForDate(cell, date, dateCellStyle);
            return true;
        }
        if(valueAsObj instanceof DateTime) {
            DateTime value = (DateTime) valueAsObj;
            Date date = value.toDate();
            setCellValueForDate(cell, date, dateCellStyle);
            return true;
        }

        // number
        if(valueAsObj instanceof Double) {
            Double value = (Double) valueAsObj;
            setCellValueForDouble(cell, value, defaultCellStyle);
            return true;
        }
        if(valueAsObj instanceof Float) {
            Float value = (Float) valueAsObj;
            setCellValueForDouble(cell, value, defaultCellStyle);
            return true;
        }
        if(valueAsObj instanceof BigDecimal) {
            BigDecimal value = (BigDecimal) valueAsObj;
            setCellValueForDouble(cell, value.doubleValue(), defaultCellStyle);
            return true;
        }
        if(valueAsObj instanceof BigInteger) {
            BigInteger value = (BigInteger) valueAsObj;
            setCellValueForDouble(cell, value.doubleValue(), defaultCellStyle);
            return true;
        }
        if(valueAsObj instanceof Long) {
            Long value = (Long) valueAsObj;
            setCellValueForDouble(cell, value, defaultCellStyle);
            return true;
        }
        if(valueAsObj instanceof Integer) {
            Integer value = (Integer) valueAsObj;
            setCellValueForDouble(cell, value, defaultCellStyle);
            return true;
        }
        if(valueAsObj instanceof Short) {
            Short value = (Short) valueAsObj;
            setCellValueForDouble(cell, value, defaultCellStyle);
            return true;
        }
        if(valueAsObj instanceof Byte) {
            Byte value = (Byte) valueAsObj;
            setCellValueForDouble(cell, value, defaultCellStyle);
            return true;
        }
        if(valueAsObj instanceof Enum) {
            Enum<?> value = (Enum<?>) valueAsObj;
            setCellValueForEnum(cell, (Enum<?>)value, defaultCellStyle);
            return true;
        }
        return false;
    }

    private static void setCellValueForString(final Cell cell, final String objectAsStr, final CellStyle cellStyle) {
        // char 10 is for linebreak within a cell; to display correctly wrap text needs to be set to true
        if (objectAsStr.contains(Character.toString((char)10))) {
            CellStyle wrappedCellStyle = cell.getSheet().getWorkbook().createCellStyle();
            wrappedCellStyle.setVerticalAlignment(VerticalAlignment.TOP);
            wrappedCellStyle.setWrapText(true);
            cell.setCellStyle(wrappedCellStyle);
        } else {
            cell.setCellStyle(cellStyle);
        }
        cell.setCellValue(objectAsStr);
    }

    private void setCellValueForBookmark(final Cell cell, final Object propertyAsObject, final String propertyAsTitle, final CellStyle cellStyle) {
        Bookmark bookmark = bookmarkService.bookmarkForElseFail(propertyAsObject);
        setCellComment(cell, bookmark.toString());

        cell.setCellValue(propertyAsTitle);
        cell.setCellStyle(cellStyle);
    }

    private static void setCellComment(final Cell cell, final String commentText) {
        Sheet sheet = cell.getSheet();
        Row row = cell.getRow();
        Workbook workbook = sheet.getWorkbook();
        CreationHelper creationHelper = workbook.getCreationHelper();
        ClientAnchor anchor = creationHelper.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex()+1);
        anchor.setRow1(row.getRowNum());
        anchor.setRow2(row.getRowNum()+3);

        Drawing<?> drawing = sheet.createDrawingPatriarch();
        Comment comment1 = drawing.createCellComment(anchor);

        RichTextString commentRtf = creationHelper.createRichTextString(commentText);

        comment1.setString(commentRtf);
        Comment comment = comment1;
        cell.setCellComment(comment);
    }

    private static <E extends Enum<E>> void setCellValueForEnum(final Cell cell, final Enum<E> objectAsStr, final CellStyle cellStyle) {
        cell.setCellValue(objectAsStr.name());
        cell.setCellStyle(cellStyle);
    }

    private static void setCellValueForDouble(final Cell cell, final double value, final CellStyle cellStyle) {
        cell.setCellValue(value);
        cell.setCellStyle(cellStyle);
    }

    private static void setCellValueForDate(final Cell cell, final Date date, final CellStyle dateCellStyle) {
        cell.setCellValue(date);
        cell.setCellStyle(dateCellStyle);
    }

    String getStringCellValue(final Cell cell) {
        return getCellValue(cell, String.class);
    }

    Object getCellValue(final Cell cell, final OneToOneAssociation otoa) {

        final CellType cellType = cell.getCellType();

        if(cellType == CellType.BLANK) {
            return null;
        }

        final ObjectSpecification propertySpec = otoa.getElementType();
        Class<?> requiredType = propertySpec.getCorrespondingClass();

        // value types
        if(propertySpec.isValue()) {
            return getCellValue(cell, requiredType);
        }

        // reference types
        if(!propertySpec.isNonScalar()) {
            return getCellComment(cell, requiredType);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T getCellValue(final Cell cell, final Class<T> requiredType) {
        final CellType cellType = cell.getCellType();

        if(requiredType == boolean.class || requiredType == Boolean.class) {
            if(cellType == CellType.BOOLEAN) {
                boolean booleanCellValue = cell.getBooleanCellValue();
                return (T) Boolean.valueOf(booleanCellValue);
            } else {
                return null;
            }
        }

        // enum
        if(Enum.class.isAssignableFrom(requiredType)) {
            String stringCellValue = cell.getStringCellValue();
            @SuppressWarnings("rawtypes")
            Class rawType = requiredType;
            return (T) Enum.valueOf(rawType, stringCellValue);
        }

        // date
        if(requiredType == java.util.Date.class) {
            java.util.Date dateCellValue = cell.getDateCellValue();
            return (T) dateCellValue;
        }

//        if(requiredType == org.apache.isis.applib.value.Date.class) {
//            java.util.Date dateCellValue = cell.getDateCellValue();
//            return (T)new org.apache.isis.applib.value.Date(dateCellValue);
//        }

//        if(requiredType == org.apache.isis.applib.value.DateTime.class) {
//            java.util.Date dateCellValue = cell.getDateCellValue();
//            return (T)new org.apache.isis.applib.value.DateTime(dateCellValue);
//        }

        if(requiredType == LocalDate.class) {
            java.util.Date dateCellValue = cell.getDateCellValue();
            return (T) new LocalDate(dateCellValue.getTime());
        }

        if(requiredType == LocalDateTime.class) {
            java.util.Date dateCellValue = cell.getDateCellValue();
            return (T) new LocalDateTime(dateCellValue.getTime());
        }

        if(requiredType == DateTime.class) {
            java.util.Date dateCellValue = cell.getDateCellValue();
            return (T) new DateTime(dateCellValue.getTime());
        }


        // number
        if(requiredType == double.class || requiredType == Double.class) {
            if(cellType == CellType.NUMERIC) {
                double doubleValue = cell.getNumericCellValue();
                return (T) Double.valueOf(doubleValue);
            } else {
                return null;
            }
        }

        if(requiredType == float.class || requiredType == Float.class) {
            if(cellType == CellType.NUMERIC) {
                float floatValue = (float)cell.getNumericCellValue();
                return (T) Float.valueOf(floatValue);
            } else {
                return null;
            }
        }

        if(requiredType == BigDecimal.class) {
            if(cellType == CellType.NUMERIC) {
                double doubleValue = cell.getNumericCellValue();
                return (T) BigDecimal.valueOf(doubleValue);
            } else {
                return null;
            }
        }

        if(requiredType == BigInteger.class) {
            if(cellType == CellType.NUMERIC) {
                long longValue = (long)cell.getNumericCellValue();
                return (T) BigInteger.valueOf(longValue);
            } else {
                return null;
            }
        }

        if(requiredType == long.class || requiredType == Long.class) {
            if(cellType == CellType.NUMERIC) {
                long longValue = (long) cell.getNumericCellValue();
                return (T) Long.valueOf(longValue);
            } else {
                return null;
            }
        }

        if(requiredType == int.class || requiredType == Integer.class) {
            if(cellType == CellType.NUMERIC) {
                int intValue = (int) cell.getNumericCellValue();
                return (T) Integer.valueOf(intValue);
            } else {
                return null;
            }
        }

        if(requiredType == short.class || requiredType == Short.class) {
            if(cellType == CellType.NUMERIC) {
                short shortValue = (short) cell.getNumericCellValue();
                return (T) Short.valueOf(shortValue);
            } else {
                return null;
            }
        }

        if(requiredType == byte.class || requiredType == Byte.class) {
            if(cellType == CellType.NUMERIC) {
                byte byteValue = (byte) cell.getNumericCellValue();
                return (T) Byte.valueOf(byteValue);
            } else {
                return null;
            }
        }

        if(requiredType == String.class) {
            if(cellType == CellType.STRING) {
                return (T) cell.getStringCellValue();
            } else if(cellType == CellType.NUMERIC) {
            	/*
            	 * In some cases, when editing a string type cell in excel and
            	 * when the cell content is just a number excel will silently
            	 * convert the cell type to numeric. To remedy unexpected
            	 * behavior we check whether we can recover numeric cells as
            	 * text, accounting for the fact that an integer number is
            	 * also stored with a floating point in excel, whereby
            	 * a textual representation as such may be undesired.
            	 *
            	 * @ see https://stackoverflow.com/questions/9898512/how-to-test-if-a-double-is-an-integer
            	 * @see https://docs.oracle.com/javase/8/docs/api/java/lang/Math.html#floor-double-
            	 * - If the argument value is already equal to a mathematical integer, then the result is the same as the argument.
            	 * - If the argument is NaN or an infinity or positive zero or negative zero, then the result is the same as the argument.
            	 */
            	double val = cell.getNumericCellValue();
        		if((val == Math.floor(val)) && !Double.isInfinite(val)) {
        			return (T) Integer.toString((int) val);
        		}
        		return (T) Double.toString(val);
            } else {
            	return null;
            }
        }

        return null;
    }


    private Object getCellComment(final Cell cell, final Class<?> requiredType) {
        final Comment comment = cell.getCellComment();
        if(comment == null) {
            return null;
        }
        final RichTextString commentRts = comment.getString();
        if(commentRts == null) {
            return null;
        }
        final String bookmarkStr = commentRts.getString();
        final Bookmark bookmark = Bookmark.parse(bookmarkStr).orElse(null);
        return bookmarkService.lookup(bookmark, requiredType).orElse(null);
    }


}