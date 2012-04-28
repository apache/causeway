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

package org.apache.isis.viewer.dnd.viewer.basic;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.viewer.dnd.drawing.Canvas;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.drawing.Padding;
import org.apache.isis.viewer.dnd.drawing.Size;
import org.apache.isis.viewer.dnd.icon.SubviewIconSpecification;
import org.apache.isis.viewer.dnd.view.Axes;
import org.apache.isis.viewer.dnd.view.Click;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.ContentDrag;
import org.apache.isis.viewer.dnd.view.DragEvent;
import org.apache.isis.viewer.dnd.view.DragStart;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.UserAction;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewAreaType;
import org.apache.isis.viewer.dnd.view.ViewConstants;
import org.apache.isis.viewer.dnd.view.ViewRequirement;
import org.apache.isis.viewer.dnd.view.ViewSpecification;
import org.apache.isis.viewer.dnd.view.ViewState;
import org.apache.isis.viewer.dnd.view.Workspace;
import org.apache.isis.viewer.dnd.view.base.AbstractView;
import org.apache.isis.viewer.dnd.view.option.UserActionAbstract;
import org.apache.isis.viewer.dnd.view.window.WindowControl;

public class MinimizedView extends AbstractView {
    private class CloseWindowControl extends WindowControl {

        public CloseWindowControl(final View target) {
            super(new UserAction() {
                @Override
                public Consent disabled(final View view) {
                    return Allow.DEFAULT;
                }

                @Override
                public void execute(final Workspace workspace, final View view, final Location at) {
                    ((MinimizedView) view).close();
                }

                @Override
                public String getDescription(final View view) {
                    return "Close " + view.getSpecification().getName();
                }

                @Override
                public String getHelp(final View view) {
                    return null;
                }

                @Override
                public String getName(final View view) {
                    return "Close view";
                }

                @Override
                public ActionType getType() {
                    return ActionType.USER;
                }
            }, target);
        }

        @Override
        public void draw(final Canvas canvas) {
            final int x = 0;
            final int y = 0;
            final Color crossColor = Toolkit.getColor(ColorsAndFonts.COLOR_BLACK);
            canvas.drawLine(x + 4, y + 3, x + 10, y + 9, crossColor);
            canvas.drawLine(x + 5, y + 3, x + 11, y + 9, crossColor);
            canvas.drawLine(x + 10, y + 3, x + 4, y + 9, crossColor);
            canvas.drawLine(x + 11, y + 3, x + 5, y + 9, crossColor);
        }
    }

    private class RestoreWindowControl extends WindowControl {
        public RestoreWindowControl(final View target) {
            super(new UserAction() {

                @Override
                public Consent disabled(final View view) {
                    return Allow.DEFAULT;
                }

                @Override
                public void execute(final Workspace workspace, final View view, final Location at) {
                    ((MinimizedView) view).restore();
                }

                @Override
                public String getDescription(final View view) {
                    return "Restore " + view.getSpecification().getName() + " to normal size";
                }

                @Override
                public String getHelp(final View view) {
                    return null;
                }

                @Override
                public String getName(final View view) {
                    return "Restore view";
                }

                @Override
                public ActionType getType() {
                    return ActionType.USER;
                }
            }, target);
        }

        @Override
        public void draw(final Canvas canvas) {
            final int x = 0;
            final int y = 0;
            final Color black = Toolkit.getColor(ColorsAndFonts.COLOR_BLACK);
            canvas.drawRectangle(x + 1, y + 1, WIDTH - 1, HEIGHT - 1, black);
            canvas.drawLine(x + 2, y + 2, x + WIDTH - 2, y + 2, black);
            canvas.drawLine(x + 2, y + 3, x + WIDTH - 2, y + 3, black);
        }
    }

    private static class Specification implements ViewSpecification {

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
            return "minimized view";
        }

        @Override
        public boolean isAligned() {
            return false;
        }

