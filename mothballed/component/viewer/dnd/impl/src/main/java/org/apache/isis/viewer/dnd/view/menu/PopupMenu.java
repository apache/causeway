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

package org.apache.isis.viewer.dnd.view.menu;

import java.awt.event.KeyEvent;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.Veto;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Image;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Padding;
import org.apache.isis.viewer.dnd.drawing.Shape;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.drawing.Text;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Click;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.FocusManager;
import org.apache.isis.viewer.dnd.view.KeyboardAction;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.UserAction;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewConstants;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.Workspace;
import org.apache.isis.viewer.dnd.view.base.AbstractView;
import org.apache.isis.viewer.dnd.view.content.AbstractContent;
import org.apache.isis.viewer.dnd.view.content.NullContent;
import org.apache.isis.viewer.dnd.view.window.SubviewFocusManager;

public class PopupMenu extends AbstractView {

    private static class Item {
        public static Item createDivider() {
            final Item item = new Item();
            item.isBlank = true;
            return item;
        }

        public static Item createNoOption() {
            final Item item = new Item();
            item.name = "no options";
            return item;
        }

        public static Item createOption(final UserAction action, final Object object, final View view, final Location location) {
            final Item item = new Item();
            if (action == null) {
                item.isBlank = true;
            } else {
                item.isBlank = false;
                item.action = action;
                item.view = view;
                item.name = action.getName(view);
                item.description = action.getDescription(view);
                final Consent consent = action.disabled(view);
                item.isDisabled = consent.isVetoed();
                item.reasonDisabled = consent.getReason();
            }
            return item;
        }

        UserAction action;
        String description;
        boolean isBlank;
        boolean isDisabled;
        String name;
        String reasonDisabled;
        View view;

        private Item() {
        }

        public String getHelp() {
            return action.getHelp(view);
        }

        @Override
        public String toString() {
            return isBlank ? "NONE" : (name + " " + (isDisabled ? "DISABLED " : " " + action));
        }
    }

    private class PopupContent extends AbstractContent {

        public PopupContent() {
        }

        @Override
        public Consent canDrop(final Content sourceContent) {
            return Veto.DEFAULT;
        }

        @Override
        public void debugDetails(final DebugBuilder debug) {
        }

        @Override
        public ObjectAdapter drop(final Content sourceContent) {
            return null;
        }

        @Override
        public String getDescription() {
            final int optionNo = getOption();
            return items[optionNo].description;
        }

        @Override
        public String getHelp() {
            final int optionNo = getOption();
            return items[optionNo].getHelp();
        }

        @Override
        public String getIconName() {
            return null;
        }

        @Override
        public Image getIconPicture(final int iconHeight) {
            return null;
        }

        @Override
        public String getId() {
            return null;
        }

        @Override
        public ObjectAdapter getAdapter() {
            return null;
        }

        @Override
        public boolean isOptionEnabled() {
            return false;
        }

        @Override
        public ObjectSpecification getSpecification() {
            return null;
        }

        @Override
        public boolean isTransient() {
            return false;
        }

        public void parseTextEntry(final String entryText) {
        }

        @Override
        public String title() {
            final int optionNo = getOption();
            return items[optionNo].name;
        }

        @Override
        public ObjectAdapter[] getOptions() {
            return null;
        }
    }

    private static class PopupSpecification implements ViewSpecification {
        @Override
        public boolean canDisplay(final ViewRequirement requirement) {
            return false;
        }

        @Override
        public View createView(final Content content, final Axes axes, final int sequence) {
            return null;
        }

        @Override
        public String getName() {
            return "Popup Menu";
        }

        @Override
        public boolean isAligned() {
            return false;
        }

        @Override
        public boolean isOpen() {
            return true;
        }

        @Override
        public boolean isReplaceable() {
            return false;
        }

        @Override
        public boolean isResizeable() {
            return false;
        }

