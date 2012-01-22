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
package org.apache.isis.viewer.dnd.view.look.linux;

import java.awt.event.KeyEvent;
import java.util.Calendar;
import java.util.Date;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.progmodel.facets.value.date.DateValueFacet;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Image;
import org.apache.isis.viewer.dnd.drawing.ImageFactory;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.drawing.Text;
import org.apache.isis.viewer.dnd.field.DatePicker;
import org.apache.isis.viewer.dnd.view.Click;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.KeyboardAction;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.base.AbstractView;
import org.apache.isis.viewer.dnd.view.content.FieldContent;
import org.apache.isis.viewer.dnd.view.content.TextParseableContent;

public class LinuxDatePicker extends AbstractView implements DatePicker {

    protected Image calendarImage = ImageFactory.getInstance().loadImage("calendar2.png");
    protected Image monthUp = ImageFactory.getInstance().loadImage("monthup.png");
    protected Image monthDown = ImageFactory.getInstance().loadImage("monthdown.png");
    protected Image yearUp = ImageFactory.getInstance().loadImage("yearup.png");
    protected Image yearDown = ImageFactory.getInstance().loadImage("yeardown.png");
    protected Image decadeUp = ImageFactory.getInstance().loadImage("decadeup.png");
    protected Image decadeDown = ImageFactory.getInstance().loadImage("decadedown.png");
    protected Image highlight = ImageFactory.getInstance().loadImage("highlight2.png");
    protected Image day_img = ImageFactory.getInstance().loadImage("day2.png");

    private static final int ROWS = 7;// Days
    private static final int COLUMNS = 6;// Weeks
    private final int CONTROL_VERTICAL_INSET = 17;
    private final int CONTROL_VERTICAL_SPACING = monthUp.getWidth() + 5;
    private final int CONTROL_HORIZONTAL_INSET = 215;
    private final int CONTROL_HORIZONTAL_SPACING = monthUp.getHeight() + 5;
    private final int cellWidth = day_img.getWidth();
    private final int cellHeight = day_img.getHeight();
    private final Calendar date;
    private final Text style = Toolkit.getText(ColorsAndFonts.TEXT_NORMAL);
    private Calendar currentDate;
    private final Calendar today;
    private final boolean isEditable;

