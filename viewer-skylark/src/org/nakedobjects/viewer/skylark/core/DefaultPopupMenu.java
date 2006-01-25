package org.nakedobjects.viewer.skylark.core;

import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.Action.Type;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.utility.DebugString;
import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.FocusManager;
import org.nakedobjects.viewer.skylark.Image;
import org.nakedobjects.viewer.skylark.KeyboardAction;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.SimpleFocusManager;
import org.nakedobjects.viewer.skylark.UserActionSet;
import org.nakedobjects.viewer.skylark.Padding;
import org.nakedobjects.viewer.skylark.Shape;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.Style;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.UserAction;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.basic.AbstractContent;
import org.nakedobjects.viewer.skylark.basic.NullView;

import java.awt.event.KeyEvent;
import java.util.Vector;

import org.apache.log4j.Logger;


public class DefaultPopupMenu extends AbstractView {
    private static class Item {
        public static Item createDivider() {
            Item item = new Item();
            item.isBlank = true;
            return item;
        }

        public static Item createNoOption() {
            Item item = new Item();
            item.name = "no options";
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

        public String getDescription() {
            int optionNo = getOption();
            return items[optionNo].description;
        }

        public String getIconName() {
            return null;
        }

        public Image getIconPicture(int iconHeight) {
            return null;
        }

        public String getId() {
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
    }

    private static class PopupSpecification implements ViewSpecification {
        public boolean canDisplay(Content content) {
            return false;
        }

        public View createView(Content content, ViewAxis axis) {
            return null;
        }

        public String getName() {
            return "Popup Menu";
        }

        public boolean isOpen() {
            return true;
        }

        public boolean isReplaceable() {
            return false;
        }

        public boolean isSubView() {
            return false;
        }
    }

    private static final Logger LOG = Logger.getLogger(DefaultPopupMenu.class);
    private Color backgroundColor;
    private Bounds coreSize;
    private View forView;
    private Item[] items = new Item[0];
    private int optionIdentified;
    private View submenu = new NullView();
    private SimpleFocusManager simpleFocusManager;

    public DefaultPopupMenu() {
        super(null, new PopupSpecification(), null);
        setContent(new PopupContent());
        simpleFocusManager = new SimpleFocusManager();
        simpleFocusManager.setFocus(this);
    }

    private void addItems(View target, UserAction[] options, int len, Vector list, Type type) {
        int initialSize = list.size();
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

    public boolean canChangeValue() {
        return false;
    }

    public boolean canFocus() {
        return true;
    }

    public String debugDetails() {
        DebugString b = new DebugString();
        b.append(super.debugDetails());
        b.appendTitle("Submenu");
        b.append(submenu);
        b.append("\n");

        return b.toString();
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
        // canvas.drawRectangleAround(this, Color.DEBUG_VIEW_BOUNDS);
        int width = coreSize.getWidth();
        int height = coreSize.getHeight();
        canvas.drawSolidRectangle(0, 0, width, height, backgroundColor);
        canvas.draw3DRectangle(0, 0, width, height, backgroundColor, true);

        int itemHeight = style().getLineHeight() + VPADDING;
        int baseLine = itemHeight / 2 + style().getAscent() / 2 + getPadding().getTop();
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
                if (items[i].action instanceof UserActionSet) {
                    // canvas.drawRectangle(width - 10, 2, 6, 6, Style.SECONDARY2);

                    Shape arrow;
                    arrow = new Shape(0, 0);
                    arrow.extendsLine(4, 4);
                    arrow.extendsLine(-4, 4);
                    canvas.drawSolidShape(arrow, width - 10, baseLine - 8, color);
                }
            }

            baseLine += itemHeight;
        }

        Canvas submenuCanvas = canvas.createSubcanvas(submenu.getBounds());
        LOG.debug(submenu.getBounds());
        submenu.draw(submenuCanvas);
    }

    public void editComplete() {}

    public void firstClick(Click click) {
        if (coreSize.contains(click.getLocation())) {
            if (click.button1()) {
                mouseMoved(click.getLocation());
                invoke();
            }
        } else {
            click.subtract(submenu.getLocation());
            submenu.firstClick(click);
        }
    }

