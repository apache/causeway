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

package org.apache.isis.viewer.dnd.calendar;

import java.util.Date;
import java.util.List;

import org.apache.isis.core.commons.exceptions.UnexpectedCallException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.progmodel.facets.value.date.DateValueFacet;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.icon.IconElementFactory;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.FocusManager;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewConstants;
import org.apache.isis.viewer.dnd.view.Workspace;
import org.apache.isis.viewer.dnd.view.base.BlankView;
import org.apache.isis.viewer.dnd.view.border.ScrollBorder;
import org.apache.isis.viewer.dnd.view.collection.CollectionContent;
import org.apache.isis.viewer.dnd.view.composite.CollectionElementBuilder;
import org.apache.isis.viewer.dnd.view.composite.CompositeView;
import org.apache.isis.viewer.dnd.view.composite.CompositeViewUsingBuilder;
import org.apache.isis.viewer.dnd.view.composite.StackLayout;
import org.apache.isis.viewer.dnd.view.content.NullContent;
import org.apache.isis.viewer.dnd.view.option.UserActionAbstract;

public class CalendarGrid extends CompositeView {
    private Cells cellLayout;
    private int rows;
    private int columns;
    private boolean acrossThenDown;

    @Override
    protected void buildNewView() {
        final CalendarCellContent[] cellContents = createContentForCells();
        addCellsToView(cellContents);
    }

