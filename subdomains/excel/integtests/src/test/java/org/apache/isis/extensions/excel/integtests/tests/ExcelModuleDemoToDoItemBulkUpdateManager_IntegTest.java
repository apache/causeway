package org.apache.isis.extensions.excel.integtests.tests;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import com.google.common.io.ByteSource;
import com.google.common.io.Resources;

import org.assertj.core.api.Assertions;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.apache.isis.applib.value.Blob;
import org.apache.isis.extensions.excel.dom.ExcelService;
import org.apache.isis.extensions.excel.fixtures.demoapp.demomodule.dom.bulkupdate.BulkUpdateLineItemForDemoToDoItem;
import org.apache.isis.extensions.excel.fixtures.demoapp.demomodule.dom.bulkupdate.BulkUpdateManagerForDemoToDoItem;
import org.apache.isis.extensions.excel.fixtures.demoapp.demomodule.dom.bulkupdate.BulkUpdateMenuForDemoToDoItem;
import org.apache.isis.extensions.excel.fixtures.demoapp.demomodule.fixturescripts.DemoToDoItem_recreate_usingExcelFixture;
import org.apache.isis.extensions.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItemMenu;
import org.apache.isis.extensions.excel.integtests.ExcelModuleIntegTestAbstract;

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
    @Test
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
        Assertions.assertThat(lineItems.size()).isEqualTo(2);

        final BulkUpdateLineItemForDemoToDoItem lineItem1 = lineItems.get(0);
        final BulkUpdateLineItemForDemoToDoItem lineItem2 = lineItems.get(1);

        Assertions.assertThat(lineItem1.getDescription()).isEqualTo("Buy milk - updated!");
        Assertions.assertThat(lineItem2.getNotes()).isEqualTo("Get sliced brown if possible.");
    }

    private static byte[] getBytes(final Class<?> contextClass, final String name) throws IOException {
        final ByteSource byteSource = Resources.asByteSource(contextClass.getResource(name));
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byteSource.copyTo(baos);
        return baos.toByteArray();
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