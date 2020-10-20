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
package org.apache.isis.subdomains.excel.applib.dom.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.subdomains.excel.applib.dom.AggregationType;
import org.apache.isis.subdomains.excel.applib.dom.ExcelMetaDataEnabled;
import org.apache.isis.subdomains.excel.applib.dom.ExcelService;
import org.apache.isis.subdomains.excel.applib.dom.HyperLink;
import org.apache.isis.subdomains.excel.applib.dom.PivotColumn;
import org.apache.isis.subdomains.excel.applib.dom.PivotDecoration;
import org.apache.isis.subdomains.excel.applib.dom.PivotRow;
import org.apache.isis.subdomains.excel.applib.dom.PivotValue;
import org.apache.isis.subdomains.excel.applib.dom.RowHandler;
import org.apache.isis.subdomains.excel.applib.dom.WorksheetContent;
import org.apache.isis.subdomains.excel.applib.dom.WorksheetSpec;

import lombok.ToString;
import lombok.val;

class ExcelConverter {

    private static final String XLSX_SUFFIX = ".xlsx";

    private static final Predicate<ObjectAssociation> VISIBLE_PROPERTIES =
            ObjectAssociation.Predicates.PROPERTIES.and(
            ObjectAssociation.Predicates.staticallyVisible(Where.STANDALONE_TABLES));

    static class RowFactory {
        private final Sheet sheet;
        private int rowNum;

        RowFactory(final Sheet sheet) {
            this.sheet = sheet;
        }

        public Row newRow() {
            return sheet.createRow(rowNum++);
        }
    }

    // //////////////////////////////////////

    private final SpecificationLoader specificationLoader;
    private final ObjectManager objectManager;
    private final BookmarkService bookmarkService;
    private final ServiceInjector serviceInjector;

    ExcelConverter(
            final SpecificationLoader specificationLoader,
            final ObjectManager objectManager,
            final BookmarkService bookmarkService,
            final ServiceInjector serviceInjector) {
        this.specificationLoader = specificationLoader;
        this.objectManager = objectManager;
        this.bookmarkService = bookmarkService;
        this.serviceInjector = serviceInjector;
    }

    // //////////////////////////////////////

    File appendSheet(final List<WorksheetContent> worksheetContents, XSSFWorkbook workbook) throws IOException {
        final Set<String> worksheetNames = worksheetContents.stream()
                .map(x -> x.getSpec().getSheetName())
                .collect(Collectors.toSet());
        if(worksheetNames.size() < worksheetContents.size()) {
            throw new IllegalArgumentException("Sheet names must have distinct names");
        }
        for (final String worksheetName : worksheetNames) {
            if(worksheetName.length() > 30) {
                throw new IllegalArgumentException(
                        String.format("Sheet name cannot exceed 30 characters (invalid name: '%s')",
                                worksheetName));
            }
        }

        final File tempFile =
                File.createTempFile(ExcelConverter.class.getName(), UUID.randomUUID().toString() + XLSX_SUFFIX);
        try(final FileOutputStream fos = new FileOutputStream(tempFile)) {

            for (WorksheetContent worksheetContent : worksheetContents) {
                final WorksheetSpec spec = worksheetContent.getSpec();
                appendSheet(workbook, worksheetContent.getDomainObjects(), spec.getFactory(), spec.getSheetName());
            }
            workbook.write(fos);
        }
        return tempFile;
    }

