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
package org.apache.causeway.extensions.excel.applib.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.extensions.excel.applib.CausewayModuleExtExcelApplib;
import org.apache.causeway.extensions.excel.applib.ExcelService;
import org.apache.causeway.extensions.excel.applib.Mode;
import org.apache.causeway.extensions.excel.applib.WorksheetContent;
import org.apache.causeway.extensions.excel.applib.WorksheetSpec;

/**
 * Implementation of {@link ExcelService}.
 *
 * @since 2.0 {@index}
 */
@Service
@Named(ExcelServiceDefault.LOGICAL_TYPE_NAME)
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class ExcelServiceDefault implements ExcelService {

    static final String LOGICAL_TYPE_NAME = CausewayModuleExtExcelApplib.NAMESPACE + ".ExcelServiceDefault";

    private _ExcelServiceHelper helper;

    @Inject private ServiceInjector serviceInjector;

    @PostConstruct
    public void init() {
        helper = new _ExcelServiceHelper();
        serviceInjector.injectServicesInto(helper);
    }

    @Override
    public <T> Blob toExcel(
            final List<T> domainObjects,
            final Class<T> cls,
            final String sheetName,
            final String fileName) throws ExcelServiceDefault.Exception {
        return helper.toExcel(domainObjects, cls, sheetName, fileName);
    }

    @Override
    public <T> Blob toExcel(
            final List<T> domainObjects,
            final Class<T> cls,
            final String sheetName,
            final String fileName,
            final InputStream in) throws ExcelServiceDefault.Exception {
        return helper.toExcel(domainObjects, cls, sheetName, fileName, in);
    }

    @Override
    public <T> Blob toExcel(
            final WorksheetContent worksheetContent,
            final String fileName) throws ExcelServiceDefault.Exception {
        return helper.toExcel(worksheetContent, fileName);
    }

    @Override
    public <T> Blob toExcel(
            final WorksheetContent worksheetContent,
            final String fileName,
            final InputStream in) throws ExcelServiceDefault.Exception {
        return helper.toExcel(worksheetContent, fileName, in);
    }

    @Override
    public Blob toExcel(
            final List<WorksheetContent> worksheetContents,
            final String fileName) throws ExcelServiceDefault.Exception {
        return helper.toExcel(worksheetContents, fileName);
    }

    @Override
    public Blob toExcel(
            final List<WorksheetContent> worksheetContents,
            final String fileName,
            final InputStream in) throws ExcelServiceDefault.Exception {
        return helper.toExcel(worksheetContents, fileName, in);
    }

    @Override
    public <T> Blob toExcelPivot(
            final List<T> domainObjects,
            final Class<T> cls,
            final String fileName) throws ExcelServiceDefault.Exception {
        return helper.toExcelPivot(domainObjects, cls, fileName);
    }

    @Override
    public <T> Blob toExcelPivot(
            final List<T> domainObjects,
            final Class<T> cls,
            final String sheetName,
            final String fileName) throws ExcelServiceDefault.Exception {
        return helper.toExcelPivot(domainObjects, cls, sheetName, fileName);
    }

    @Override
    public <T> Blob toExcelPivot(
            final WorksheetContent worksheetContent,
            final String fileName) throws ExcelServiceDefault.Exception {
        return helper.toExcelPivot(worksheetContent, fileName);
    }

    @Override
    public Blob toExcelPivot(
            final List<WorksheetContent> worksheetContents,
            final String fileName) throws ExcelServiceDefault.Exception {

        return helper.toExcelPivot(worksheetContents, fileName);
    }

    @Override
    public <T> List<T> fromExcel(
            final Blob excelBlob,
            final Class<T> cls,
            final String sheetName) throws ExcelServiceDefault.Exception {
        return fromExcel(excelBlob, new WorksheetSpec(cls, sheetName));
    }

    @Override
    public <T> List<T> fromExcel(
            final Blob excelBlob,
            final Class<T> cls,
            final String sheetName,
            final Mode mode) throws ExcelServiceDefault.Exception {
        return fromExcel(excelBlob, new WorksheetSpec(cls, sheetName, mode));
    }

    @Override
    public <T> List<T> fromExcel(
            final Blob excelBlob,
            final WorksheetSpec worksheetSpec) throws ExcelServiceDefault.Exception {
        return helper.fromExcel(excelBlob, worksheetSpec);
    }

    @Override
    public List<List<?>> fromExcel(
            final Blob excelBlob,
            final List<WorksheetSpec> worksheetSpecs) throws ExcelServiceDefault.Exception {
        return helper.fromExcel(excelBlob, worksheetSpecs);
    }

    @Override
    public List<List<?>> fromExcel(
            final Blob excelBlob,
            final WorksheetSpec.Matcher matcher) throws ExcelServiceDefault.Exception {

        return fromExcel(excelBlob, matcher, null);
    }

    @Override
    public List<List<?>> fromExcel(
            final Blob excelBlob,
            final WorksheetSpec.Matcher matcher,
            final WorksheetSpec.Sequencer sequencer) throws ExcelServiceDefault.Exception {

        List<WorksheetSpec> worksheetSpecs = _Lists.newArrayList();
        try (ByteArrayInputStream bais = new ByteArrayInputStream(excelBlob.bytes())) {
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
            throw new ExcelServiceDefault.Exception(e);
        }

        if(sequencer != null) {
            worksheetSpecs = sequencer.sequence(worksheetSpecs);
        }

        return fromExcel(excelBlob, worksheetSpecs);
    }

}
