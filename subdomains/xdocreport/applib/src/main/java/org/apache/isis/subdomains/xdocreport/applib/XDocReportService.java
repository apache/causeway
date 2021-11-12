package org.apache.isis.subdomains.xdocreport.applib;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @since 2.0 {@index}
 */
public interface XDocReportService {

    byte[] render(byte[] templateBytes, XDocReportModel dataModel, OutputType outputType) throws IOException;

    enum OutputType {
        DOCX,
        PDF
    }

    /**
     * @since 2.0 {@index}
     */
    interface XDocReportModel {

        @lombok.Data
        class Data {
            private final Object obj;
            private final Class<?> cls;
            private final boolean list;

            public static <T> Data list(final List<T> objects, final Class<T> cls) {
                return new Data(objects, cls, true);
            }

            public static <T> Data object(final T object) {
                return new Data(object, object.getClass(), false);
            }
        }
        Map<String, Data> getContextData();

    }

}