    private Sheet appendSheet(
            final XSSFWorkbook workbook,
            final List<?> domainObjects,
            final WorksheetSpec.RowFactory<?> factory,
            final String sheetName) throws IOException {

        final ObjectSpecification objectSpec = specificationLoader.loadSpecification(factory.getCls());

        final List<ManagedObject> adapters = domainObjects.stream().map(objectManager::adapt).collect(Collectors.toList());

        final List<ObjectAssociation> propertyList = objectSpec.streamAssociations(Contributed.INCLUDED)
                                                        .filter(VISIBLE_PROPERTIES)
                                                        .collect(Collectors.toList());

        List<ObjectAssociation> annotatedAsHyperlink = new ArrayList<>();
        for (Field f : fieldsAnnotatedWith(factory.getCls(), HyperLink.class)){
            propertyList.stream()
                    .filter(oa -> Objects.equals(oa.getId(), f.getName()))
                    .forEach(annotatedAsHyperlink::add);
        }

        final Sheet sheet = ((Workbook) workbook).createSheet(sheetName);

        final RowFactory rowFactory = new RowFactory(sheet);
        final Row headerRow = rowFactory.newRow();


        // header row
        int i = 0;
        for (final ObjectAssociation property : propertyList) {
            final Cell cell = headerRow.createCell(i++);
            cell.setCellValue(property.getName());
        }

        final CellMarshaller cellMarshaller = newCellMarshaller(workbook);

        // detail rows
        for (final ManagedObject objectAdapter : adapters) {
            final Row detailRow = rowFactory.newRow();
            i = 0;
            for (final ObjectAssociation oa : propertyList) {
                final Cell cell = detailRow.createCell(i++);
                final OneToOneAssociation otoa = (OneToOneAssociation) oa;
                if (annotatedAsHyperlink.contains(oa)){
                    cellMarshaller.setCellValueForHyperlink(objectAdapter, otoa, cell);
                } else {
                    cellMarshaller.setCellValue(objectAdapter, otoa, cell);
                }
            }
        }

        // freeze panes
        sheet.createFreezePane(0, 1);

        return sheet;
    }

    File appendPivotSheet(final List<WorksheetContent> worksheetContents) throws IOException {
        
        
        val worksheetNames = _NullSafe.stream(worksheetContents)
        .map(worksheetContent->worksheetContent==null
                ? null
                : worksheetContent.getSpec().getSheetName())
        .filter(_Strings::isNotEmpty)
        .collect(_Sets.toUnmodifiableSorted());
        
        if(worksheetNames.size() < worksheetContents.size()) {
            throw new IllegalArgumentException("Sheet names must have distinct names and cannot be empty");
        }
        
        for (val worksheetName : worksheetNames) {
            if(worksheetName.length() > 30) {
                throw new IllegalArgumentException(
                        String.format("Sheet name cannot exceed 30 characters (invalid name: '%s')",
                                worksheetName));
            }
        }

        try(final XSSFWorkbook workbook = new XSSFWorkbook()) {
            final File tempFile =
                    File.createTempFile(ExcelConverter.class.getName(), UUID.randomUUID().toString() + XLSX_SUFFIX);
            try(final FileOutputStream fos = new FileOutputStream(tempFile)) {
    
                for (WorksheetContent worksheetContent : worksheetContents) {
                    final WorksheetSpec spec = worksheetContent.getSpec();
                    appendPivotSheet(workbook, worksheetContent.getDomainObjects(), spec.getFactory(), spec.getSheetName());
                }
                workbook.write(fos);
            }
            return tempFile;
        }
    }

    private void appendPivotSheet(
            final XSSFWorkbook workbook,
            final List<?> domainObjects,
            final WorksheetSpec.RowFactory<?> factory,
            final String sheetName) throws IOException {

        final ObjectSpecification objectSpec = specificationLoader.loadSpecification(factory.getCls());

        final List<ObjectAssociation> propertyList = objectSpec.streamAssociations(Contributed.INCLUDED)
                .filter(VISIBLE_PROPERTIES)
                .collect(Collectors.toList());

        // Validate the annotations for pivot
        validateAnnotations(propertyList, factory.getCls());

        // Proces the annotations for pivot
        final List<String> annotationList = new ArrayList<>();
        final List<Integer> orderList = new ArrayList<>();
        final List<AggregationType> typeList = new ArrayList<>();
        for (AnnotationOrderAndType annotationOrderAndType : getAnnotationAndOrderFrom(propertyList, factory.getCls())){
            annotationList.add(annotationOrderAndType.annotation);
            orderList.add(annotationOrderAndType.order);
            typeList.add(annotationOrderAndType.type);
        }

        // create pivot sheet
        final Sheet pivotSheet = ((Workbook) workbook).createSheet(sheetName);

        // Create source sheet for pivot
        String pivotSourceSheetName = ("source for ".concat(sheetName));
        if (WorksheetSpec.isTooLong(pivotSourceSheetName)) {
            pivotSourceSheetName = WorksheetSpec.trim(pivotSourceSheetName);
        }
        final Sheet pivotSourceSheet = appendSheet(workbook, domainObjects, factory, pivotSourceSheetName);
        pivotSourceSheet.shiftRows(0, pivotSourceSheet.getLastRowNum(), 3);
        final Row annotationRow = pivotSourceSheet.createRow(0);
        final Row orderRow = pivotSourceSheet.createRow(1);
        final Row typeRow = pivotSourceSheet.createRow(2);
        PivotUtils.createAnnotationRow(annotationRow, annotationList);
        PivotUtils.createOrderRow(orderRow, orderList);
        PivotUtils.createTypeRow(typeRow, typeList);

        // And finally: fill the pivot sheet with a pivot of the values found in pivot source sheet
        SheetPivoter p = new SheetPivoter();
        p.pivot(pivotSourceSheet, pivotSheet);
        pivotSourceSheet.removeRow(annotationRow);
        pivotSourceSheet.removeRow(orderRow);
        pivotSourceSheet.removeRow(typeRow);
        pivotSourceSheet.shiftRows(3, pivotSourceSheet.getLastRowNum(), -3);
    }

