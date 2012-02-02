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
package org.apache.isis.viewer.dnd.field;

import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.progmodel.facets.value.date.DateValueFacet;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Shape;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.drawing.Text;
import org.apache.isis.viewer.dnd.view.Click;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.KeyboardAction;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.base.AbstractView;
import org.apache.isis.viewer.dnd.view.content.FieldContent;
import org.apache.isis.viewer.dnd.view.content.TextParseableContent;

public class SimpleDatePicker extends AbstractView implements DatePicker {
    private static class Button {
        private final char key;
        private final int period;
        private final int increment;

        public Button(final char key, final int period, final int increment) {
            this.key = key;
            this.period = period;
            this.increment = increment;
        }

        public void adjustDate(final Calendar date) {
            date.add(period, increment);
        }

        public String getLabel() {
            return "" + key;
        }
    }

    private static final Button[] buttons = new Button[] { new Button('W', Calendar.WEEK_OF_YEAR, 1), new Button('F', Calendar.WEEK_OF_YEAR, 2), new Button('M', Calendar.MONTH, 1), new Button('Q', Calendar.MONTH, 3), new Button('Y', Calendar.YEAR, 1), new Button('D', Calendar.YEAR, 10),

    new Button('w', Calendar.WEEK_OF_YEAR, -1), new Button('f', Calendar.WEEK_OF_YEAR, -2), new Button('m', Calendar.MONTH, -1), new Button('q', Calendar.MONTH, -3), new Button('y', Calendar.YEAR, -1), new Button('d', Calendar.YEAR, -10) };
    private static final int ROWS = 7;
    private static final int COLUMNS = 7;
    private static final int PADDING = 5;
    private final Calendar date;
    private final Text style = Toolkit.getText(ColorsAndFonts.TEXT_NORMAL);
    private final int labelWidth = style.stringWidth("XXX 0000") * 4 / 3;
    private final int cellWidth = style.stringWidth("00") * 8 / 5;
    private final int cellHeight = style.getLineHeight() * 4 / 3;
    private final int headerHeight = style.getLineHeight() + PADDING * 2;
    private final int firstRowBaseline = headerHeight + style.getLineHeight();
    private final int calendarHeight = cellHeight * ROWS;
    private final int controlWidth = style.charWidth('W') + 4;
    private final int controlHeight = style.getLineHeight() * 11 / 10 + controlWidth + 4 + PADDING * 2;
    private int mouseOverButton = -1;
    private int mouseOverRow = -1;
    private int mouseOverColumn;
    private Calendar currentDate;
    private final Calendar today;
    private final DateFormat monthFormat = new SimpleDateFormat("MMM");
    private final DateFormat dayFormat = new SimpleDateFormat("EEE");
    private final boolean isEditable;

    public SimpleDatePicker(final Content content) {
        super(content);

        isEditable = content instanceof FieldContent && ((FieldContent) content).isEditable().isAllowed();

        today = Calendar.getInstance();
        clearTime(today);

        date = Calendar.getInstance();
        final ObjectAdapter dateAdapter = ((TextParseableContent) getContent()).getAdapter();
        if (dateAdapter != null) {
            final DateValueFacet facet = dateAdapter.getSpecification().getFacet(DateValueFacet.class);
            currentDate = Calendar.getInstance();
            final Date dateValue = facet.dateValue(dateAdapter);
            currentDate.setTime(dateValue);
            clearTime(currentDate);
            date.setTime(dateValue);
        }
        clearTime(date);
        date.add(Calendar.DAY_OF_MONTH, -21);
        roundDownDate();
    }

    private void roundDownDate() {
        date.add(Calendar.DAY_OF_MONTH, date.getFirstDayOfWeek() - date.get(Calendar.DAY_OF_WEEK));
    }

