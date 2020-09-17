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
package org.apache.isis.subdomains.excel.applib.dom;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.RecoverableException;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.subdomains.excel.applib.dom.util.ExcelServiceImpl;
import org.apache.isis.subdomains.excel.applib.dom.util.Mode;

@Service
@Named("isisSubExcel.ExcelService")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
public class ExcelService {

    public static class Exception extends RecoverableException {

        private static final long serialVersionUID = 1L;

        public Exception(final String msg, final Throwable ex) {
            super(msg, ex);
        }

        public Exception(final Throwable ex) {
            super(ex);
        }
    }

    public static final String XSLX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private ExcelServiceImpl excelServiceImpl;

    public ExcelService() {
    }

    @PostConstruct
    public void init() {
        excelServiceImpl = new ExcelServiceImpl();
        serviceInjector.injectServicesInto(excelServiceImpl);
    }

    // //////////////////////////////////////

    /**
     * Creates a Blob holding a spreadsheet of the domain objects.
     *
     * <p>
     *     There are no specific restrictions on the domain objects; they can be either persistable entities or
     *     view models.  Do be aware though that if imported back using {@link #fromExcel(Blob, Class, String)},
     *     then new instances are always created.  It is generally better therefore to work with view models than to
     *     work with entities.  This also makes it easier to maintain backward compatibility in the future if the
     *     persistence model changes; using view models represents a stable API for import/export.
     * </p>
     *
     * @param sheetName - must be 31 chars or less
     */
    public <T> Blob toExcel(
            final List<T> domainObjects,
            final Class<T> cls,
            final String sheetName,
            final String fileName) throws ExcelService.Exception {
        return excelServiceImpl.toExcel(domainObjects, cls, sheetName, fileName);
    }
    
    public <T> Blob toExcel(
            final List<T> domainObjects,
            final Class<T> cls,
            final String sheetName,
            final String fileName,
            final InputStream in) throws ExcelService.Exception {
        return excelServiceImpl.toExcel(domainObjects, cls, sheetName, fileName, in);
    }

    public <T> Blob toExcel(
            final WorksheetContent worksheetContent,
            final String fileName) throws ExcelService.Exception {
        return excelServiceImpl.toExcel(worksheetContent, fileName);
    }
    
    public <T> Blob toExcel(
            final WorksheetContent worksheetContent,
            final String fileName,
            final InputStream in) throws ExcelService.Exception {
        return excelServiceImpl.toExcel(worksheetContent, fileName, in);
    }

    public Blob toExcel(
            final List<WorksheetContent> worksheetContents,
            final String fileName) throws ExcelService.Exception {
        return excelServiceImpl.toExcel(worksheetContents, fileName);
    }
    
    public Blob toExcel(
            final List<WorksheetContent> worksheetContents,
            final String fileName,
            final InputStream in) throws ExcelService.Exception {
        return excelServiceImpl.toExcel(worksheetContents, fileName, in);
    }    

    public <T> Blob toExcelPivot(
            final List<T> domainObjects,
            final Class<T> cls,
            final String fileName) throws ExcelService.Exception {
        return excelServiceImpl.toExcelPivot(domainObjects, cls, fileName);
    }

    public <T> Blob toExcelPivot(
            final List<T> domainObjects,
            final Class<T> cls,
            final String sheetName,
            final String fileName) throws ExcelService.Exception {
        return excelServiceImpl.toExcelPivot(domainObjects, cls, sheetName, fileName);
    }

    public <T> Blob toExcelPivot(
            final WorksheetContent worksheetContent,
            final String fileName) throws ExcelService.Exception {
        return excelServiceImpl.toExcelPivot(worksheetContent, fileName);
    }

    public Blob toExcelPivot(
            final List<WorksheetContent> worksheetContents,
            final String fileName) throws ExcelService.Exception {

        return excelServiceImpl.toExcelPivot(worksheetContents, fileName);
    }

    /**
     * Returns a list of objects for each line in the spreadsheet, of the specified type.
     *
     * <p>
     *     If the class is a view model then the objects will be properly instantiated, with the correct
     *     view model memento); otherwise the objects will be simple transient objects.
     * </p>
     */
    public <T> List<T> fromExcel(
            final Blob excelBlob,
            final Class<T> cls,
            final String sheetName) throws ExcelService.Exception {
        return fromExcel(excelBlob, new WorksheetSpec(cls, sheetName));
    }

    public <T> List<T> fromExcel(
            final Blob excelBlob,
            final Class<T> cls,
            final String sheetName,
            final Mode mode) throws ExcelService.Exception {
        return fromExcel(excelBlob, new WorksheetSpec(cls, sheetName, mode));
    }

    public <T> List<T> fromExcel(
            final Blob excelBlob,
            final WorksheetSpec worksheetSpec) throws ExcelService.Exception {
        return excelServiceImpl.fromExcel(excelBlob, worksheetSpec);
    }

    public List<List<?>> fromExcel(
            final Blob excelBlob,
            final List<WorksheetSpec> worksheetSpecs) throws ExcelService.Exception {
        return excelServiceImpl.fromExcel(excelBlob, worksheetSpecs);
    }

    public List<List<?>> fromExcel(
            final Blob excelBlob,
            final WorksheetSpec.Matcher matcher) throws ExcelService.Exception {

        return fromExcel(excelBlob, matcher, null);
    }

    public List<List<?>> fromExcel(
            final Blob excelBlob,
            final WorksheetSpec.Matcher matcher,
            final WorksheetSpec.Sequencer sequencer) throws ExcelService.Exception {

        List<WorksheetSpec> worksheetSpecs = _Lists.newArrayList();
        try (ByteArrayInputStream bais = new ByteArrayInputStream(excelBlob.getBytes())) {
            try (final Workbook wb = org.apache.poi.ss.usermodel.WorkbookFactory.create(bais)) {
                final int numberOfSheets = wb.getNumberOfSheets();
                for (int i = 0; i < numberOfSheets; i++) {
                    final Sheet sheet = wb.getSheetAt(i);
                    WorksheetSpec worksheetSpec = matcher.fromSheet(sheet.getSheetName());
                    if(worksheetSpec != null) {
                        worksheetSpecs.add(worksheetSpec);
                    }
                }
            }
        } catch (IOException e) {
            throw new ExcelService.Exception(e);
        }

        if(sequencer != null) {
            worksheetSpecs = sequencer.sequence(worksheetSpecs);
        }

        return fromExcel(excelBlob, worksheetSpecs);
    }

    @Inject private ServiceInjector serviceInjector;

}
