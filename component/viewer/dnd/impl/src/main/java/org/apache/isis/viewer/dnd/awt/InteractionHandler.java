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

package org.apache.isis.viewer.dnd.awt;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.interaction.ClickImpl;
import org.apache.isis.viewer.dnd.interaction.ContentDragImpl;
import org.apache.isis.viewer.dnd.interaction.DragStartImpl;
import org.apache.isis.viewer.dnd.view.Click;
import org.apache.isis.viewer.dnd.view.ContentDrag;
import org.apache.isis.viewer.dnd.view.DragEvent;
import org.apache.isis.viewer.dnd.view.InteractionSpy;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.ViewAreaType;
import org.apache.isis.viewer.dnd.view.base.AbstractView;
import org.apache.isis.viewer.dnd.view.content.NullContent;

public class InteractionHandler implements MouseMotionListener, MouseListener, KeyListener {
    private static final Logger LOG = LoggerFactory.getLogger(InteractionHandler.class);
    private final static int THRESHOLD = 7;
    private boolean canDrag;
    /*
     * The location within the frame where the mouse button was pressed down.
     */
    private Location downAt;
    private DragEvent drag;
    private final KeyboardManager keyboardManager;
    private View identifiedView;
    private final InteractionSpy spy;
    private final XViewer viewer;
    private KeyEvent lastTyped;
    private View draggedView;
    private final XFeedbackManager feedbackManager;

    public InteractionHandler(final XViewer viewer, final XFeedbackManager feedbackManager, final KeyboardManager keyboardManager, final InteractionSpy spy) {
        this.viewer = viewer;
        this.feedbackManager = feedbackManager;
        this.spy = spy;
        this.keyboardManager = keyboardManager;
    }

    private void drag(final MouseEvent me) {
        final Location location = createLocation(me.getPoint());
        spy.addAction("Mouse dragged " + location);
        final View target = viewer.identifyView(new Location(location), false);
        drag.drag(target, location, me.getModifiers());
    }

    private Location createLocation(final Point point) {
        return new Location(point.x, point.y);
    }

    private void dragStart(final MouseEvent me) {
        if (!isOverThreshold(downAt, me.getPoint())) {
            return;
        }

        spy.addAction("Drag start  at " + downAt);
        drag = viewer.dragStart(new DragStartImpl(downAt, me.getModifiers()));

        if (drag == null) {
            spy.addAction("drag start  ignored");
            canDrag = false;
        } else {
            spy.addAction("drag start " + drag);
            final View overlay = drag.getOverlay();
            if (overlay != null) {
                viewer.setOverlayView(overlay);
            }
            final View target = viewer.identifyView(createLocation(me.getPoint()), false);
            drag.drag(target, createLocation(me.getPoint()), me.getModifiers());
        }
        identifiedView = null;
    }

    /**
     * Returns true when the point is outside the area around the downAt
     * location
     */
    private boolean isOverThreshold(final Location pressed, final Point dragged) {
        final int xDown = pressed.getX();
        final int yDown = pressed.getY();
        final int x = dragged.x;
        final int y = dragged.y;

        return x > xDown + THRESHOLD || x < xDown - THRESHOLD || y > yDown + THRESHOLD || y < yDown - THRESHOLD;
    }

    /**
     * Listener for key presses. Cancels popup and drags, and forwards key
     * presses to the view that has the keyboard focus.
     * 
     * @see java.awt.event.KeyListener#keyPressed(KeyEvent)
     */
    @Override
    public void keyPressed(final KeyEvent ke) {
        if (isBusy(identifiedView)) {
            return;
        }

        lastTyped = null;
        try {
            if (ke.getKeyCode() == KeyEvent.VK_ESCAPE && drag != null) {
                if (drag != null) {
                    drag.cancel(viewer);
                    drag = null;
                }
                viewer.clearAction();
            } else if (ke.getKeyCode() == KeyEvent.VK_F5) {
                draggedView = identifiedView;
            } else if (draggedView != null && ke.getKeyCode() == KeyEvent.VK_F6) {
                final ContentDrag content = new ContentDragImpl(draggedView, new Location(), new AbstractView(new NullContent()) {
                });
                if (identifiedView != null) {
                    identifiedView.drop(content);
                }

                draggedView = null;
            } else {
                keyboardManager.pressed(ke.getKeyCode(), ke.getModifiers());
            }
            // ke.consume();

            redraw();
        } catch (final Exception e) {
            interactionException("keyPressed", e);
        }
    }

