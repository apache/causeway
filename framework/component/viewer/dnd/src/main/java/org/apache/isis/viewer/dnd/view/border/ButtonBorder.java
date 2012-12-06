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

package org.apache.isis.viewer.dnd.view.border;

import java.awt.event.KeyEvent;

import org.apache.isis.viewer.dnd.drawing.Bounds;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.ButtonAction;
import org.apache.isis.viewer.dnd.view.Click;
import org.apache.isis.viewer.dnd.view.KeyboardAction;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewConstants;
import org.apache.isis.viewer.dnd.view.base.AbstractBorder;
import org.apache.isis.viewer.dnd.view.control.Button;

public class ButtonBorder extends AbstractBorder {
    private static final int BUTTON_SPACING = 5;
    private final View[] buttons;
    private ButtonAction defaultAction;

    public ButtonBorder(final ButtonAction[] actions, final View view) {
        super(view);

        buttons = new View[actions.length];
        for (int i = 0; i < actions.length; i++) {
            final ButtonAction action = actions[i];
            buttons[i] = new Button(action, view);
            if (action.isDefault()) {
                defaultAction = action;
            }
        }
        // space for: line & button with whitespace
        bottom = 1 + ViewConstants.VPADDING + buttons[0].getRequiredSize(new Size()).getHeight() + ViewConstants.VPADDING;

    }

    @Override
    public void draw(final Canvas canvas) {
        // draw buttons
        for (final View button : buttons) {
            final Canvas buttonCanvas = canvas.createSubcanvas(button.getBounds());
            button.draw(buttonCanvas);
            final int buttonWidth = button.getSize().getWidth();
            buttonCanvas.offset(BUTTON_SPACING + buttonWidth, 0);
        }

        // draw rest
        super.draw(canvas);
    }

    @Override
    public void firstClick(final Click click) {
        final View button = overButton(click.getLocation());
        if (button == null) {
            super.firstClick(click);
        } else {
            button.firstClick(click);
        }
    }

    public View[] getButtons() {
        return buttons;
    }

    @Override
    public Size getRequiredSize(final Size maximumSize) {
        final Size size = super.getRequiredSize(maximumSize);
        size.ensureWidth(totalButtonWidth());
        size.extendWidth(BUTTON_SPACING * 2);
        return size;
    }

    @Override
    public View identify(final Location location) {
        for (final View button : buttons) {
            if (button.getBounds().contains(location)) {
                return button;
            }
        }
        return super.identify(location);
    }

    @Override
    public void keyPressed(final KeyboardAction key) {
        if (key.getKeyCode() == KeyEvent.VK_ENTER) {
            if (defaultAction != null && defaultAction.disabled(getView()).isAllowed()) {
                key.consume();
                defaultAction.execute(getWorkspace(), getView(), getLocation());
            }
        }

        super.keyPressed(key);
    }

    public void layout(final int width) {
        int x = width / 2 - totalButtonWidth() / 2;
        final int y = getSize().getHeight() - ViewConstants.VPADDING - buttons[0].getRequiredSize(new Size()).getHeight();

        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = buttons[i];
            buttons[i].setSize(buttons[i].getRequiredSize(new Size()));
            buttons[i].setLocation(new Location(x, y));

            x += buttons[i].getSize().getWidth();
            x += BUTTON_SPACING;
        }
    }

    @Override
    public void mouseDown(final Click click) {
        final View button = overButton(click.getLocation());
        if (button == null) {
            super.mouseDown(click);
        } else {
            button.mouseDown(click);
        }
    }

    @Override
    public void mouseUp(final Click click) {
        final View button = overButton(click.getLocation());
        if (button == null) {
            super.mouseUp(click);
        } else {
            button.mouseUp(click);
        }
    }

    /**
     * Finds the action button under the pointer; returning null if none.
     */
    private View overButton(final Location location) {
        for (final View button : buttons) {
            if (button.getBounds().contains(location)) {
                return button;
            }
        }
        return null;
    }

    @Override
    public void secondClick(final Click click) {
        final View button = overButton(click.getLocation());
        if (button == null) {
            super.secondClick(click);
        }
    }

    @Override
    public void setBounds(final Bounds bounds) {
        super.setBounds(bounds);
        layout(bounds.getWidth());
    }

    @Override
    public void setSize(final Size size) {
        super.setSize(size);
        layout(size.getWidth());
    }

    @Override
    public void thirdClick(final Click click) {
        final View button = overButton(click.getLocation());
        if (button == null) {
            super.thirdClick(click);
        }
    }

    private int totalButtonWidth() {
        int totalButtonWidth = 0;
        for (int i = 0; i < buttons.length; i++) {
            final int buttonWidth = buttons[i].getRequiredSize(new Size()).getWidth();
            totalButtonWidth += i > 0 ? BUTTON_SPACING : 0;
            totalButtonWidth += buttonWidth;
        }
        return totalButtonWidth;
    }

}
