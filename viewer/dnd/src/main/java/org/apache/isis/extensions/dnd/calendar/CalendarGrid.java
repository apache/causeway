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


package org.apache.isis.extensions.dnd.calendar;


import java.util.Date;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facets.Facet;
import org.apache.isis.metamodel.facets.value.DateValueFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.commons.exceptions.UnexpectedCallException;
import org.apache.isis.extensions.dnd.drawing.Canvas;
import org.apache.isis.extensions.dnd.drawing.Location;
import org.apache.isis.extensions.dnd.drawing.Size;
import org.apache.isis.extensions.dnd.icon.IconElementFactory;
import org.apache.isis.extensions.dnd.view.Axes;
import org.apache.isis.extensions.dnd.view.Content;
import org.apache.isis.extensions.dnd.view.FocusManager;
import org.apache.isis.extensions.dnd.view.UserActionSet;
import org.apache.isis.extensions.dnd.view.View;
import org.apache.isis.extensions.dnd.view.Workspace;
import org.apache.isis.extensions.dnd.view.base.BlankView;
import org.apache.isis.extensions.dnd.view.border.ScrollBorder;
import org.apache.isis.extensions.dnd.view.collection.CollectionContent;
import org.apache.isis.extensions.dnd.view.composite.CollectionElementBuilder;
import org.apache.isis.extensions.dnd.view.composite.CompositeView;
import org.apache.isis.extensions.dnd.view.composite.CompositeViewUsingBuilder;
import org.apache.isis.extensions.dnd.view.composite.StackLayout;
import org.apache.isis.extensions.dnd.view.content.NullContent;
import org.apache.isis.extensions.dnd.view.option.UserActionAbstract;

public class CalendarGrid extends CompositeView {
    private Cells cellLayout;
    private int rows;
    private int columns;
    private boolean acrossThenDown;

    protected void buildNewView() {
        CalendarCellContent[] cellContents = createContentForCells();      
        addCellsToView(cellContents);
    }

    private void addCellsToView(CalendarCellContent[] cellContents) {
        View[] cells = new View[rows * columns];
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int cellNo = acrossThenDown ? row * columns + column : column * rows + row;
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
        CalendarCellContent[] cellContents = new CalendarCellContent[rows * columns];
        CollectionContent content = (CollectionContent) getContent();
        for (ObjectAdapter element: content.elements()) {
            Date date = dateFor(element);
            if (date == null) {
                continue;
            }
            int period = cellLayout.getPeriodFor(date);
            if (period >= 0 && period < cellContents.length) {
                if (cellContents[period] == null) {
                    cellContents[period] = new CalendarCellContent(cellLayout.title(period));
                }
                cellContents[period].addElement(element);
            }
        }
        return cellContents;
    }
    
    private Date dateFor(ObjectAdapter element) {
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
        final ObjectAssociation[] fields = spec.getAssociations();
        for (int i = 0; i < fields.length; i++) {
            final Facet facet = fields[i].getSpecification().getFacet(DateValueFacet.class);
            if (facet != null) {
                return fields[i];
            }
        }
        return null;
    }

    protected void buildModifiedView() {
        disposeContentsOnly();
        buildNewView();
    }
    
    // TODO remove
    protected void buildView() {
        throw new UnexpectedCallException();
    }

