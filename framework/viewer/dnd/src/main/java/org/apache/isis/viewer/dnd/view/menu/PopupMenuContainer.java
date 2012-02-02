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
import java.util.Enumeration;
import java.util.Vector;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.view.Click;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.KeyboardAction;
import org.apache.isis.viewer.dnd.view.MenuOptions;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.UserAction;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.base.AbstractView;
import org.apache.isis.viewer.dnd.view.content.NullContent;
import org.apache.isis.viewer.dnd.view.debug.DebugOption;

public class PopupMenuContainer extends AbstractView {
    private static final int MENU_OVERLAP = 4;
    private static final UserAction DEBUG_OPTION = new DebugOption();
    private PopupMenu menu;
    private PopupMenu submenu;
    private Color backgroundColor;
    private final View target;
    private final Vector options = new Vector();
    private final Location at;
    private boolean isLayoutInvalid;

    public PopupMenuContainer(final View target, final Location at) {
        super(new NullContent());
        this.target = target;
        this.at = at;
        setLocation(at);
        isLayoutInvalid = true;
    }

    @Override
    public void debug(final DebugBuilder debug) {
        super.debug(debug);
        debug.appendTitle("Submenu");
        debug.append(submenu);
        debug.append("\n");
    }

    @Override
    public void dispose() {
        if (getParent() == null) {
            super.dispose();
            getViewManager().clearOverlayView(this);
        } else {
            getParent().dispose();
        }
    }

    @Override
    public Size getRequiredSize(final Size availableSpace) {
        final Size size = menu.getRequiredSize(Size.createMax());
        if (submenu != null) {
            final Size subviewSize = submenu.getRequiredSize(Size.createMax());
            size.extendWidth(subviewSize.getWidth() - MENU_OVERLAP);
            size.ensureHeight(submenuOffset() + subviewSize.getHeight());
        }
        return size;
    }

    @Override
    public void layout() {
        if (isLayoutInvalid) {
            menu.layout();
            final Size menuSize = menu.getRequiredSize(Size.createMax());
            menu.setSize(menuSize);
            menu.setLocation(new Location(0, 0));

            final Location containerLocation = new Location(at);
            final Size bounds = getViewManager().getOverlaySize();
            if (containerLocation.getX() < 0) {
                containerLocation.setX(0);
            } else if (containerLocation.getX() + menuSize.getWidth() > bounds.getWidth()) {
                containerLocation.setX(bounds.getWidth() - menuSize.getWidth());
            }

            if (containerLocation.getY() < 0) {
                containerLocation.setY(0);
            } else if (containerLocation.getY() + menuSize.getHeight() > bounds.getHeight()) {
                containerLocation.setY(bounds.getHeight() - menuSize.getHeight());
            }

            if (submenu != null) {
                submenu.layout();
                final Size submenuSize = submenu.getRequiredSize(Size.createMax());
                submenu.setSize(submenuSize);

                int submenuOffset = submenuOffset();
                final Location menuLocation = new Location();

                final int containerBottom = containerLocation.getY() + submenuOffset + submenuSize.getHeight();
                if (containerBottom > bounds.getHeight()) {
                    final int overstretch = containerBottom - bounds.getHeight();
                    submenuOffset -= overstretch;
                }
                final Location submenuLocation = new Location(0, submenuOffset);

                final boolean placeToLeft = at.getX() + menuSize.getWidth() + submenuSize.getWidth() < getViewManager().getOverlaySize().getWidth();
                if (placeToLeft) {
                    submenuLocation.setX(menuSize.getWidth() - MENU_OVERLAP);
                } else {
                    menuLocation.setX(submenuSize.getWidth() - MENU_OVERLAP);
                    containerLocation.move(-submenu.getSize().getWidth() + MENU_OVERLAP, 0);
                }

                if (containerLocation.getY() + menuSize.getHeight() > bounds.getHeight()) {
                    containerLocation.setY(bounds.getHeight() - menuSize.getHeight());
                }

                submenu.setLocation(submenuLocation); // // !
                menu.setLocation(menuLocation); // / !

            }

            setLocation(containerLocation);

        }
    }

    private int submenuOffset() {
        return menu.getOptionPostion();
    }