    /**
     * Listener for key releases and forward them to the view that has the
     * keyboard focus.
     * 
     * @see java.awt.event.KeyListener#keyReleased(KeyEvent)
     */
    @Override
    public void keyReleased(final KeyEvent ke) {
        if (isBusy(identifiedView)) {
            return;
        }
        // LOG.debug("key " + KeyEvent.getKeyText(ke.getKeyCode()) +
        // " released\n");

        try {
            if (lastTyped == null && ke.getKeyCode() != KeyEvent.VK_SHIFT && ke.getKeyCode() != KeyEvent.VK_ALT && ke.getKeyCode() != KeyEvent.VK_CONTROL) {
                if (ke.getKeyCode() >= KeyEvent.VK_0 && ke.getKeyCode() <= KeyEvent.VK_DIVIDE) {
                    LOG.error("no type event for '" + KeyEvent.getKeyText(ke.getKeyCode()) + "':  " + ke);
                }
            }

            keyboardManager.released(ke.getKeyCode(), ke.getModifiers());
            ke.consume();
            redraw();
        } catch (final Exception e) {
            interactionException("keyReleased", e);
        }

    }

    /**
     * Listener for key press, and subsequent release, and forward it as one
     * event to the view that has the keyboard focus.
     * 
     * @see java.awt.event.KeyListener#keyTyped(KeyEvent)
     */
    @Override
    public void keyTyped(final KeyEvent ke) {
        if (isBusy(identifiedView)) {
            return;
        }

        final char keyChar = ke.getKeyChar();
        if (!Character.isISOControl(keyChar)) {
            // ignoring control keys and the delete key
            // LOG.debug("typed '" + keyChar + "': " + ke);
            // LOG.debug("typed " + (int) keyChar);
            keyboardManager.typed(keyChar);
            ke.consume();
            lastTyped = ke;
            redraw();
        }
    }

    private void interactionException(final String action, final Exception e) {
        LOG.error("error during user interaction: " + action, e);
        feedbackManager.showException(e);
    }

    /**
     * Responds to mouse click events by calling <code>firstClick</code>,
     * <code>secondClick</code>, and <code>thirdClick</code> on the view that
     * the mouse is over. Ignored if the mouse is not over a view.
     * 
     * @see java.awt.event.MouseListener#mouseClicked(MouseEvent)
     */
    @Override
    public void mouseClicked(final MouseEvent me) {
        if (isBusy(identifiedView)) {
            return;
        }
        try {
            final Click click = new ClickImpl(downAt, me.getModifiers());
            spy.addAction("Mouse clicked " + click.getLocation());
            if (click.button3() && identifiedView != null) {
                // ignore popup trigger - dealt with by mousePressed or
                // mouseReleased (depending on platform)
            } else if (viewer.isOverlayAvailable()) {
                overlayClick(click);
                // } else if (click.button3() && identifiedView != null) {
                // fireMenuPopup(click);
            } else {
                fireClick(click, me.getClickCount());
            }
            redraw();
        } catch (final Exception e) {
            interactionException("mouseClicked", e);
        }
    }

    private void overlayClick(final Click click) {
        final View overlayView = viewer.getOverlayView();
        if (overlayView == identifiedView || (identifiedView != null && identifiedView.getParent() != null && overlayView == identifiedView.getParent())) {
            viewer.firstClick(click);
        } else {
            viewer.clearAction();
        }
    }

    private void fireMenuPopup(final Click click) {
        if (identifiedView != null) {
            spy.addAction(" popup " + downAt + " over " + identifiedView);

            boolean forView = viewer.viewAreaType(new Location(click.getLocation())) == ViewAreaType.VIEW;
            forView = click.isAlt() ^ forView;
            final boolean includeExploration = click.isCtrl();
            final boolean includeDebug = click.isShift();
            final Location at = click.getLocation();
            at.move(-14, -10);
            viewer.popupMenu(identifiedView, at, forView, includeExploration, includeDebug);
        }
    }

    private void fireClick(final Click click, final int clickCount) {
        viewer.setKeyboardFocus(identifiedView);

        switch (clickCount) {
        case 1:
            viewer.firstClick(click);
            break;

        case 2:
            viewer.secondClick(click);
            break;

        case 3:
            viewer.thirdClick(click);
            break;

        default:
            break;
        }
    }

    /**
     * Responds to mouse dragged according to the button used. If the left
     * button then identified view is moved.
     * 
     * @see java.awt.event.MouseMotionListener#mouseDragged(MouseEvent)
     */
    @Override
    public void mouseDragged(final MouseEvent me) {
        if (isBusy(identifiedView)) {
            return;
        }

        try {
            viewer.translate(me);

            final Location location = createLocation(me.getPoint());
            spy.setLocationInViewer(location);

            if (canDrag) {
                // checked to ensure that dragging over a view doesn't start a
                // drag - it should only start when already over a view.

                spy.reset();
                // viewer.translate(me);
                if (drag == null) {
                    // no drag in progress yet
                    dragStart(me);
                    redraw();
                } else {
                    drag(me);
                    redraw();
                }
            }
        } catch (final Exception e) {
            interactionException("mouseDragged", e);
        }

    }