    private void addCellsToView(final CalendarCellContent[] cellContents) {
        final View[] cells = new View[rows * columns];
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                final int cellNo = acrossThenDown ? row * columns + column : column * rows + row;
                View cell;
                if (cellContents[cellNo] == null) {
                    cell = new BlankView(new NullContent());
                } else {
                    cell = new CompositeViewUsingBuilder(cellContents[cellNo], null, new Axes(), new StackLayout(), new CollectionElementBuilder(new IconElementFactory()));
                    cell = new ScrollBorder(cell);
                }
                cells[cellNo] = cell;
                addView(cell);
            }
        }
    }

    private CalendarCellContent[] createContentForCells() {
        final CalendarCellContent[] cellContents = new CalendarCellContent[rows * columns];
        final CollectionContent content = (CollectionContent) getContent();
        for (final ObjectAdapter element : content.elements()) {
            final Date date = dateFor(element);
            if (date == null) {
                continue;
            }
            final int period = cellLayout.getPeriodFor(date);
            if (period >= 0 && period < cellContents.length) {
                if (cellContents[period] == null) {
                    cellContents[period] = new CalendarCellContent(cellLayout.title(period));
                }
                cellContents[period].addElement(element);
            }
        }
        return cellContents;
    }

    private Date dateFor(final ObjectAdapter element) {
        final ObjectAssociation dateField = findDate(element);
        if (dateField == null) {
            return null;
        }
        final DateValueFacet facet = dateField.getSpecification().getFacet(DateValueFacet.class);
        final ObjectAdapter field = dateField.get(element);
        final Date date = facet.dateValue(field);
        return date;
    }

    private ObjectAssociation findDate(final ObjectAdapter adapter) {
        final ObjectSpecification spec = adapter.getSpecification();
        final List<ObjectAssociation> fields = spec.getAssociations(Contributed.EXCLUDED);
        for (int i = 0; i < fields.size(); i++) {
            final Facet facet = fields.get(i).getSpecification().getFacet(DateValueFacet.class);
            if (facet != null) {
                return fields.get(i);
            }
        }
        return null;
    }

    @Override
    protected void buildModifiedView() {
        disposeContentsOnly();
        buildNewView();
    }

    // TODO remove
    @Override
    protected void buildView() {
        throw new UnexpectedCallException();
    }

    @Override
    protected void doLayout(final Size maximumSize) {
        final boolean hasHeader = cellLayout.header(0) != null;
        final int topInset = 0 + (acrossThenDown && hasHeader ? 30 : 0);
        final int leftInset = !acrossThenDown && hasHeader ? 50 : 0;
        final int width = maximumSize.getWidth();
        final int height = maximumSize.getHeight();
        final int columnWidth = (width - leftInset) / columns;
        final int rowHeight = (height - topInset) / rows;

        final View[] cells = getSubviews();
        int i = 0;
        final int top = CalendarConstants.style.getLineHeight() + ViewConstants.VPADDING;
        CalendarConstants.style.getLineSpacing();
        final Location location = new Location(leftInset, topInset + top);
        final Size size = new Size(columnWidth, rowHeight - top);
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                final View cell = cells[i++];
                cell.setSize(size);
                cell.setLocation(location);
                location.add(columnWidth, 0);
            }
            location.setX(leftInset);
            location.add(0, rowHeight);
        }
    }

    @Override
    public Size requiredSize(final Size availableSpace) {
        return new Size(300, 300);
    }

    protected CalendarGrid(final Content content) {
        super(content, null);

        cellLayout = new DayCells(null);
        acrossThenDown = true;
        rows = cellLayout.defaultRows();
        columns = cellLayout.defaultColumns();
        cellLayout.add(-rows * columns / 2);
    }

    @Override
    public void setFocusManager(final FocusManager focusManager) {
        // this.focusManager = focusManager;
    }

    @Override
    public void draw(final Canvas canvas) {
        super.draw(canvas);

        final boolean hasHeader = cellLayout.header(0) != null;
        final int topInset = 0 + (acrossThenDown && hasHeader ? 30 : 0);
        final int leftInset = !acrossThenDown && hasHeader ? 50 : 0;
        final int width = getSize().getWidth();
        final int height = getSize().getHeight();
        final int columnWidth = (width - leftInset) / columns;
        final int rowHeight = (height - topInset) / rows;

        for (int row = 0; row < rows; row++) {
            final int y = topInset + row * rowHeight;
            if (!acrossThenDown && hasHeader) {
                canvas.drawText(cellLayout.header(row), 0, y + 20, CalendarConstants.textColor, CalendarConstants.style);
            }
            canvas.drawLine(leftInset, y, width, y, CalendarConstants.lineColor);
        }
        canvas.drawLine(leftInset, topInset + height - 1, width, topInset + height - 1, CalendarConstants.lineColor);

        for (int column = 0; column < columns; column++) {
            final int x = leftInset + column * columnWidth;
            if (acrossThenDown && hasHeader) {
                canvas.drawText(cellLayout.header(column), x, topInset - 5, CalendarConstants.textColor, CalendarConstants.style);
            }
            canvas.drawLine(x, topInset, x, topInset + height, CalendarConstants.lineColor);
        }
        canvas.drawLine(width - 1, topInset, width - 1, height, CalendarConstants.lineColor);

        for (int row = 0; row < rows; row++) {
            final int y = topInset + row * rowHeight + CalendarConstants.style.getAscent() + 2;
            for (int column = 0; column < columns; column++) {
                final int x = leftInset + column * columnWidth + 2;
                final int cell = acrossThenDown ? row * columns + column : column * rows + row;
                canvas.drawText(cellLayout.title(cell), x, y, CalendarConstants.textColor, CalendarConstants.style);
            }
        }
    }

    void addRow() {
        rows++;
        invalidateContent();
        markDamaged();
    }

    void removeRow() {
        rows--;
        invalidateContent();
        markDamaged();
    }

    void addColumn() {
        columns++;
        invalidateContent();
        markDamaged();
    }

    void removeColumn() {
        columns--;
        invalidateContent();
        markDamaged();
    }

    void showYears() {
        cellLayout = new YearCells(cellLayout);
        show();
    }

    void showMonths() {
        cellLayout = new MonthCells(cellLayout);
        show();
    }

    void showWeeks() {
        cellLayout = new WeekCells(cellLayout);
        show();
    }

    void show() {
        cellLayout.roundDown();
        rows = cellLayout.defaultRows();
        columns = cellLayout.defaultColumns();
        invalidateContent();
        markDamaged();
    }

    void showSingleDay() {
        cellLayout = new SingleDayCells(cellLayout);
        show();
    }

    void showDays() {
        cellLayout = new DayCells(cellLayout);
        show();
    }

    void acrossFirst() {
        acrossThenDown = true;
        final int temp = rows;
        rows = columns;
        columns = temp;
        invalidateContent();
        markDamaged();
    }

    void downFirst() {
        acrossThenDown = false;
        final int temp = rows;
        rows = columns;
        columns = temp;
        invalidateContent();
        markDamaged();
    }

    void nextPeriod() {
        cellLayout.add(rows * columns);
        invalidateContent();
        markDamaged();
    }

    void previousePeriod() {
        cellLayout.add(-rows * columns);
        invalidateContent();
        markDamaged();
    }

    void today() {
        cellLayout.today();
        invalidateContent();
        markDamaged();
    }

    @Override
    public void viewMenuOptions(final UserActionSet options) {
        super.viewMenuOptions(options);

        options.add(new UserActionAbstract("Add row") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                addRow();
            }
        });
        options.add(new UserActionAbstract("Remove row") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                removeRow();
            }
        });

        options.add(new UserActionAbstract("Add column") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                addColumn();
            }
        });
        options.add(new UserActionAbstract("Remove column") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                removeColumn();
            }
        });

        options.add(new UserActionAbstract("Years") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                showYears();
            }
        });

        options.add(new UserActionAbstract("Months") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                showMonths();
            }
        });

        options.add(new UserActionAbstract("Weeks") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                showWeeks();
            }
        });

        options.add(new UserActionAbstract("Day") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                showSingleDay();
            }
        });
        options.add(new UserActionAbstract("Days") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                showDays();
            }
        });

        options.add(new UserActionAbstract("Across then down") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                acrossFirst();
            }
        });

        options.add(new UserActionAbstract("Down then across") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                downFirst();
            }
        });

        options.add(new UserActionAbstract("Previous period") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                previousePeriod();
            }
        });

        options.add(new UserActionAbstract("Next period") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                nextPeriod();
            }
        });

        options.add(new UserActionAbstract("Today") {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                today();
            }
        });
    }

}