    @Override
    public void mouseMoved(final Location at) {
        if (menu.getBounds().contains(at)) {
            at.subtract(menu.getLocation());
            menu.mouseMoved(at);
        } else if (submenu != null && submenu.getBounds().contains(at)) {
            at.subtract(submenu.getLocation());
            submenu.mouseMoved(at);
        }
    }

    public void show(final boolean forView, final boolean includeDebug, final boolean includeExploration, final boolean includePrototype) {
        final boolean withExploration = getViewManager().isRunningAsExploration() && includeExploration;
        final boolean withPrototype = getViewManager().isRunningAsPrototype() && includePrototype;

        final UserActionSet optionSet = new UserActionSetImpl(withExploration, withPrototype, includeDebug, ActionType.USER);
        if (forView) {
            target.viewMenuOptions(optionSet);
        } else {
            target.contentMenuOptions(optionSet);
        }
        optionSet.add(DEBUG_OPTION);
        final Enumeration e = options.elements();
        while (e.hasMoreElements()) {
            final MenuOptions element = (MenuOptions) e.nextElement();
            element.menuOptions(optionSet);
        }

        menu = new PopupMenu(this);

        backgroundColor = optionSet.getColor();
        menu.show(target, optionSet.getUserActions(), backgroundColor);
        getViewManager().setOverlayView(this);

        if (target != null) {
            final String status = changeStatus(target, forView, withExploration, includeDebug);
            getFeedbackManager().setViewDetail(status);
        }
    }

    private String changeStatus(final View over, final boolean forView, final boolean includeExploration, final boolean includeDebug) {
        final StringBuffer status = new StringBuffer("Menu for ");
        if (forView) {
            status.append("view ");
            status.append(over.getSpecification().getName());
        } else {
            status.append("object");
            final Content content = over.getContent();
            if (content != null) {
                status.append(" '");
                status.append(content.title());
                status.append("'");
            }

        }
        if (includeDebug || includeExploration) {
            status.append(" (includes ");
            if (includeExploration) {
                status.append("exploration");
            }
            if (includeDebug) {
                if (includeExploration) {
                    status.append(" & ");
                }
                status.append("debug");
            }
            status.append(" options)");
        }
        return status.toString();
    }

    public void addMenuOptions(final MenuOptions options) {
        this.options.addElement(options);
    }

    void openSubmenu(final UserAction[] options) {
        markDamaged();

        submenu = new PopupMenu(this);
        submenu.setParent(this);
        submenu.show(target, options, backgroundColor);
        invalidateLayout();
        final Size size = getRequiredSize(Size.createMax());
        setSize(size);
        layout();

        isLayoutInvalid = false;

        markDamaged();
    }

    @Override
    public void keyPressed(final KeyboardAction key) {
        if (submenu != null) {
            final int keyCode = key.getKeyCode();
            if (keyCode == KeyEvent.VK_ESCAPE) {
                markDamaged();
                invalidateLayout();
                submenu = null;
                key.consume();

            } else if (getParent() != null && keyCode == KeyEvent.VK_LEFT) {
                markDamaged();
                invalidateLayout();
                submenu = null;
                key.consume();
            } else {
                submenu.keyPressed(key);
            }
        } else {
            menu.keyPressed(key);
        }
    }

    @Override
    public void invalidateLayout() {
        isLayoutInvalid = true;
    }

    @Override
    public void draw(final Canvas canvas) {
        super.draw(canvas);
        if (menu != null) {
            final Canvas menuCanvas = canvas.createSubcanvas(menu.getBounds());
            menu.draw(menuCanvas);
        }
        if (submenu != null) {
            final Canvas submenuCanvas = canvas.createSubcanvas(submenu.getBounds());
            submenu.draw(submenuCanvas);
        }

        if (Toolkit.debug) {
            canvas.drawRectangleAround(getBounds(), Toolkit.getColor(ColorsAndFonts.COLOR_DEBUG_BOUNDS_VIEW));
        }
    }

    @Override
    public void firstClick(final Click click) {
        final Location location = click.getLocation();
        if (menu.getBounds().contains(location)) {
            click.subtract(menu.getLocation());
            menu.firstClick(click);
        } else if (submenu != null && submenu.getBounds().contains(location)) {
            click.subtract(submenu.getLocation());
            submenu.firstClick(click);
        }
    }

}
