package org.nakedobjects.viewer.skylark.core;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.utility.DebugString;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Image;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.MenuOptionSet;
import org.nakedobjects.viewer.skylark.Padding;
import org.nakedobjects.viewer.skylark.PopupMenu;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.UserAction;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.basic.AbstractContent;

import java.awt.event.KeyEvent;
import java.util.Vector;

import org.apache.log4j.Logger;


public class DefaultPopupMenu extends AbstractView implements PopupMenu {

    private static class Item {
        UserAction action;
        String description;
        boolean isBlank;
        boolean isDisabled;
        String name;
        String reason;

        private Item() {}

        public String toString() {
            return isBlank ? "NONE" : (name + " " + (isDisabled ? "DISABLED " : " " + action));
        }

        public static Item createNoOption() {
            Item item = new Item();
            item.name = "no option";
            return item;
        }

        public static Item createOption(UserAction action, Object object, View view, Location location) {
            Item item = new Item();
            if (action == null) {
                item.isBlank = true;
            } else {
                item.isBlank = false;
                item.action = action;
                item.name = action.getName(view);
                item.description = action.getDescription(view);
                item.isDisabled = action.disabled(view).isVetoed();
                item.reason = action.disabled(view).getReason();
            }
            return item;
        }
    }

    private class PopupContent extends AbstractContent {

        public PopupContent() {}

        public Consent canDrop(Content sourceContent) {
            return Veto.DEFAULT;
        }

        public void debugDetails(DebugString debug) {}

        public Naked drop(Content sourceContent) {
            return null;
        }

        public String getIconName() {
            return null;
        }

        public Image getIconPicture(int iconHeight) {
            return null;
        }

        public Naked getNaked() {
            return null;
        }

        public NakedObjectSpecification getSpecification() {
            return null;
        }

        public boolean isTransient() {
            return false;
        }

        public void parseTextEntry(String entryText) throws InvalidEntryException {}

        public String title() {
            int optionNo = getOption();
            return items[optionNo].name;
        }

        public String getDescription() {
            int optionNo = getOption();
            return items[optionNo].description;
        }

        public String getId() {
            return null;
        }
    }

    private static final UserAction DEBUG_VIEW_OPTION = new DebugViewOption();

    private static final Logger LOG = Logger.getLogger(DefaultPopupMenu.class);
    private Color backgroundColor;
    private View forView;
    private Item[] items = new Item[0];
    private int optionIdentified;

    public DefaultPopupMenu() {
        super(null, null, null);
        setContent(new PopupContent());
    }

    protected Color backgroundColor() {
        return backgroundColor;
    }

    public boolean canChangeValue() {
        return false;
    }

    public boolean canFocus() {
        return true;
    }

    protected Color disabledColor() {
        return Style.DISABLED_MENU;
    }

