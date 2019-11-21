package org.isisaddons.module.excel.dom;

public interface RowHandler<T extends RowHandler<T>> {

    void handleRow(T previousRow);

}
