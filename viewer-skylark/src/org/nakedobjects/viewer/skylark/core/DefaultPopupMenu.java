package org.nakedobjects.viewer.skylark.core;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Color;
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

        Item() {
            name = "no option";
        }

        Item(UserAction action, Workspace workspace, View view, Location at) {
            if (action == null) {
                isBlank = true;
            } else {
                isBlank = false;
                this.action = action;
                name = action.getName(view);
                description = action.getDescription(view);
                isDisabled = action.disabled(view).isVetoed();
                reason = action.disabled(view).getReason();
            }
        }

        public String toString() {
            return isBlank ? "NONE" : (name + " " + (isDisabled ? "DISABLED " : " " + action));
        }
    }

    private static final UserAction DEBUG_OPTION = new DebugOption();

    private static final Logger LOG = Logger.getLogger(DefaultPopupMenu.class);
    private Color backgroundColor;
    private View forView;
    private Item[] items = new Item[0];
    private int optionIdentified;

    public DefaultPopupMenu() {
        super(null, null, null);
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
        super.draw(canvas);

        int width = getSize().getWidth();
        int height = getSize().getHeight();
        canvas.drawSolidRectangle(0, 0, width - 1, height - 1, backgroundColor);
        canvas.draw3DRectangle(0, 0, width - 1, height - 1, true);

        int baseLine = style().getAscent() + VPADDING;
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
                    canvas.drawSolidRectangle(2, baseLine - style().getAscent(), width - 4, style().getTextHeight() + 2,
                            backgroundColor.darker());
                    color = reversedColor();
                } else {
                    color = normalColor();
                }

                canvas.drawText(items[i].name, left, baseLine, color, style());
            }

            baseLine += style().getTextHeight() + VPADDING;
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

    public void focusRecieved() {}

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
            size.extendHeight(style().getTextHeight() + VPADDING);
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
        optionSet.add(MenuOptionSet.DEBUG, DEBUG_OPTION);
        optionIdentified = 0;
        backgroundColor = optionSet.getColor();

        Vector options = optionSet.getMenuOptions(includeExploration, includeDebug);
        int len = options.size();
        if (len == 0) {
            items = new Item[] { new Item() };
        } else {
            items = new Item[len];

            for (int i = 0; i < len; i++) {
                items[i] = new Item((UserAction) options.elementAt(i), null, over, getLocation());
            }
        }

        Size size = getRequiredSize();
        setSize(size);
        Location location = new Location(mouseAt);
        location.move(-14, -10);
        setLocation(location);
        limitBoundsWithin(getViewManager().getOverlayBounds());
    }

    public void invoke() {
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

    public void mouseMoved(Location at) {
        int option = at.getY() / (style().getTextHeight() + VPADDING);
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
        options.add(MenuOptionSet.DEBUG, DEBUG_OPTION);
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */