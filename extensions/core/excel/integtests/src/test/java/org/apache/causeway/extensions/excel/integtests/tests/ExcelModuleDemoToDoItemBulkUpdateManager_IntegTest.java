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
package org.apache.causeway.extensions.excel.integtests.tests;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.commons.internal.base._Bytes;
import org.apache.causeway.commons.internal.resources._Resources;
import org.apache.causeway.extensions.excel.applib.ExcelService;
import org.apache.causeway.extensions.excel.fixtures.demoapp.demomodule.dom.bulkupdate.BulkUpdateLineItemForDemoToDoItem;
import org.apache.causeway.extensions.excel.fixtures.demoapp.demomodule.dom.bulkupdate.BulkUpdateManagerForDemoToDoItem;
import org.apache.causeway.extensions.excel.fixtures.demoapp.demomodule.dom.bulkupdate.BulkUpdateMenuForDemoToDoItem;
import org.apache.causeway.extensions.excel.fixtures.demoapp.demomodule.fixturescripts.DemoToDoItem_recreate_usingExcelFixture;
import org.apache.causeway.extensions.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItemMenu;
import org.apache.causeway.extensions.excel.integtests.ExcelModuleIntegTestAbstract;

public class ExcelModuleDemoToDoItemBulkUpdateManager_IntegTest extends ExcelModuleIntegTestAbstract {

    @BeforeEach
    public void setUpData() throws Exception {
        fixtureScripts.run(new DemoToDoItem_recreate_usingExcelFixture());
    }

    @Inject
    private ExcelDemoToDoItemMenu toDoItems;

    @javax.inject.Inject
    private BulkUpdateMenuForDemoToDoItem exportImportService;

    private BulkUpdateManagerForDemoToDoItem bulkUpdateManager;

    @BeforeEach
    public void setUp() throws Exception {
        bulkUpdateManager = exportImportService.bulkUpdateManager();
    }

    /**
     * Can't do in two steps because the exported XLSX references the ToDoItem's OID which would change if reset db.
     * @throws Exception
     */
    @Disabled("TODO - reinstate")
    //@Test
    public void export_then_import() throws Exception {

        // given
        final byte[] expectedBytes = getBytes(getClass(), "toDoItems-expected.xlsx");

        // when
        final Blob exportedBlob = bulkUpdateManager.export();

        // then
        final byte[] actualBytes = exportedBlob.getBytes();
        // assertThat(actualBytes, lengthWithinPercentage(expectedBytes, 10));  /// ... too flaky


        // and given
        final byte[] updatedBytes = getBytes(getClass(), "toDoItems-updated.xlsx");

        // when
        final List<BulkUpdateLineItemForDemoToDoItem> lineItems =
                bulkUpdateManager.importBlob(new Blob("toDoItems-updated.xlsx", ExcelService.XSLX_MIME_TYPE, updatedBytes));

        // then
        assertThat(lineItems.size()).isEqualTo(2);

        final BulkUpdateLineItemForDemoToDoItem lineItem1 = lineItems.get(0);
        final BulkUpdateLineItemForDemoToDoItem lineItem2 = lineItems.get(1);

        assertThat(lineItem1.getDescription()).isEqualTo("Buy milk - updated!");
        assertThat(lineItem2.getNotes()).isEqualTo("Get sliced brown if possible.");
    }

    private static byte[] getBytes(final Class<?> contextClass, final String resourceName) throws IOException {
        return _Bytes.of(_Resources.load(contextClass, resourceName));
    }

    private static final Matcher<? super byte[]> lengthWithinPercentage(final byte[] expectedBytes, final int percentage) {
        return new TypeSafeMatcher<byte[]>() {
            @Override
            protected boolean matchesSafely(byte[] item) {
                final double lower = expectedBytes.length * (100 - percentage) / 100;
                final double upper = expectedBytes.length * (100 + percentage) / 100;
                final int actualLength = item.length;
                return actualLength > lower && actualLength < upper;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Byte array with length within " + percentage + "% of expected length (" + expectedBytes.length + " bytes)");
            }
        };
    }


}
