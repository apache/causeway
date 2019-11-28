package org.apache.isis.extensions.excel.integtests.tests;

import java.net.URL;
import java.util.List;

import javax.inject.Inject;

import com.google.common.io.Resources;


import org.apache.isis.applib.value.Blob;

import org.assertj.core.api.Assertions;
import org.apache.isis.extensions.excel.dom.util.ExcelFileBlobConverter;
import org.apache.isis.extensions.excel.integtests.ExcelModuleIntegTestAbstract;
import org.apache.isis.extensions.excel.fixtures.demoapp.demomodule.fixturehandlers.excelupload.ExcelUploadServiceForDemoToDoItem;
import org.apache.isis.extensions.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItem;
import org.apache.isis.extensions.excel.fixtures.demoapp.todomodule.dom.ExcelDemoToDoItemMenu;
import org.apache.isis.extensions.excel.fixtures.demoapp.todomodule.fixturescripts.ExcelDemoToDoItem_tearDown;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.core.Is.is;

public class ExcelModuleDemoUploadService_IntegTest extends ExcelModuleIntegTestAbstract {

    @Inject
    private ExcelUploadServiceForDemoToDoItem uploadService;

    @BeforeEach
    public void setUpData() throws Exception {
        fixtureScripts.run(new ExcelDemoToDoItem_tearDown());
    }

    @Test
    public void uploadSpreadsheet() throws Exception{

        // Given
        final URL excelResource = Resources.getResource(getClass(), "ToDoItemsWithMultipleSheets.xlsx");
        final Blob blob = new ExcelFileBlobConverter().toBlob("unused", excelResource);

        // When
        uploadService.uploadSpreadsheet(blob, null);

        // Then
        final List<ExcelDemoToDoItem> all = toDoItems.allInstances();

        Assertions.assertThat(all.size()).isEqualTo(8);
    }

    @Inject
    private ExcelDemoToDoItemMenu toDoItems;


}
