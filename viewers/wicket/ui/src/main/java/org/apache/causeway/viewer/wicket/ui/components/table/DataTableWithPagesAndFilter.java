package org.apache.causeway.viewer.wicket.ui.components.table;

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

public abstract class DataTableWithPagesAndFilter<T, S> extends DataTable<T, S> {
    private static final long serialVersionUID = 1L;

    public DataTableWithPagesAndFilter(final String id, final List<? extends IColumn<T, S>> columns,
        final ISortableDataProvider<T, S> dataProvider, final int rowsPerPage) {
        super(id, columns, dataProvider, rowsPerPage);
        setOutputMarkupId(true);
        setVersioned(false);
        addToolBars(dataProvider);
    }

    /**
     * Factory method for toolbars
     * @param dataProvider {@link org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider}
     */
    protected void addToolBars(final ISortableDataProvider<T, S> dataProvider) {
        addTopToolbar(new AjaxNavigationToolbar(this));
        addTopToolbar(new AjaxFallbackHeadersToolbar<>(this, dataProvider));
        addBottomToolbar(new NoRecordsToolbar(this));
    }

    @Override
    protected Item<T> newRowItem(final String id, final int index, final IModel<T> model) {
        return new OddEvenItem<>(id, index, model);
    }

}