    private void validateAnnotations(final List<? extends ObjectAssociation> list, Class<?> cls) throws IllegalArgumentException{

        if (fieldsAnnotatedWith(cls, PivotRow.class).size()==0){
            throw new IllegalArgumentException("No annotation for row found");
        }
        if (fieldsAnnotatedWith(cls, PivotRow.class).size()>1){
            throw new IllegalArgumentException("Only one annotation for row allowed");
        }
        if (fieldsAnnotatedWith(cls, PivotColumn.class).size()==0){
            throw new IllegalArgumentException("No annotation for column found");
        }
        if (fieldsAnnotatedWith(cls, PivotValue.class).size()==0){
            throw new IllegalArgumentException("No annotation for value found");
        }

    }

    private List<AnnotationOrderAndType> getAnnotationAndOrderFrom(final List<? extends ObjectAssociation> list, final Class<?> cls){

        List<AnnotationOrderAndType> results = new ArrayList<>();
        for (ObjectAssociation oa : list){
            AnnotationOrderAndType resultToAdd = null;
            if (fieldsAnnotatedWith(cls, PivotRow.class).get(0).getName().equals(oa.getId())){
                resultToAdd = new AnnotationOrderAndType("row", 0, null);
            }
            for (Field f : fieldsAnnotatedWith(cls, PivotColumn.class)){
                if (f.getName().equals(oa.getId())){
                    resultToAdd = new AnnotationOrderAndType("column", f.getAnnotation(PivotColumn.class).order(), null);
                }
            }
            for (Field f : fieldsAnnotatedWith(cls, PivotValue.class)){
                if (f.getName().equals(oa.getId())){
                    resultToAdd = new AnnotationOrderAndType("value", f.getAnnotation(PivotValue.class).order(), f.getAnnotation(PivotValue.class).type());
                }
            }
            for (Field f : fieldsAnnotatedWith(cls, PivotDecoration.class)){
                if (f.getName().equals(oa.getId())){
                    resultToAdd = new AnnotationOrderAndType("deco", f.getAnnotation(PivotDecoration.class).order(), null);
                }
            }
            if (resultToAdd==null){
                resultToAdd = new AnnotationOrderAndType("skip", 0, null);
            }
            results.add(resultToAdd);
        }
        return results;
    }

    private List<Field> fieldsAnnotatedWith(final Class<?> cls, final Class<? extends Annotation> annotationCls){
        List<Field> result = new ArrayList<>();
        for (Field f : cls.getDeclaredFields()){
            if (f.isAnnotationPresent(annotationCls)) {
                result.add(f);
            }
        }
        return result;
    }

    private class AnnotationOrderAndType {

        AnnotationOrderAndType(final String annotation, final Integer order, final AggregationType type){
            this.annotation = annotation;
            this.order = order;
            this.type = type;
        }

        String annotation;
        Integer order;
        AggregationType type;

    }

    List<List<?>> fromBytes(
            final List<WorksheetSpec> worksheetSpecs,
            final byte[] bs) throws IOException, InvalidFormatException {

        final List<List<?>> listOfLists = _Lists.newArrayList();
        for (WorksheetSpec worksheetSpec : worksheetSpecs) {
            listOfLists.add(fromBytes(bs, worksheetSpec));
        }
        return listOfLists;
    }

