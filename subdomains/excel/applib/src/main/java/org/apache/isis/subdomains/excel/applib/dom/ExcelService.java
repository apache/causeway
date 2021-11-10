package org.apache.isis.subdomains.excel.applib.dom;

import java.io.InputStream;
import java.util.List;

import org.apache.isis.applib.exceptions.RecoverableException;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.subdomains.excel.applib.service.ExcelServiceDefault;
import org.apache.isis.subdomains.excel.applib.util.Mode;

/**
 * @since 2.0 {@index}
 */
public interface ExcelService {

    String XSLX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

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
    <T> Blob toExcel(List<T> domainObjects, Class<T> cls, String sheetName, String fileName)
            throws ExcelServiceDefault.Exception;

    <T> Blob toExcel(List<T> domainObjects, Class<T> cls, String sheetName, String fileName, InputStream in)
            throws ExcelServiceDefault.Exception;

    <T> Blob toExcel(WorksheetContent worksheetContent, String fileName) throws ExcelServiceDefault.Exception;

    <T> Blob toExcel(WorksheetContent worksheetContent, String fileName, InputStream in)
            throws ExcelServiceDefault.Exception;

    Blob toExcel(List<WorksheetContent> worksheetContents, String fileName) throws ExcelServiceDefault.Exception;

    Blob toExcel(List<WorksheetContent> worksheetContents, String fileName, InputStream in)
            throws ExcelServiceDefault.Exception;

    <T> Blob toExcelPivot(List<T> domainObjects, Class<T> cls, String fileName) throws ExcelServiceDefault.Exception;

    <T> Blob toExcelPivot(List<T> domainObjects, Class<T> cls, String sheetName, String fileName)
            throws ExcelServiceDefault.Exception;

    <T> Blob toExcelPivot(WorksheetContent worksheetContent, String fileName) throws ExcelServiceDefault.Exception;

    Blob toExcelPivot(List<WorksheetContent> worksheetContents, String fileName) throws ExcelServiceDefault.Exception;

    /**
     * Returns a list of objects for each line in the spreadsheet, of the specified type.
     *
     * <p>
     *     If the class is a view model then the objects will be properly instantiated, with the correct
     *     view model memento); otherwise the objects will be simple transient objects.
     * </p>
     */
    <T> List<T> fromExcel(Blob excelBlob, Class<T> cls, String sheetName) throws ExcelServiceDefault.Exception;

    <T> List<T> fromExcel(Blob excelBlob, Class<T> cls, String sheetName, Mode mode)
            throws ExcelServiceDefault.Exception;

    <T> List<T> fromExcel(Blob excelBlob, WorksheetSpec worksheetSpec) throws ExcelServiceDefault.Exception;

    List<List<?>> fromExcel(Blob excelBlob, List<WorksheetSpec> worksheetSpecs) throws ExcelServiceDefault.Exception;

    List<List<?>> fromExcel(Blob excelBlob, WorksheetSpec.Matcher matcher) throws ExcelServiceDefault.Exception;

    List<List<?>> fromExcel(Blob excelBlob, WorksheetSpec.Matcher matcher, WorksheetSpec.Sequencer sequencer)
            throws ExcelServiceDefault.Exception;

    public static class Exception extends RecoverableException {

        private static final long serialVersionUID = 1L;

        public Exception(final String msg, final Throwable ex) {
            super(msg, ex);
        }

        public Exception(final Throwable ex) {
            super(ex);
        }
    }

}