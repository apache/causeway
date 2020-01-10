package org.apache.isis.subdomains.excel.testing;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import com.google.common.io.Resources;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScript;
import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScripts;
import org.apache.isis.subdomains.excel.applib.dom.ExcelService;
import org.apache.isis.subdomains.excel.applib.dom.WorksheetSpec;

import lombok.Getter;
import lombok.Setter;

/**
 * This class should be executed using {@link FixtureScripts.MultipleExecutionStrategy#EXECUTE_ONCE_BY_VALUE} (it
 * has value semantics).
 */
@DomainObject(
        objectType = "isisexcel.ExcelFixture2"
)
public class ExcelFixture2 extends FixtureScript {


    /**
     * Input, optional: defines the name of the resource.
     */
    @Getter @Setter
    @MemberOrder(sequence = "1.1")
    private String excelResourceName;

    /**
     * Input, either this or the blob is mandatory ... the Excel spreadsheet to read.
     */
    @Getter @Setter
    @MemberOrder(sequence = "1.2")
    private URL excelResource;

    /**
     * Input, either this or the excelResource is mandatory ... the Excel spreadsheet to read.
     */
    @Getter @Setter
    private Blob blob;

    /**
     * Input, mandatory ... how to process each sheet of the workbook.
     */
    @Getter @Setter
    WorksheetSpec.Matcher matcher;

    /**
     * Input, optional ... which sequence to process the matched sheets
     */
    @Getter @Setter
    private WorksheetSpec.Sequencer sequencer;


    /**
     * Output... a list of list of objects (each representing a row of a sheet)
     */
    @Getter
    private List<List<?>> lists;


    @Override
    protected void execute(final ExecutionContext executionContext) {

        if (blob == null){
            byte[] bytes = getBytes();
            blob = new Blob("unused", ExcelService.XSLX_MIME_TYPE, bytes);
        }

        this.lists = excelService.fromExcel(blob, matcher, sequencer);

    }

    //region > bytes
    private byte[] bytes;

    private byte[] getBytes() {
        if (bytes == null) {
            if (blob != null){
                bytes = blob.getBytes();
            } else {
                bytes = readBytes();
            }
        }
        return bytes;
    }

    private byte[] readBytes() {

        final URL excelResource = getExcelResource();
        try {
            bytes = Resources.toByteArray(excelResource);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read from resource: " + excelResource);
        }
        return bytes;
    }
    //endregion


    protected <T> WorksheetSpec.RowFactory<T> rowFactoryFor(
            final Class<T> rowClass,
            final ExecutionContext ec) {

        return new WorksheetSpec.RowFactory<T>() {
            @Override
            public T create() {
                final T importLine =
                        factoryService.instantiate(rowClass);
                // allow the line to interact with the calling fixture
                if(importLine instanceof FixtureAwareRowHandler<?>) {
                    final FixtureAwareRowHandler<?> farh = (FixtureAwareRowHandler<?>) importLine;
                    farh.setExecutionContext(ec);
                    farh.setExcelFixture2(ExcelFixture2.this);
                }
                return importLine;
            }

            @Override
            public Class<?> getCls() {
                return rowClass;
            }
        };
    }
    //region > hashCode, equals

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final ExcelFixture2 that = (ExcelFixture2) o;

        return Arrays.equals(getBytes(), that.getBytes());

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getBytes());
    }

    //endregion

    @javax.inject.Inject
    private RepositoryService repositoryService;
    @javax.inject.Inject
    private BookmarkService bookmarkService;
    @javax.inject.Inject
    private ServiceRegistry serviceRegistry;

    @Inject
    FactoryService factoryService;

    @Inject
    ExcelService excelService;

}