    public void focusLost() {}

    public void focusReceived() {}

    private Size getCoreRequiredSize() {
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
        Size size = getCoreRequiredSize();

        Size subviewSize = submenu.getRequiredSize();
        size.extendWidth(subviewSize.getWidth());
        size.ensureHeight(subviewSize.getHeight());

        return size;
    }

    public Workspace getWorkspace() {
        return forView.getWorkspace();
    }

    public FocusManager getFocusManager() {
        return simpleFocusManager;
    }
    
    public boolean hasFocus() {
        return false;
    }

    public void init(View target, UserAction[] options, Color color) {
        this.forView = target;

        optionIdentified = 0;
        backgroundColor = color;

        int len = options.length;
        if (len == 0) {
            items = new Item[] { Item.createNoOption() };
        } else {
            Vector list = new Vector();
            addItems(target, options, len, list, UserAction.USER);
            addItems(target, options, len, list, UserAction.EXPLORATION);
            addItems(target, options, len, list, UserAction.DEBUG);
            items = new Item[list.size()];
            list.copyInto(items);
        }
    }

    private void invoke() {
        int option = getOption();
        Item item = items[option];
        if (item.isBlank || item.action == null || item.action.disabled(forView).isVetoed()) {
            return;

        } else if (item.action instanceof UserActionSet) {
            /*
             * MenuContainer menuContainer = (MenuContainer) getParent(); int itemHeight =
             * style().getLineHeight() + VPADDING; Location at = new Location(getSize().getWidth() - 8,
             * itemHeight * option); menuContainer.add((MenuOptionSet) item.action, at );
             */
        } else {
            Workspace workspace = getWorkspace();

            Location location = new Location(getAbsoluteLocation());
            location.subtract(workspace.getView().getAbsoluteLocation());
            Padding padding = workspace.getView().getPadding();
            location.move(-padding.getLeft(), -padding.getTop());
            location.move(30, 0);

            dispose();
            // TODO this if is superflous; see first if in this method
            if (!item.isBlank && item.action != null && item.action.disabled(forView).isAllowed()) {
                showStatus("Executing " + item);
                LOG.debug("execute " + item.name + " on " + forView + " in " + workspace);
                item.action.execute(workspace, forView, location);
                showStatus("");
            }
        }
    }

    public void keyPressed(KeyboardAction key) {
        int keyCode = key.getKeyCode();
        
        if (keyCode == KeyEvent.VK_ESCAPE) {
            key.consume();
            dispose();

        } else if (keyCode == KeyEvent.VK_ENTER) {
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

    public void keyReleased(int keyCode, int modifiers) {}

    public void keyTyped(char keyCode) {}

    public void layout() {
        submenu.layout();
        coreSize = new Bounds(getCoreRequiredSize());
        setSize(getRequiredSize());
    }

    public View makeView(Naked object, NakedObjectField field) throws CloneNotSupportedException {
        throw new RuntimeException();
    }

    public void markDamaged() {
        super.markDamaged();
    }

    public void mouseMoved(Location at) {
        if (coreSize.contains(at)) {
            int option = (at.getY() - getPadding().getTop()) / (style().getLineHeight() + VPADDING);
            option = Math.max(option, 0);
            option = Math.min(option, items.length - 1);
            if (option >= 0 && optionIdentified != option) {
                setOption(option);

                Item item = items[option];
                if (item.action instanceof UserActionSet) {
                    int itemHeight = style().getLineHeight() + VPADDING;
                    Location menuLocation = new Location(coreSize.getWidth() - 8, itemHeight * option);
                    submenu = new DefaultPopupMenu();
                    ((DefaultPopupMenu) submenu).init(forView, ((UserActionSet) item.action).getMenuOptions(), backgroundColor);
                    submenu.setLocation(menuLocation);
                    invalidateLayout();
                } else {
                    submenu = new NullView();
                }

                getViewManager().forceRepaint();
                markDamaged();
            }
        } else {
            at.subtract(submenu.getLocation());
            submenu.mouseMoved(at);
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