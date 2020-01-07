package org.apache.isis.extensions.xdocreport.dom.impl;

import java.util.List;
import java.util.Map;

public interface XDocReportModel {

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