        @Override
        public boolean isOpen() {
            return false;
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

    private final static int BORDER_WIDTH = 5;
    private final WindowControl controls[];
    private View iconView;

    private final View minimizedView;

    public MinimizedView(final View viewToMinimize) {
        super(viewToMinimize.getContent(), new Specification());
        this.minimizedView = viewToMinimize;
        iconView = new SubviewIconSpecification().createView(viewToMinimize.getContent(), viewToMinimize.getViewAxes(), -1);
        iconView.setParent(this);
        controls = new WindowControl[] { new RestoreWindowControl(this), new CloseWindowControl(this) };
    }

    @Override
    public void debug(final DebugBuilder debug) {
        super.debug(debug);
        debug.appendln("minimized view", minimizedView);
        debug.appendln();

        debug.appendln("icon size", iconView.getSize());
        debug.append(iconView);
    }

    @Override
    public void dispose() {
        super.dispose();
        iconView.dispose();
        // viewToMinimize.dispose();
    }

    @Override
    public DragEvent dragStart(final DragStart drag) {
        if (iconView.getBounds().contains(drag.getLocation())) {
            drag.subtract(BORDER_WIDTH, BORDER_WIDTH);
            return iconView.dragStart(drag);
        } else {
            return super.dragStart(drag);
        }
        // View dragOverlay = new DragViewOutline(getView());
        // return new ViewDrag(this, new Offset(drag.getLocation()),
        // dragOverlay);
    }

    @Override
    public void draw(final Canvas canvas) {
        super.draw(canvas);

        final Size size = getSize();
        final int width = size.getWidth();
        final int height = size.getHeight();
        final int left = 3;
        final int top = 3;

        final boolean hasFocus = containsFocus();
        final Color lightColor = hasFocus ? Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY1) : Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY2);
        clearBackground(canvas, Toolkit.getColor(ColorsAndFonts.COLOR_WINDOW));
        canvas.drawRectangle(1, 0, width - 2, height, lightColor);
        canvas.drawRectangle(0, 1, width, height - 2, lightColor);
        for (int i = 2; i < left; i++) {
            canvas.drawRectangle(i, i, width - 2 * i, height - 2 * i, lightColor);
        }
        final ViewState state = getState();
        if (state.isActive()) {
            final int i = left;
            canvas.drawRectangle(i, top, width - 2 * i, height - 2 * i - top, Toolkit.getColor(ColorsAndFonts.COLOR_ACTIVE));
        }

        final int bw = controls[0].getLocation().getX() - 3; // controls.length
                                                             // *
                                                             // WindowControl.WIDTH;
        canvas.drawSolidRectangle(bw, top, width - bw - 3, height - top * 2, Toolkit.getColor(ColorsAndFonts.COLOR_SECONDARY3));
        canvas.drawLine(bw - 1, top, bw - 1, height - top * 2, lightColor);

        for (int i = 0; controls != null && i < controls.length; i++) {
            final Canvas controlCanvas = canvas.createSubcanvas(controls[i].getBounds());
            controls[i].draw(controlCanvas);
        }

