package org.apache.isis.extensions.excel.dom;

public interface RowHandler<T extends RowHandler<T>> {

    void handleRow(T previousRow);

}
