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
package org.apache.isis.subdomains.excel.integtests.tests;

import java.net.URL;
import java.util.List;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.commons.internal.resources._Resources;
import org.apache.isis.subdomains.excel.applib.dom.util.ExcelFileBlobConverter;
import org.apache.isis.subdomains.excel.fixtures.demoapp.demomodule.fixturehandlers.excelupload.ExcelUploadServiceForDemoToDoItem;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItem;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItemMenu;
import org.apache.isis.subdomains.excel.fixtures.demoapp.todomodule.fixturescripts.ExcelDemoToDoItem_tearDown;
import org.apache.isis.subdomains.excel.integtests.ExcelModuleIntegTestAbstract;

public class ExcelModuleDemoUploadService_IntegTest extends ExcelModuleIntegTestAbstract {

    @Inject private ExcelUploadServiceForDemoToDoItem uploadService;
    @Inject private ExcelDemoToDoItemMenu toDoItems;

    @BeforeEach
    public void setUpData() throws Exception {
        fixtureScripts.run(new ExcelDemoToDoItem_tearDown());
    }

    //@Test @Disabled("TODO[2033] removal of PlatformTransactionManager") 
    public void uploadSpreadsheet() throws Exception{

        // Given
        final URL excelResource = _Resources.getResourceUrl(getClass(), "ToDoItemsWithMultipleSheets.xlsx");
        final Blob blob = new ExcelFileBlobConverter().toBlob("unused", excelResource);

        // When
        uploadService.uploadSpreadsheet(blob, null);

        // Then
        final List<ExcelDemoToDoItem> all = toDoItems.allInstances();

        Assertions.assertThat(all.size()).isEqualTo(8);
    }


}
