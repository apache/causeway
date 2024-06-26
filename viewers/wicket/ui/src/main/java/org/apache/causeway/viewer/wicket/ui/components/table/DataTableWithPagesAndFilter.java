/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.viewer.wicket.ui.components.table;

import java.util.List;
import java.util.OptionalLong;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LambdaModel;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.primitives._Longs;
import org.apache.causeway.viewer.wicket.model.hints.UiHintContainer;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.model.pagesize.PagesizeChoice;
import org.apache.causeway.viewer.wicket.ui.components.table.internal._TableUtils;
import org.apache.causeway.viewer.wicket.ui.components.table.nav.pagesize.PagesizeChooser;

public abstract class DataTableWithPagesAndFilter<T, S> extends DataTable<T, S> {
    private static final long serialVersionUID = 1L;

    private static final String UIHINT_PAGE_NUMBER = "pageNumber";
    private static final String UIHINT_PAGE_SIZE = "pageSize";
    private static final String UIHINT_SEARCH_ARG = "searchArg";

    protected String searchArg;

    protected DataTableWithPagesAndFilter(
                    final String id,
                    final List<? extends IColumn<T, S>> columns,
                    final ISortableDataProvider<T, S> dataProvider,
                    final int rowsPerPage) {
        super(id, columns, dataProvider, rowsPerPage);
        setOutputMarkupId(true);
        setVersioned(false);
    }

    public final void setSortOrderHintAndBroadcast(final SortOrder order, final String property, final AjaxRequestTarget target) {
        final UiHintContainer uiHintContainer = getUiHintContainer();
        if(uiHintContainer == null) return;

        // first clear all SortOrder hints...
        for (SortOrder eachSortOrder : SortOrder.values()) {
            uiHintContainer.clearHint(this, eachSortOrder.name());
        }
        // .. then set this one
        uiHintContainer.setHint(this, order.name(), property);
    }

    public final void setPageSizeHintAndBroadcast(final AjaxRequestTarget target) {
        final UiHintContainer uiHintContainer = getUiHintContainer();
        if(uiHintContainer == null) return;

        uiHintContainer.setHint(this, UIHINT_PAGE_SIZE, "" + getCurrentPage());
    }

    public final void setPageNumberHintAndBroadcast(final AjaxRequestTarget target) {
        final UiHintContainer uiHintContainer = getUiHintContainer();
        if(uiHintContainer == null) return;

        uiHintContainer.setHint(this, UIHINT_PAGE_NUMBER, "" + getCurrentPage());
    }

    public void setSearchHintAndBroadcast(final AjaxRequestTarget target) {
        final UiHintContainer uiHintContainer = getUiHintContainer();
        if(uiHintContainer == null) return;

        uiHintContainer.setHint(this, UIHINT_SEARCH_ARG, "" + getCurrentPage());
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        //honorSearchArgHint(); //TODO[CAUSEWAY-3794] honorSearchArgHint does not work
        honorPageSizeHint(); //TODO[CAUSEWAY-3794] honorPageSizeHint does not work (resets to default)
        honorPageNumberHint();
    }

    // -- FILTER

    public void setSearchArg(final String value) {
        if(_Strings.nullToEmpty(this.searchArg).equals(_Strings.nullToEmpty(value))) return;
        this.searchArg = value;
        // update the interactive model
        _TableUtils.interactive(this).getSearchArgument().setValue(searchArg);
    }

    // -- PAGESIZE

    /** Used by the {@link PagesizeChooser}, to indicate the currently set page-size. */
    public IModel<String> getEntriesPerPageAsLiteral() {
        return LambdaModel.of(()->
            String.format("%s", getItemsPerPage()<=1000
                    ? "" + getItemsPerPage()
                    : "All"));
    }

    /**
     * Used by the {@link PagesizeChooser}, to offer page-size choices to the end user.
     * (Typically a drop-down select.)
     */
    public List<PagesizeChoice> getPagesizeChoices() {
        var choices = List.of(
                new PagesizeChoice("All", Long.MAX_VALUE),
                new PagesizeChoice("10", 10L),
                new PagesizeChoice("25", 25L),
                new PagesizeChoice("100", 100L),
                new PagesizeChoice("1000", 1000L));
        return choices;
    }

    // -- HELPER

    private final void honorPageSizeHint() {
        var uiHintContainer = getUiHintContainer();
        if(uiHintContainer == null) return;

        parsePageSize(uiHintContainer.getHint(this, UIHINT_PAGE_SIZE))
            .ifPresent(this::setItemsPerPage);
    }

    private final void honorPageNumberHint() {
        var uiHintContainer = getUiHintContainer();
        if(uiHintContainer == null) return;

        parseZeroBasedPageNr(uiHintContainer.getHint(this, UIHINT_PAGE_NUMBER))
            .ifPresent(this::setCurrentPage);
    }

    private final void honorSearchArgHint() {
        var uiHintContainer = getUiHintContainer();
        if(uiHintContainer == null) return;

        setSearchArg(uiHintContainer.getHint(this, UIHINT_SEARCH_ARG));
    }

    private UiHintContainer getUiHintContainer() {
        return UiHintContainer.Util.hintContainerOf(this, UiObjectWkt.class);
    }

    private OptionalLong parsePageSize(final String string) {
        final long pageSize = _Longs.parseLong(string, 10).orElse(-1);
        return pageSize>=1L
                ? OptionalLong.of(pageSize)
                : OptionalLong.empty();
    }

    private OptionalLong parseZeroBasedPageNr(final String string) {
        final long zeroBasedPageNr = _Longs.parseLong(string, 10).orElse(-1);
        return zeroBasedPageNr>=0L
                ? OptionalLong.of(zeroBasedPageNr)
                : OptionalLong.empty();
    }

}