    <T> List<T> fromBytes(
            final byte[] bs,
            final WorksheetSpec worksheetSpec) throws IOException, InvalidFormatException {

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bs)) {
            final Workbook wb = org.apache.poi.ss.usermodel.WorkbookFactory.create(bais);
            return fromWorkbook(wb, worksheetSpec);
        }
    }

    private <T> List<T> fromWorkbook(
            final Workbook workbook,
            final WorksheetSpec worksheetSpec) {

        final WorksheetSpec.RowFactory<Object> factory = worksheetSpec.getFactory();
        this.serviceInjector.injectServicesInto(factory);

        final Class<T> cls = _Casts.uncheckedCast(factory.getCls());
        final String sheetName = worksheetSpec.getSheetName();
        final Mode mode = worksheetSpec.getMode();

        final List<T> importedItems = _Lists.newArrayList();

        final CellMarshaller cellMarshaller = this.newCellMarshaller(workbook);

        final Sheet sheet = lookupSheet(cls, sheetName, workbook);

        boolean header = true;
        final Map<Integer, Property> propertyByColumn = _Maps.newHashMap();

        final ObjectSpecification objectSpec = specificationLoader.loadSpecification(cls);

        T previousRow = null;
        for (final Row row : sheet) {
            if (header) {
                for (final Cell cell : row) {

                    try{
                        if (cell.getCellType() != CellType.BLANK) {
                            final int columnIndex = cell.getColumnIndex();
                            final String propertyName = cellMarshaller.getStringCellValue(cell);
                            final OneToOneAssociation property = getAssociation(objectSpec, propertyName);
                            if (property != null) {
                                final Class<?> propertyType = property.getSpecification().getCorrespondingClass();
                                propertyByColumn.put(columnIndex, new Property(propertyName, property, propertyType));
                            }
                        }

                    } catch (final Exception e) {
                        switch (mode) {
                        case RELAXED:
                            // ignore
                        default:
                            throw new ExcelService.Exception(String.format("Error processing Excel row nr. %d. Message: %s", row.getRowNum(), e.getMessage()), e);
                        }
                    }

                }
                header = false;
            } else {
                // detail

                // Let's require at least one column to be not null for detecting a blank row.
                // Excel can have physical rows with cells empty that it seem do not existent for the user.
                ManagedObject templateAdapter = null;
                T imported = null;
                for (final Cell cell : row) {

                    try {

                        final int columnIndex = cell.getColumnIndex();
                        final Property property = propertyByColumn.get(columnIndex);
                        if (property != null) {
                            final OneToOneAssociation otoa = property.getOneToOneAssociation();
                            final Object value = cellMarshaller.getCellValue(cell, otoa);
                            if (value != null) {
                                if (imported == null) {
                                    // copy the row into a new object
                                    imported = _Casts.uncheckedCast(factory.create());
                                    // set excel metadata if applicable
                                    if (ExcelMetaDataEnabled.class.isAssignableFrom(cls)){
                                        ExcelMetaDataEnabled importedEnhanced = (ExcelMetaDataEnabled) imported;
                                        importedEnhanced.setExcelRowNumber(row.getRowNum());
                                        importedEnhanced.setExcelSheetName(sheetName);
                                        imported = _Casts.uncheckedCast(importedEnhanced);
                                    }
                                    templateAdapter = this.objectManager.adapt(imported);
                                }
                                final ManagedObject valueAdapter = this.objectManager.adapt(value);
                                otoa.set(templateAdapter, valueAdapter, InteractionInitiatedBy.USER);
                            }
                        } else {
                            // not expected; just ignore.
                        }

                    } catch (final Exception e) {
                        switch (mode) {
                        case RELAXED:
                            // ignore
                            break;
                        default:
                            throw new ExcelService.Exception(String.format("Error processing Excel row nr. %d. Message: %s", row.getRowNum(), e.getMessage()), e);

                        }
                    }
                }

                //
                // TODO: v2: to review... there is no longer an API to remove adapters.
                //  However, my hope is that it isn't needed, because we no longer maintain an oid <-> adapter map.
                //
//                // we need to remove the templateAdapter because earlier on we will have created an adapter (and corresponding OID)
//                // for a view model where the OID is initially computed on the incomplete (in fact, empty) view model.
//                // removing the adapter therefore removes the OID as well, so next time an adapter is needed for the view model
//                // the OID will be recomputed based on the fully populated view model pojo.
//                if(templateAdapter != null) {
//                    this.objectManager.removeAdapter(templateAdapter);
//                }

                if (imported != null) {
                    importedItems.add(imported);

                    if(imported instanceof RowHandler) {
                        val rowHandler = (RowHandler<?>) imported;
                        val rowHandlerPrev = (RowHandler<?>) previousRow;
                        
                        rowHandler.handleRow(_Casts.uncheckedCast(rowHandlerPrev));
                    }

                    previousRow = imported;
                }

            }



        }
        return importedItems;
    }

    protected <T> Sheet lookupSheet(final Class<T> cls, final String sheetName, final Workbook workbook) {
        final List<String> sheetNames = determineCandidateSheetNames(sheetName, cls);
        return lookupSheet(workbook, sheetNames);
    }

    private static <T> List<String> determineCandidateSheetNames(final String sheetName, final Class<T> cls) {
        final List<String> names = _Lists.newArrayList();
        if(sheetName != null) {
            names.add(sheetName);
        }
        final String simpleName = cls.getSimpleName();
        if(WorksheetSpec.hasSuffix(simpleName)) {
            names.add(WorksheetSpec.prefix(simpleName));
        }
        return names;
    }

    protected Sheet lookupSheet(
            final Workbook wb,
            final List<String> sheetNames) {
        for (String sheetName : sheetNames) {
            final Sheet sheet = wb.getSheet(sheetName);
            if(sheet != null) {
                return sheet;
            }
        }
        throw new IllegalArgumentException(String.format("Could not locate sheet named any of: '%s'", sheetNames));
    }

    private static OneToOneAssociation getAssociation(final ObjectSpecification objectSpec, final String propertyNameOrId) {
        final Stream<ObjectAssociation> associations = objectSpec.streamAssociations(Contributed.INCLUDED);
        return associations
                .filter(OneToOneAssociation.class::isInstance)
                .map(OneToOneAssociation.class::cast)
                .filter(association -> propertyNameOrId.equalsIgnoreCase(association.getName())
                                    || propertyNameOrId.equalsIgnoreCase(association.getId()))
                .findFirst()
                .orElse(null);
    }

    @ToString(of = {"name", "type", "currentValue"})
    static class Property {
        private final String name;
        private final Class<?> type;
        private final OneToOneAssociation property;
        private Object currentValue;

        public Property(final String name, final OneToOneAssociation property, final Class<?> type) {
            this.name = name;
            this.property = property;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public OneToOneAssociation getOneToOneAssociation() {
            return property;
        }

        public Class<?> getType() {
            return type;
        }

        public Object getCurrentValue() {
            return currentValue;
        }

        public void setCurrentValue(final Object currentValue) {
            this.currentValue = currentValue;
        }
    }

//    @SuppressWarnings("unused")
//    private void autoSize(final Sheet sh, final int numProps) {
//        for (int prop = 0; prop < numProps; prop++) {
//            sh.autoSizeColumn(prop);
//        }
//    }

    // //////////////////////////////////////

    protected CellMarshaller newCellMarshaller(final Workbook wb) {
        final CellStyle dateCellStyle = createDateFormatCellStyle(wb);
        final CellStyle defaultCellStyle = defaultCellStyle(wb);
        final CellMarshaller cellMarshaller = new CellMarshaller(bookmarkService, dateCellStyle, defaultCellStyle);
        return cellMarshaller;
    }

    protected CellStyle createDateFormatCellStyle(final Workbook wb) {
        final CreationHelper createHelper = wb.getCreationHelper();
        final short dateFormat = createHelper.createDataFormat().getFormat("yyyy-mm-dd");
        final CellStyle dateCellStyle = wb.createCellStyle();
        dateCellStyle.setDataFormat(dateFormat);
        dateCellStyle.setVerticalAlignment(VerticalAlignment.TOP);
        return dateCellStyle;
    }

    protected CellStyle defaultCellStyle(final Workbook wb) {
        final CellStyle defaultCellStyle = wb.createCellStyle();
        defaultCellStyle.setVerticalAlignment(VerticalAlignment.TOP);
        return defaultCellStyle;
    }

}
