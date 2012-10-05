package org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable;

import java.util.List;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxNavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.IModel;

public class MyAjaxFallbackDefaultDataTable<T, S> extends DataTable<T, S>
{
    private static final long serialVersionUID = 1L;

    public MyAjaxFallbackDefaultDataTable(final String id, final List<? extends IColumn<T, S>> columns,
        final ISortableDataProvider<T, S> dataProvider, final int rowsPerPage)
    {
        super(id, columns, dataProvider, rowsPerPage);
        setOutputMarkupId(true);
        setVersioned(false);
        addTopToolbar(new AjaxFallbackHeadersToolbar<S>(this, dataProvider));
        addBottomToolbar(new AjaxNavigationToolbar(this));
        addBottomToolbar(new NoRecordsToolbar(this));
    }

    @Override
    protected Item<T> newRowItem(final String id, final int index, final IModel<T> model)
    {
        return new OddEvenItem<T>(id, index, model);
    }

}