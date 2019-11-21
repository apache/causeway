package org.isisaddons.module.excel.fixture.demoapp.todomodule.dom;

import java.util.Comparator;

import com.google.common.collect.Ordering;

public class DependenciesComparator implements Comparator<ExcelDemoToDoItem> {
    @Override
    public int compare(final ExcelDemoToDoItem p, final ExcelDemoToDoItem q) {
        final Ordering<ExcelDemoToDoItem> byDescription = new Ordering<ExcelDemoToDoItem>() {
            public int compare(final ExcelDemoToDoItem p, final ExcelDemoToDoItem q) {
                return Ordering.natural().nullsFirst().onResultOf(ExcelDemoToDoItem::getDescription)
                        .compare(p, q);
            }
        };
        return byDescription
                .compound(Ordering.<ExcelDemoToDoItem>natural())
                .compare(p, q);
    }
}