    protected void doLayout(Size maximumSize) {
        boolean hasHeader = cellLayout.header(0) != null;
        int topInset = 0 + (acrossThenDown && hasHeader ? 30 : 0);
        int leftInset = !acrossThenDown && hasHeader ? 50 : 0;
        int width = maximumSize.getWidth();
        int height = maximumSize.getHeight();
        int columnWidth = (width - leftInset) / columns;
        int rowHeight = (height - topInset) / rows;

        View[] cells = getSubviews();
        int i = 0;
        int top = CalendarConstants.style.getLineHeight() + VPADDING; CalendarConstants.style.getLineSpacing();
        Location location = new Location(leftInset, topInset + top);
        Size size = new Size(columnWidth, rowHeight - top);
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                View cell = cells[i++];
                cell.setSize(size);
                cell.setLocation(location);
                location.add(columnWidth, 0);
            }
            location.setX(leftInset);
            location.add(0, rowHeight);
        }
    }

    public Size requiredSize(Size availableSpace) {
        return new Size(300, 300);
    }
    
    protected CalendarGrid(Content content) {
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
    public void draw(Canvas canvas) {
        super.draw(canvas);
        
        boolean hasHeader = cellLayout.header(0) != null;
        int topInset = 0 + (acrossThenDown && hasHeader ? 30 : 0);
        int leftInset = !acrossThenDown && hasHeader ? 50 : 0;
        int width = getSize().getWidth();
        int height = getSize().getHeight();
        int columnWidth = (width - leftInset) / columns;
        int rowHeight = (height - topInset) / rows;

        for (int row = 0; row < rows; row++) {
            int y = topInset + row * rowHeight;
            if (!acrossThenDown && hasHeader) {
                canvas.drawText(cellLayout.header(row), 0, y + 20, CalendarConstants.textColor, CalendarConstants.style);
            }
            canvas.drawLine(leftInset, y, width, y, CalendarConstants.lineColor);
        }
        canvas.drawLine(leftInset, topInset + height - 1, width, topInset + height - 1, CalendarConstants.lineColor);

        for (int column = 0; column < columns; column++) {
            int x = leftInset + column * columnWidth;
            if (acrossThenDown && hasHeader) {
                canvas.drawText(cellLayout.header(column), x, topInset - 5, CalendarConstants.textColor, CalendarConstants.style);
            }
            canvas.drawLine(x, topInset, x, topInset + height, CalendarConstants.lineColor);
        }
        canvas.drawLine(width - 1, topInset, width - 1, height, CalendarConstants.lineColor);

        for (int row = 0; row < rows; row++) {
            int y = topInset + row * rowHeight + CalendarConstants.style.getAscent() + 2;
            for (int column = 0; column < columns; column++) {
                int x = leftInset + column * columnWidth + 2;
                int cell = acrossThenDown ? row * columns + column : column * rows + row;
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
        int temp = rows;
        rows = columns;
        columns = temp;
        invalidateContent();
        markDamaged();
    }

    void downFirst() {
        acrossThenDown = false;
        int temp = rows;
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
    public void viewMenuOptions(UserActionSet options) {
        super.viewMenuOptions(options);

        options.add(new UserActionAbstract("Add row") {
            public void execute(Workspace workspace, View view, Location at) {
                addRow();
            }
        });
        options.add(new UserActionAbstract("Remove row") {
            public void execute(Workspace workspace, View view, Location at) {
                removeRow();
            }
        });

        options.add(new UserActionAbstract("Add column") {
            public void execute(Workspace workspace, View view, Location at) {
                addColumn();
            }
        });
        options.add(new UserActionAbstract("Remove column") {
            public void execute(Workspace workspace, View view, Location at) {
                removeColumn();
            }
        });

        options.add(new UserActionAbstract("Years") {
            public void execute(Workspace workspace, View view, Location at) {
                showYears();
            }
        });

        options.add(new UserActionAbstract("Months") {
            public void execute(Workspace workspace, View view, Location at) {
                showMonths();
            }
        });

        options.add(new UserActionAbstract("Weeks") {
            public void execute(Workspace workspace, View view, Location at) {
                showWeeks();
            }
        });

        options.add(new UserActionAbstract("Day") {
            public void execute(Workspace workspace, View view, Location at) {
                showSingleDay();
            }
        });
        options.add(new UserActionAbstract("Days") {
            public void execute(Workspace workspace, View view, Location at) {
                showDays();
            }
        });

        options.add(new UserActionAbstract("Across then down") {
            public void execute(Workspace workspace, View view, Location at) {
                acrossFirst(); 
            }
        });

        options.add(new UserActionAbstract("Down then across") {
            public void execute(Workspace workspace, View view, Location at) {
                downFirst();
            }
        });

        options.add(new UserActionAbstract("Previous period") {
            public void execute(Workspace workspace, View view, Location at) {
                previousePeriod();
            }
        });

        options.add(new UserActionAbstract("Next period") {
            public void execute(Workspace workspace, View view, Location at) {
                nextPeriod();
            }
        });

        options.add(new UserActionAbstract("Today") {
            public void execute(Workspace workspace, View view, Location at) {
                today();
            }
        });
    }

}