        final Canvas c = canvas.createSubcanvas(iconView.getBounds());
        iconView.draw(c);
    }

    @Override
    public Size getRequiredSize(final Size availableSpace) {
        final Size size = new Size();

        size.extendWidth(BORDER_WIDTH);
        final Size iconMaximumSize = iconView.getRequiredSize(Size.createMax());
        size.extendWidth(iconMaximumSize.getWidth());

        size.extendHeight(iconMaximumSize.getHeight());
        size.ensureHeight(WindowControl.HEIGHT);
        size.extendHeight(BORDER_WIDTH);
        size.extendHeight(BORDER_WIDTH);

        size.extendWidth(ViewConstants.HPADDING);
        size.extendWidth(controls.length * (WindowControl.WIDTH + ViewConstants.HPADDING));
        size.extendWidth(BORDER_WIDTH);
        return size;
    }

    @Override
    public Padding getPadding() {
        return new Padding(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH);
    }

    @Override
    public void layout() {
        final Size size = getRequiredSize(Size.createMax());

        layoutControls(size.getWidth());

        size.contractWidth(BORDER_WIDTH * 2);
        size.contractWidth(ViewConstants.HPADDING);
        size.contractWidth(controls.length * (WindowControl.WIDTH + ViewConstants.HPADDING));

        size.contractHeight(BORDER_WIDTH * 2);

        iconView.setLocation(new Location(BORDER_WIDTH, BORDER_WIDTH));
        iconView.setSize(size);
    }

    private void layoutControls(final int width) {
        final int widthControl = WindowControl.WIDTH + ViewConstants.HPADDING;
        int x = width - BORDER_WIDTH + ViewConstants.HPADDING;
        x -= widthControl * controls.length;
        final int y = BORDER_WIDTH;

        for (final WindowControl control : controls) {
            control.setSize(control.getRequiredSize(Size.createMax()));
            control.setLocation(new Location(x, y));
            x += widthControl;
        }
    }

    private void restore() {
        final Workspace workspace = getWorkspace();
        final View[] views = workspace.getSubviews();
        for (final View view : views) {
            if (view == this) {
                dispose();

                minimizedView.setParent(workspace);
                // workspace.removeView(this);
                workspace.addView(minimizedView);
                workspace.invalidateLayout();

                return;

            }
        }
    }

    private void close() {
        final Workspace workspace = getWorkspace();
        final View[] views = workspace.getSubviews();
        for (final View view : views) {
            if (view == this) {
                dispose();

                minimizedView.setParent(workspace);
                workspace.invalidateLayout();
                workspace.addView(minimizedView);
                minimizedView.dispose();

                return;

            }
        }
    }

    @Override
    public void removeView(final View view) {
        if (view == iconView) {
            iconView = null;
        } else {
            throw new IsisException("No view " + view + " in " + this);
        }
    }

    @Override
    public void secondClick(final Click click) {
        restore();
    }

    @Override
    public ViewAreaType viewAreaType(final Location location) {
        location.subtract(BORDER_WIDTH, BORDER_WIDTH);
        return iconView.viewAreaType(location);
    }

    @Override
    public void viewMenuOptions(final UserActionSet options) {
        options.add(new UserActionAbstract("Restore") {

            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                restore();
            }
        });
        super.viewMenuOptions(options);
    }

    @Override
    public void firstClick(final Click click) {
        final View button = overControl(click.getLocation());
        if (button == null) {
            /*
             * if (overBorder(click.getLocation())) { Workspace workspace =
             * getWorkspace(); if (workspace != null) { if (click.button2()) {
             * workspace.lower(getView()); } else if (click.button1()) {
             * workspace.raise(getView()); } } } else { super.firstClick(click);
             * }
             */} else {
            button.firstClick(click);
        }

    }

    private View overControl(final Location location) {
        for (final WindowControl control : controls) {
            if (control.getBounds().contains(location)) {
                return control;
            }
        }
        return null;
    }

    @Override
    public void dragIn(final ContentDrag drag) {
        if (iconView.getBounds().contains(drag.getTargetLocation())) {
            drag.subtract(BORDER_WIDTH, BORDER_WIDTH);
            iconView.dragIn(drag);
        }
    }

    @Override
    public void dragOut(final ContentDrag drag) {
        if (iconView.getBounds().contains(drag.getTargetLocation())) {
            drag.subtract(BORDER_WIDTH, BORDER_WIDTH);
            iconView.dragOut(drag);
        }
    }

    @Override
    public View identify(final Location location) {
        if (iconView.getBounds().contains(location)) {
            location.subtract(BORDER_WIDTH, BORDER_WIDTH);
            return iconView.identify(location);
        }
        return this;
    }

    @Override
    public void drop(final ContentDrag drag) {
        if (iconView.getBounds().contains(drag.getTargetLocation())) {
            drag.subtract(BORDER_WIDTH, BORDER_WIDTH);
            iconView.drop(drag);
        }
    }
}