    String[] monthName = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };

    private Location mouseLocation = new Location(-1, -1);

    public LinuxDatePicker(final Content content) {
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
    }

    private void clearTime(final Calendar date) {
        date.clear(Calendar.AM_PM);
        date.clear(Calendar.HOUR);
        date.clear(Calendar.HOUR_OF_DAY);
        date.clear(Calendar.MINUTE);
        date.clear(Calendar.SECOND);
        date.clear(Calendar.MILLISECOND);
    }

    @Override
    public Size getRequiredSize(final Size availableSpace) {
        return new Size(225, 270);
    }

    @Override
    public void draw(final Canvas canvas) {
        final int width = getSize().getWidth();
        final int height = getSize().getHeight();

        final Color textColor = Toolkit.getColor(ColorsAndFonts.COLOR_WHITE);

        drawBackground(canvas, width, height, textColor);
        drawDaysOfWeek(canvas, textColor);
        drawControls(canvas, width);
    }

    private void drawBackground(final Canvas canvas, final int width, final int height, final Color text) {
        canvas.drawImage(calendarImage, 0, 0);
        canvas.drawText(monthName[today.get(Calendar.MONTH)], 26, 28, text, style);
        canvas.drawText(Integer.toString(today.get(Calendar.YEAR)), 176, 28, text, style);
    }

    private void drawDaysOfWeek(final Canvas canvas, final Color textColor) {
        int x, y = 0;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(today.getTime());
        cal.set(Calendar.DATE, 1);
        cal.add(Calendar.DATE, -cal.get(Calendar.DAY_OF_WEEK) + 1);
        for (int week = 0; week < COLUMNS; week++) {
            y = week * 21 + 60;
            for (int d = 0; d < ROWS; d++) {
                x = new Float(d * 22.5).intValue() + 40;
                Image img = day_img;
                if (cal.get(Calendar.MONTH) == today.get(Calendar.MONTH)) {
                    if (mouseOver(x, y)) {
                        img = highlight;
                    }
                    canvas.drawImage(img, x, y);
                    canvas.drawText(Integer.toString(cal.get(Calendar.DAY_OF_MONTH)), x + 4, y + 15, textColor, style);
                }
                cal.add(Calendar.DATE, +1);
            }
        }
    }

    private boolean mouseOver(final int x, final int y) {
        final int mouseX = mouseLocation.getX();
        final int mouseY = mouseLocation.getY();
        if (mouseX > x && mouseX < x + cellWidth) {
            if (mouseY > y && mouseY < y + cellHeight) {
                return true;
            }
        }
        return false;
    }

    private void drawControls(final Canvas canvas, final int width) {

        canvas.drawImage(monthUp, CONTROL_VERTICAL_INSET, CONTROL_HORIZONTAL_INSET);
        canvas.drawImage(monthDown, CONTROL_VERTICAL_INSET, CONTROL_HORIZONTAL_INSET + CONTROL_HORIZONTAL_SPACING);
        canvas.drawImage(yearUp, CONTROL_VERTICAL_INSET + CONTROL_VERTICAL_SPACING, CONTROL_HORIZONTAL_INSET);
        canvas.drawImage(yearDown, CONTROL_VERTICAL_INSET + CONTROL_VERTICAL_SPACING, CONTROL_HORIZONTAL_INSET + CONTROL_HORIZONTAL_SPACING);
        canvas.drawImage(decadeUp, CONTROL_VERTICAL_INSET + (2 * CONTROL_VERTICAL_SPACING), CONTROL_HORIZONTAL_INSET);
        canvas.drawImage(decadeDown, CONTROL_VERTICAL_INSET + (2 * CONTROL_VERTICAL_SPACING), CONTROL_HORIZONTAL_INSET + CONTROL_HORIZONTAL_SPACING);
    }

    @Override
    public void mouseMoved(final Location location) {
        mouseLocation = location;
        if (location.getX() > 0 && location.getY() > 0) {
            markDamaged();
        }
    }

    @Override
    public void exited() {
        mouseOver(-1, -1);
        super.exited();
    }

    @Override
    public void firstClick(final Click click) {
        final int x = click.getLocation().getX();
        final int y = click.getLocation().getY();
        if (y >= CONTROL_HORIZONTAL_INSET + CONTROL_HORIZONTAL_SPACING && y <= CONTROL_HORIZONTAL_INSET + (2 * CONTROL_HORIZONTAL_SPACING)) {
            if (x <= CONTROL_VERTICAL_INSET + (3 * CONTROL_VERTICAL_SPACING) && x >= CONTROL_VERTICAL_INSET + (2 * CONTROL_VERTICAL_SPACING)) {
                today.add(Calendar.YEAR, -10);
            } else if (x >= CONTROL_VERTICAL_INSET + CONTROL_VERTICAL_SPACING && x <= CONTROL_VERTICAL_INSET + (2 * CONTROL_VERTICAL_SPACING)) {
                today.add(Calendar.YEAR, -1);
            } else if (x >= CONTROL_VERTICAL_INSET && x <= CONTROL_VERTICAL_INSET + CONTROL_VERTICAL_SPACING) {
                today.add(Calendar.MONTH, -1);
            }
            markDamaged();
            return;
        }
        if (y > CONTROL_HORIZONTAL_INSET && y <= CONTROL_HORIZONTAL_INSET + CONTROL_HORIZONTAL_SPACING) {
            if (x <= CONTROL_VERTICAL_INSET + (3 * CONTROL_VERTICAL_SPACING) && x >= CONTROL_VERTICAL_INSET + (2 * CONTROL_VERTICAL_SPACING)) {
                today.add(Calendar.YEAR, 10);
            } else if (x >= CONTROL_VERTICAL_INSET + CONTROL_VERTICAL_SPACING && x <= CONTROL_VERTICAL_INSET + (2 * CONTROL_VERTICAL_SPACING)) {
                today.add(Calendar.YEAR, 1);
            } else if (x >= CONTROL_VERTICAL_INSET && x <= CONTROL_VERTICAL_INSET + CONTROL_VERTICAL_SPACING) {
                today.add(Calendar.MONTH, 1);
            }
            markDamaged();
            return;
        }
        if (isEditable) {
            final Location location = click.getLocation();
            final Date selectedDate = getSelectedDate(location);
            if (selectedDate != null) {
                date.setTime(selectedDate);
                final Content content = getContent();
                final DateValueFacet facet = content.getSpecification().getFacet(DateValueFacet.class);
                final ObjectAdapter value = facet.createValue(date.getTime());
                ((TextParseableContent) content).parseTextEntry(value.titleString());
                ((TextParseableContent) content).entryComplete();
                getView().refresh();
            }
        }
        getViewManager().clearOverlayView();
    }

    private Date getSelectedDate(final Location location) {
        int x, y = 0;
        final Calendar cal = Calendar.getInstance();
        cal.setTime(today.getTime());
        cal.set(Calendar.DATE, 1);
        cal.add(Calendar.DATE, -cal.get(Calendar.DAY_OF_WEEK) + 1);
        for (int week = 0; week < COLUMNS; week++) {
            y = (week * 21) + 60;
            for (int d = 0; d < ROWS; d++) {
                x = new Float(d * 22.5).intValue() + 40;
                if (location.getX() > x && location.getX() < x + cellWidth) {
                    if (location.getY() > y && location.getY() < y + cellHeight) {
                        return cal.getTime();// This is the selected date
                    }
                }
                cal.add(Calendar.DATE, +1);
            }
        }
        return null;
    }

    @Override
    public void keyPressed(final KeyboardAction key) {
        if (isEditable && key.getKeyCode() == KeyEvent.VK_ESCAPE) {
            getViewManager().clearOverlayView();
        } else {
            super.keyPressed(key);
        }
    }
}