        @Override
        public boolean isSubView() {
            return false;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(PopupMenu.class);
    private Color backgroundColor;
    private View forView;
    private Item[] items = new Item[0];
    private int optionIdentified;
    private final FocusManager simpleFocusManager;

    public PopupMenu(final PopupMenuContainer parent) {
        super(new NullContent(), new PopupSpecification());
        // REVIEW should this content be used as param 1 above?
        setContent(new PopupContent());
        setParent(parent);
        simpleFocusManager = new SubviewFocusManager(this);
    }

    private void addItems(final View target, final UserAction[] options, final int len, final Vector<Item> list, final ActionType type) {
        final int initialSize = list.size();
        for (int i = 0; i < len; i++) {
            if (options[i].getType() == type) {
                if (initialSize > 0 && list.size() == initialSize) {
                    list.addElement(Item.createDivider());
                }
                list.addElement(Item.createOption(options[i], null, target, getLocation()));
            }
        }
    }

    protected Color backgroundColor() {
        return backgroundColor;
    }

    @Override
    public Consent canChangeValue() {
        return Veto.DEFAULT;
    }

    @Override
    public boolean canFocus() {
        return true;
    }

    protected Color disabledColor() {
        return Toolkit.getColor(ColorsAndFonts.COLOR_MENU_DISABLED);
    }

    /**
     * Draws the popup menu
     * 
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    @Override
    public void draw(final Canvas canvas) {
        final Size coreSize = getSize();
        final int width = coreSize.getWidth();
        final int height = coreSize.getHeight();
        canvas.drawSolidRectangle(0, 0, width, height, backgroundColor);
        canvas.draw3DRectangle(0, 0, width, height, backgroundColor, true);

        final int itemHeight = style().getLineHeight() + ViewConstants.VPADDING;
        // int baseLine = itemHeight / 2 + style().getAscent() / 2 +
        // getPadding().getTop();
        int baseLine = style().getAscent() + getPadding().getTop() + 1;
        final int left = getPadding().getLeft();
        for (int i = 0; i < items.length; i++) {
            if (items[i].isBlank) {
                final int y = baseLine - (style().getAscent() / 2);
                canvas.drawLine(1, y, width - 2, y, backgroundColor.brighter());
                canvas.drawLine(1, y - 1, width - 2, y - 1, backgroundColor.darker());
            } else {
                Color color;
                if (items[i].isDisabled || items[i].action == null) {
                    color = disabledColor();
                } else if (getOption() == i) {
                    final int top = getPadding().getTop() + i * itemHeight;
                    final int depth = style().getLineHeight() + 2;
                    canvas.drawSolidRectangle(2, top, width - 4, depth, backgroundColor.darker());
                    canvas.draw3DRectangle(2, top, width - 4, depth + 1, backgroundColor.brighter(), false);
                    // canvas.drawText(items[i].name, left, baseLine,
                    // normalColor(), style());

                    color = reversedColor();
                } else {
                    color = normalColor();
                }
                canvas.drawText(items[i].name, left, baseLine, color, style());
                if (items[i].action instanceof UserActionSet) {
                    Shape arrow;
                    arrow = new Shape(0, 0);
                    arrow.addVector(4, 4);
                    arrow.addVector(-4, 4);
                    canvas.drawSolidShape(arrow, width - 10, baseLine - 8, color);
                }
            }

            baseLine += itemHeight;
        }

        // canvas.drawRectangleAround(this,
        // Toolkit.getColor(ColorsAndFonts.COLOR_DEBUG_BOUNDS_VIEW));
    }

    @Override
    public void firstClick(final Click click) {
        if (click.button1() || click.button2()) {
            mouseMoved(click.getLocation());
            invoke();
        }
    }

    @Override
    public void focusLost() {
    }

    @Override
    public void focusReceived() {
    }

    @Override
    public FocusManager getFocusManager() {
        return simpleFocusManager;
    }

    @Override
    public Size getRequiredSize(final Size availableSpace) {
        final Size size = new Size();

        for (final Item item : items) {
            final int itemWidth = item.isBlank ? 0 : style().stringWidth(item.name);
            size.ensureWidth(itemWidth);
            size.extendHeight(style().getLineHeight() + ViewConstants.VPADDING);
        }

        size.extend(getPadding());
        size.extendWidth(ViewConstants.HPADDING * 2);
        return size;
    }

    public int getOption() {
        return optionIdentified;
    }

    public int getOptionPostion() {
        final int itemHeight = style().getLineHeight() + ViewConstants.VPADDING;
        return itemHeight * getOption();
    }

    public int getOptionCount() {
        return items.length;
    }

    @Override
    public Padding getPadding() {
        final Padding in = super.getPadding();
        in.extendTop(ViewConstants.VPADDING);
        in.extendBottom(ViewConstants.VPADDING);
        in.extendLeft(ViewConstants.HPADDING + 5);
        in.extendRight(ViewConstants.HPADDING + 5);

        return in;
    }

    @Override
    public Workspace getWorkspace() {
        return forView.getWorkspace();
    }

    @Override
    public boolean hasFocus() {
        return false;
    }

    private void invoke() {
        final int option = getOption();
        final Item item = items[option];
        if (item.isBlank || item.action == null || item.action.disabled(forView).isVetoed()) {
            return;

        } else if (item.action instanceof UserActionSet) {
            final UserAction[] menuOptions = ((UserActionSet) item.action).getUserActions();
            ((PopupMenuContainer) getParent()).openSubmenu(menuOptions);
        } else {
            final Workspace workspace = getWorkspace();

            final Location location = new Location(getAbsoluteLocation());
            location.subtract(workspace.getView().getAbsoluteLocation());
            final Padding padding = workspace.getView().getPadding();
            location.move(-padding.getLeft(), -padding.getTop());

            final int itemHeight = style().getLineHeight() + ViewConstants.VPADDING;
            final int baseLine = itemHeight * option;
            location.add(0, baseLine);

            getParent().dispose();
            LOG.debug("execute " + item.name + " on " + forView + " in " + workspace);
            item.action.execute(workspace, forView, location);
        }
    }

    @Override
    public void keyPressed(final KeyboardAction key) {
        final int keyCode = key.getKeyCode();

        if (keyCode == KeyEvent.VK_ESCAPE) {
            if (getParent() == null) {
                dispose();
            }

            key.consume();

        } else if (keyCode == KeyEvent.VK_ENTER) {
            key.consume();
            invoke();

        } else if (keyCode == KeyEvent.VK_RIGHT && items[getOption()].action instanceof UserActionSet) {
            key.consume();
            invoke();

        } else if (keyCode == KeyEvent.VK_UP) {
            key.consume();
            if (optionIdentified == 0) {
                optionIdentified = items.length;
            }

            for (int i = optionIdentified - 1; i >= 0; i--) {
                if (items[i].isBlank) {
                    continue;
                }
                if (items[i].isDisabled) {
                    continue;
                }
                setOption(i);
                break;
            }

        } else if (keyCode == KeyEvent.VK_DOWN) {
            key.consume();
            if (optionIdentified == items.length - 1) {
                optionIdentified = -1;
            }

            for (int i = optionIdentified + 1; i < items.length; i++) {
                if (items[i].isBlank) {
                    continue;
                }
                if (items[i].isDisabled) {
                    continue;
                }
                setOption(i);
                break;
            }
        }

    }

    @Override
    public void keyReleased(final KeyboardAction action) {
    }

    @Override
    public void keyTyped(final KeyboardAction action) {
    }

    /*
     * @Override public void layout(final Size maximumSize) { coreSize = new
     * Bounds(getCoreRequiredSize());
     * 
     * final int option = getOption(); final int itemHeight =
     * style().getLineHeight() + VPADDING; int menuWidth = coreSize.getWidth();
     * // Location menuLocation = new Location(menuWidth - 4, itemHeight *
     * option); Location menuLocation = new Location(0, itemHeight * option);
     * 
     * if (submenu != null) { submenu.layout(maximumSize);
     * submenu.setLocation(menuLocation);
     * 
     * //coreSize.setX(submenu.getSize().getWidth() - 4); //getLocation() }
     * setSize(getMaximumSize()); }
     */
    public View makeView(final ObjectAdapter object, final ObjectAssociation field) throws CloneNotSupportedException {
        throw new RuntimeException();
    }

    @Override
    public void markDamaged() {
        if (getParent() == null) {
            super.markDamaged();
        } else {
            getParent().markDamaged();
        }
        // markDamaged(new Bounds(getAbsoluteLocation(), getSize()));
        // ///getView().getBounds());
    }

    @Override
    public void mouseMoved(final Location at) {
        int option = (at.getY() - getPadding().getTop()) / (style().getLineHeight() + ViewConstants.VPADDING);
        option = Math.max(option, 0);
        option = Math.min(option, items.length - 1);
        if (option >= 0 && optionIdentified != option) {
            // LOG.debug("mouse over option " + option + " " + this);
            setOption(option);
            markDamaged();
        }
    }

    protected Color normalColor() {
        return Toolkit.getColor(ColorsAndFonts.COLOR_MENU);
    }

    protected Color reversedColor() {
        return Toolkit.getColor(ColorsAndFonts.COLOR_MENU_REVERSED);
    }

    public void setOption(final int option) {
        if (option != optionIdentified) {
            optionIdentified = option;
            markDamaged();
            updateFeedback();
        }
    }

    private void updateFeedback() {
        final Item item = items[optionIdentified];
        if (item.isBlank) {
            getFeedbackManager().clearAction();
        } else if (isEmpty(item.reasonDisabled)) {
            getFeedbackManager().setAction(item.description == null ? "" : item.description);
        } else {
            getFeedbackManager().setAction(item.reasonDisabled);
        }
    }

    private boolean isEmpty(final String str) {
        return str == null || str.length() == 0;
    }

    void show(final View target, final UserAction[] options, final Color color) {
        this.forView = target;

        optionIdentified = 0;
        backgroundColor = color;

        final int len = options.length;
        if (len == 0) {
            items = new Item[] { Item.createNoOption() };
        } else {
            final Vector list = new Vector();
            addItems(target, options, len, list, ActionType.USER);
            addItems(target, options, len, list, ActionType.EXPLORATION);
            addItems(target, options, len, list, ActionType.PROTOTYPE);
            addItems(target, options, len, list, ActionType.DEBUG);
            items = new Item[list.size()];
            list.copyInto(items);
        }

        updateFeedback();
    }

    protected Text style() {
        return Toolkit.getText(ColorsAndFonts.TEXT_MENU);
    }

    @Override
    public String toString() {
        return "PopupMenu [location=" + getLocation() + ",item=" + optionIdentified + ",itemCount=" + (items == null ? 0 : items.length) + "]";
    }

    protected boolean transparentBackground() {
        return false;
    }
}