    private void clearTime(final Calendar date) {
        date.clear(Calendar.AM_PM);
        date.clear(Calendar.HOUR);
        date.clear(Calendar.HOUR_OF_DAY);
        date.clear(Calendar.MINUTE);
        date.clear(Calendar.SECOND);
        date.clear(Calendar.MILLISECOND);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.nakedobjects.plugins.dnd.field.DatePicker#getRequiredSize(org.
     * nakedobjects.plugins.dnd.drawing.Size)
     */
    @Override
    public Size getRequiredSize(final Size availableSpace) {
        return new Size(labelWidth + COLUMNS * cellWidth + 2, headerHeight + calendarHeight + controlHeight + 2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.nakedobjects.plugins.dnd.field.DatePicker#draw(org.nakedobjects.plugins
     * .dnd.drawing.Canvas)
     */
    @Override
    public void draw(final Canvas canvas) {
        final int width = getSize().getWidth();
        final int height = getSize().getHeight();

        final Color secondaryTextColor = Toolkit.getColor(ColorsAndFonts.COLOR_WHITE);
        final Color mainTextColor = Toolkit.getColor(ColorsAndFonts.COLOR_BLACK);

        drawBackground(canvas, width, height);
        drawDaysOfWeek(canvas, secondaryTextColor);
        if (isEditable) {
            drawDayMarker(canvas);
        }
        drawMonthsAndWeeks(canvas, secondaryTextColor);
        drawDays(canvas, mainTextColor);
        drawControls(canvas, width);
    }

    private void drawBackground(final Canvas canvas, final int width, final int height) {
        canvas.drawSolidRectangle(0, 0, width - 1, headerHeight, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY1));
        canvas.drawSolidRectangle(0, 0, labelWidth, height - 1, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY1));
        canvas.drawSolidRectangle(labelWidth, headerHeight, width - labelWidth - 1, height - cellHeight - 1, Toolkit.getColor(ColorsAndFonts.COLOR_WINDOW));
        canvas.drawRectangle(0, 0, width - 1, height - 1, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY1));
    }

    private void drawDaysOfWeek(final Canvas canvas, final Color textColor) {
        final Calendar d = Calendar.getInstance();
        d.setTime(date.getTime());
        int x = labelWidth + cellWidth / 2;
        final int y = PADDING + style.getAscent();
        for (int column = 0; column < 7; column++) {
            final String day = dayFormat.format(d.getTime()).substring(0, 1);
            canvas.drawText(day, x - style.stringWidth(day) / 2, y, textColor, style);
            x += cellWidth;
            d.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void drawDayMarker(final Canvas canvas) {
        if (mouseOverColumn >= 0 && mouseOverColumn < COLUMNS && mouseOverRow >= 0 && mouseOverRow < ROWS) {
            canvas.drawRectangle(labelWidth + mouseOverColumn * cellWidth, headerHeight + mouseOverRow * cellHeight, cellWidth, cellHeight, Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY3));
        }
    }

    private int drawMonthsAndWeeks(final Canvas canvas, final Color color) {
        final Calendar d = Calendar.getInstance();
        d.setTime(date.getTime());
        int y = firstRowBaseline;
        String lastMonth = "";
        for (int row = 0; row < ROWS; row++) {
            final int x = labelWidth;
            final String month = monthFormat.format(d.getTime()) + " " + d.get(Calendar.YEAR);
            if (!month.equals(lastMonth)) {
                canvas.drawText(month, x - style.stringWidth(month) - PADDING, y, color, style);
                lastMonth = month;
            } else {
                final String week = "wk " + (d.get(Calendar.WEEK_OF_YEAR));
                canvas.drawText(week == null ? "*" : week, x - style.stringWidth(week) - PADDING, y, color, style);
            }
            d.add(Calendar.DAY_OF_MONTH, 7);
            y += cellHeight;
        }
        return y;
    }

    private int drawDays(final Canvas canvas, final Color mainTextColor) {
        final Calendar d = Calendar.getInstance();
        d.setTime(date.getTime());
        int y = firstRowBaseline;
        for (int row = 0; row < ROWS; row++) {
            int x = labelWidth;
            for (int column = 0; column < COLUMNS; column++) {
                final String day = "" + d.get(Calendar.DAY_OF_MONTH);
                if (currentDate != null && currentDate.equals(d)) {
                    canvas.drawSolidRectangle(x, headerHeight + row * cellHeight, cellWidth, cellHeight, Toolkit.getColor(ColorsAndFonts.COLOR_PRIMARY2));
                }
                if (today.getTime().equals(d.getTime())) {
                    canvas.drawRectangle(x, headerHeight + row * cellHeight, cellWidth, cellHeight, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY2));
                }
                canvas.drawText(day, x - PADDING / 2 + cellWidth - style.stringWidth(day), y, mainTextColor, style);
                x += cellWidth;
                d.add(Calendar.DAY_OF_MONTH, 1);
            }
            y += cellHeight;
        }
        return y;
    }

    private void drawControls(final Canvas canvas, final int width) {
        int x = labelWidth + PADDING;
        final int y = headerHeight + calendarHeight + PADDING;

        final int spaceTaken = width - labelWidth - 2 * PADDING - 6 * controlWidth;
        final int spaceBetween = spaceTaken / 5;
        final int adjustment = spaceTaken - 5 * spaceBetween - 2;

        for (int i = 0; i < buttons.length / 2; i++) {
            drawControl(canvas, x, y, controlWidth, controlHeight - PADDING * 2, buttons[i].getLabel(), i);
            x += controlWidth + spaceBetween + (i == 3 ? adjustment : 0);
        }
    }

    private void drawControl(final Canvas canvas, final int x, final int y, final int width, final int height, final String label, final int over) {
        if (Toolkit.debug) {
            canvas.drawRectangle(x - 2, y, width + 4, height, Toolkit.getColor(ColorsAndFonts.COLOR_DEBUG_BOUNDS_VIEW));
        }
        final Color color = Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY2);

        final int arrowHeight = width / 2;
        final Shape upArrow = new Shape(0, arrowHeight);
        upArrow.addVector(arrowHeight, -arrowHeight);
        upArrow.addVector(arrowHeight, arrowHeight);
        if (mouseOverButton == over + 6) {
            canvas.drawSolidShape(upArrow, x, y + 2, color);
        } else {
            canvas.drawShape(upArrow, x, y + 2, color);
        }

        final Shape downArrow = new Shape(0, 0);
        downArrow.addVector(arrowHeight, arrowHeight);
        downArrow.addVector(arrowHeight, -arrowHeight);
        if (mouseOverButton == over) {
            canvas.drawSolidShape(downArrow, x, y + height - 4 - arrowHeight, color);
        } else {
            canvas.drawShape(downArrow, x, y + height - 4 - arrowHeight, color);
        }
        final int charWidth = style.stringWidth(label);
        canvas.drawText(label, x + width / 2 - charWidth / 2, y + 2 + arrowHeight + style.getAscent(), color, style);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.nakedobjects.plugins.dnd.field.DatePicker#mouseMoved(org.nakedobjects
     * .plugins.dnd.drawing.Location)
     */
    @Override
    public void mouseMoved(final Location location) {
        final int over = overButton(location);
        if (over == -1) {
            mouseOverButton = -1;
            final int col = column(location);
            final int row = row(location);
            if (col != mouseOverColumn || row != mouseOverRow) {
                if (isEditable) {
                    mouseOverColumn = col;
                    mouseOverRow = row;
                }
                markDamaged();
            }
        } else if (over != mouseOverButton) {
            mouseOverButton = over;
            markDamaged();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.nakedobjects.plugins.dnd.field.DatePicker#exited()
     */
    @Override
    public void exited() {
        mouseOverButton = -1;
        super.exited();
    }

    private int overButton(final Location location) {
        final int x = location.getX();
        final int y = location.getY();
        final int verticalBoundary = headerHeight + calendarHeight + PADDING;
        final int height = controlHeight - PADDING * 2;
        if (x > labelWidth && y > verticalBoundary && y < verticalBoundary + height) {
            int column = (x - labelWidth) / ((getSize().getWidth() - labelWidth) / 6);
            if (y <= verticalBoundary + height / 2) {
                column += 6;
            }
            return column;
        } else {
            return -1;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.nakedobjects.plugins.dnd.field.DatePicker#firstClick(org.nakedobjects
     * .plugins.dnd.view.Click)
     */
    @Override
    public void firstClick(final Click click) {
        if (mouseOverButton != -1) {
            buttons[mouseOverButton].adjustDate(date);
            roundDownDate();
            markDamaged();
            return;
        }

        if (isEditable) {
            final Location location = click.getLocation();
            final int col = column(location);
            final int row = row(location);
            if (col >= 0 && col < COLUMNS && row >= 0 && row < ROWS) {
                date.add(Calendar.DAY_OF_MONTH, row * 7 + col);
                final Content content = getContent();
                final DateValueFacet facet = content.getSpecification().getFacet(DateValueFacet.class);
                final ObjectAdapter value = facet.createValue(date.getTime());
                ((TextParseableContent) content).parseTextEntry(value.titleString());
                ((TextParseableContent) content).entryComplete();
                /*
                 * if (content.isObject()) { ((ObjectContent)
                 * content).setObject(value); }
                 */
                // content.
                getView().refresh();

                // content.
            }
        }
        getViewManager().clearOverlayView();
    }

    private int row(final Location location) {
        return (location.getY() - headerHeight) / cellHeight;
    }

    private int column(final Location location) {
        return (location.getX() - labelWidth) / cellWidth;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.nakedobjects.plugins.dnd.field.DatePicker#keyPressed(org.nakedobjects
     * .plugins.dnd.view.KeyboardAction)
     */
    @Override
    public void keyPressed(final KeyboardAction key) {
        if (isEditable && key.getKeyCode() == KeyEvent.VK_ESCAPE) {
            getViewManager().clearOverlayView();
        } else {
            super.keyPressed(key);
        }
    }
}
