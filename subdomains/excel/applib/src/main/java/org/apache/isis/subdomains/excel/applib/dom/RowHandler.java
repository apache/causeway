package org.apache.isis.subdomains.excel.applib.dom;

public interface RowHandler<T extends RowHandler<T>> {

    void handleRow(T previousRow);

}