    /**
     * event ignored
     * 
     * @see java.awt.event.MouseListener#mouseEntered(MouseEvent)
     */
    @Override
    public void mouseEntered(final MouseEvent arg0) {
    }

    /**
     * event ignored
     * 
     * @see java.awt.event.MouseListener#mouseExited(MouseEvent)
     */
    @Override
    public void mouseExited(final MouseEvent arg0) {
    }

    /**
     * responds to mouse moved event by setting the view found underneath the
     * mouse as the idetified view. Views normally respond by changing the
     * colour of themselves so they are visual distinct and hence shows itself
     * as special compared to the rest.
     * 
     * @see java.awt.event.MouseMotionListener#mouseMoved(MouseEvent)
     */
    @Override
    public void mouseMoved(final MouseEvent me) {
        try {
            if (drag == null) {
                spy.reset();
                viewer.translate(me);
                final Location location = createLocation(me.getPoint());
                spy.setLocationInViewer(location);

                final View overView = viewer.identifyView(new Location(location), true);
                spy.setOver(overView);

                spy.addAction("moved " + location);

                if (overView != null) {
                    if (overView != identifiedView) {
                        if (identifiedView != null) {
                            spy.addAction("exited " + identifiedView);
                            identifiedView.exited();
                        }

                        if (overView != null) {
                            spy.addAction("entered " + overView);
                            overView.entered();
                        }

                        redraw();
                        feedbackManager.showBusyState(overView);
                    }
                    identifiedView = overView;

                    spy.addTrace("--> mouse moved");
                    viewer.mouseMoved(location);
                    spy.addTrace(overView, " mouse location", location);
                    if ((me.getModifiers() & InputEvent.ALT_MASK) > 0 && overView.getContent() != null) {
                        final ObjectAdapter object = overView.getContent().getAdapter();
                        final ViewAreaType area = overView.viewAreaType(location);
                        feedbackManager.setViewDetail("Over " + location + " [" + area + "] " + object);
                    }

                    redraw();
                }
            }
        } catch (final Exception e) {
            interactionException("mouseMoved", e);
        }

    }

    private boolean isBusy(final View view) {
        return feedbackManager != null && feedbackManager.isBusy(view);
    }

    /**
     * Responds to the mouse pressed event (with the left button pressed) by
     * initiating a drag. This sets up the <code>View</code>'s dragging state to
     * the view that the mouse was over when the button was pressed.
     * 
     * @see java.awt.event.MouseListener#mousePressed(MouseEvent)
     */
    @Override
    public void mousePressed(final MouseEvent me) {
        try {
            if (isBusy(identifiedView)) {
                return;
            }

            spy.reset();
            viewer.translate(me);

            downAt = createLocation(me.getPoint());
            spy.setDownAt(downAt);

            final Location location = createLocation(me.getPoint());
            spy.setLocationInViewer(location);

            final View overView = viewer.identifyView(new Location(location), true);
            spy.setOver(overView);
            spy.addAction("Mouse pressed " + location);
            drag = null;

            final Click click = new ClickImpl(downAt, me.getModifiers());
            if (me.isPopupTrigger()) {
                if (overView != null) {
                    fireMenuPopup(click);
                }
            } else {
                viewer.mouseDown(click);
                // drag should not be valid after double/triple click
                canDrag = overView != null && me.getClickCount() == 1;
                identifiedView = overView;
            }
            redraw();
        } catch (final Exception e) {
            interactionException("mousePressed", e);
        }

    }

    /**
     * Responds to the mouse released event (with the left button pressed) by
     * telling the identified view (the drop zone) that the dragged object is
     * being dropped on it (via the views <code>drop</code> method). If the drop
     * takes place outside of all of the other views then the
     * <code>workspaceDrop</code> method is called instead to indicate a drop
     * onto the workspace.
     * 
     * @see java.awt.event.MouseListener#mouseReleased(MouseEvent)
     */
    @Override
    public void mouseReleased(final MouseEvent me) {
        if (isBusy(identifiedView) || downAt == null) {
            return;
        }

        try {
            if (drag != null) {
                mouseDragged(me);

                final Location location = createLocation(me.getPoint());
                final View target = viewer.identifyView(new Location(location), false);
                drag.drag(target, location, me.getModifiers());
                // viewer.clearStatus();
                drag.end(viewer);
                redraw();

                drag = null;
            }

            final Click click = new ClickImpl(downAt, me.getModifiers());
            if (me.isPopupTrigger()) {
                if (identifiedView != null) {
                    fireMenuPopup(click);
                }
            } else {
                viewer.mouseUp(click);
            }
            redraw();
        } catch (final Exception e) {
            interactionException("mouseReleased", e);
        }
    }

    private void redraw() {
        viewer.scheduleRepaint();
    }
}