    /**
     * Draws the popup menu
     * 
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    public void draw(Canvas canvas) {
        int width = getSize().getWidth();
        int height = getSize().getHeight();
        canvas.drawSolidRectangle(0, 0, width, height, backgroundColor);
        canvas.draw3DRectangle(0, 0, width, height, backgroundColor, true);

        int itemHeight = style().getLineHeight() + VPADDING;
        int baseLine = itemHeight / 2 + style().getAscent() / 2 +getPadding().getTop();
        int left = getPadding().getLeft();
        for (int i = 0; i < items.length; i++) {
            if (items[i].isBlank) {
                int y = baseLine - (style().getAscent() / 2);
                canvas.drawLine(1, y, width - 2, y, backgroundColor.brighter());
                canvas.drawLine(1, y - 1, width - 2, y - 1, backgroundColor.darker());
            } else {
                Color color;
                if (items[i].isDisabled || items[i].action == null) {
                    color = disabledColor();
                } else if (getOption() == i) {
                    int top = getPadding().getTop() + i * itemHeight;
                    int depth = style().getLineHeight() + 2;
                    canvas.drawSolidRectangle(2, top, width - 4, depth, backgroundColor.darker());
                    color = reversedColor();
                } else {
                    color = normalColor();
                }
                canvas.drawText(items[i].name, left, baseLine, color, style());
            }

            baseLine += itemHeight;
        }
    }

    public void editComplete() {}

    public void firstClick(Click click) {
        if (click.button1()) {
            mouseMoved(click.getLocation());
            invoke();
        }
    }

    public void focusLost() {}

    public void focusReceived() {}

    public int getOption() {
        return optionIdentified;
    }

    public int getOptionCount() {
        return items.length;
    }

    public Padding getPadding() {
        Padding in = super.getPadding();
        in.extendTop(VPADDING);
        in.extendBottom(VPADDING);
        in.extendLeft(HPADDING + 5);
        in.extendRight(HPADDING + 5);

        return in;
    }

    public Size getRequiredSize() {
        Size size = new Size();

        for (int i = 0; i < items.length; i++) {
            int itemWidth = items[i].isBlank ? 0 : style().stringWidth(items[i].name);
            size.ensureWidth(itemWidth);
            size.extendHeight(style().getLineHeight() + VPADDING);
        }

        size.extend(getPadding());
        size.extendWidth(HPADDING * 2);

        return size;
    }

    public Workspace getWorkspace() {
        return forView.getWorkspace();
    }

    public boolean hasFocus() {
        return false;
    }

    public void init(View over, View parent, Location mouseAt, boolean forView, boolean includeExploration, boolean includeDebug) {
        this.forView = over;

        getViewManager().saveCurrentFieldEntry();

        MenuOptionSet optionSet = new MenuOptionSet(forView);
        if (forView) {
            over.viewMenuOptions(optionSet);
        } else {
            over.contentMenuOptions(optionSet);
        }
        optionSet.add(MenuOptionSet.DEBUG, DEBUG_VIEW_OPTION);
        optionIdentified = 0;
        backgroundColor = optionSet.getColor();

        Vector options = optionSet.getMenuOptions(includeExploration, includeDebug);
        int len = options.size();
        if (len == 0) {
            items = new Item[] { Item.createNoOption() };
        } else {
            items = new Item[len];
            for (int i = 0; i < len; i++) {
                items[i] = Item.createOption((UserAction) options.elementAt(i), null, over, getLocation());
            }
        }
    }

    private void invoke() {
        int option = getOption();

        Workspace workspace = getWorkspace();
        Item item = items[option];

        Location location = new Location(getAbsoluteLocation());
        location.subtract(workspace.getView().getAbsoluteLocation());
        Padding padding = workspace.getView().getPadding();
        location.move(-padding.getLeft(), -padding.getTop());
        location.move(30, 0);

        dispose();
        if (!item.isBlank && item.action != null && item.action.disabled(forView).isAllowed()) {
            showStatus("Executing " + item);
            LOG.debug("execute " + item.name + " on " + forView + " in " + workspace);
            item.action.execute(workspace, forView, location);
            showStatus("");
        }
    }

    public void keyPressed(int keyCode, int modifiers) {
        if (keyCode == KeyEvent.VK_ESCAPE) {
            dispose();

        } else if (keyCode == KeyEvent.VK_ENTER) {
            invoke();

        } else if (keyCode == KeyEvent.VK_UP) {
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

    public void keyReleased(int keyCode, int modifiers) {}

    public void keyTyped(char keyCode) {}

    public View makeView(Naked object, NakedObjectField field) throws CloneNotSupportedException {
        throw new RuntimeException();
    }

    public void markDamaged() {
        super.markDamaged();
    }

    public void mouseMoved(Location at) {
        int option = (at.getY() - getPadding().getTop()) / (style().getLineHeight() + VPADDING);
        option = Math.max(option, 0);
        option = Math.min(option, items.length - 1);
        if (option >= 0 && optionIdentified != option) {
            setOption(option);
            getViewManager().forceRepaint();
            markDamaged();
        }
    }

    protected Color normalColor() {
        return Style.NORMAL_MENU;
    }

    protected Color reversedColor() {
        return Style.REVERSE_MENU;
    }

    public void setOption(int option) {
        if (option != optionIdentified) {
            optionIdentified = option;
            Item item = items[optionIdentified];
            if (item.isBlank) {
                showStatus("");
            } else if (item.reason == "") {
                showStatus(item.description == null ? "" : item.description);
            } else {
                showStatus(item.reason);
            }
            markDamaged();
        }
    }

    protected void showStatus(String status) {
        getViewManager().setStatus(status);
    }

    protected Text style() {
        return Style.MENU;
    }

    public String toString() {
        return "PopupMenu [location=" + getLocation() + ",item=" + optionIdentified + ",itemCount="
                + (items == null ? 0 : items.length) + "]";
    }

    protected boolean transparentBackground() {
        return false;
    }

    public void viewMenuOptions(MenuOptionSet options) {
        options.add(MenuOptionSet.DEBUG, DEBUG_VIEW_OPTION);
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */