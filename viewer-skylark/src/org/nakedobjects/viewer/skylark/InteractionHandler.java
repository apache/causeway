package org.nakedobjects.viewer.skylark;

import org.nakedobjects.object.Naked;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.apache.log4j.Logger;


public class InteractionHandler implements MouseMotionListener, MouseListener, KeyListener {
    private static final Logger LOG = Logger.getLogger(InteractionHandler.class);
    private final static int THRESHOLD = 7;
    private boolean canDrag;
    /*
     * The location within the frame where the mouse button was pressed down.
     */
    private Location downAt;
    private Drag drag;
    private final KeyboardManager keyboardManager;
    private View previouslyIdentifiedView;
    private InteractionSpy spy;
    private final Viewer viewer;

    InteractionHandler(Viewer viewer, InteractionSpy spy) {
        this.viewer = viewer;
        this.spy = spy;
        keyboardManager = new KeyboardManager(viewer);
    }

    private void drag(MouseEvent me) {
        Location location = new Location(me.getPoint());
        spy.addAction("Mouse dragged " + location);
        drag.drag(viewer, location, me.getModifiers());
    }

    private void dragStart(MouseEvent me) {
        if (!isOverThreshold(downAt, me.getPoint())) {
            return;
        }

        spy.addAction("Drag start  at " + downAt);
        drag = viewer.dragStart(new DragStart(downAt, me.getModifiers()));

        if (drag == null) {
            spy.addAction("drag start  ignored");
            canDrag = false;
        } else {
            spy.addAction("drag start " + drag);
            drag.start(viewer);
            View overlay = drag.getOverlay();
            if (overlay != null) {
                viewer.setOverlayView(overlay);
            }
            drag.drag(viewer, new Location(me.getPoint()), me.getModifiers());
        }
        previouslyIdentifiedView = null;
    }

    /**
     * Returns true when the point is outside the area around the downAt
     * location
     */
    private boolean isOverThreshold(Location pressed, Point dragged) {
        int xDown = pressed.x;
        int yDown = pressed.y;
        int x = dragged.x;
        int y = dragged.y;

        return x > xDown + THRESHOLD || x < xDown - THRESHOLD || y > yDown + THRESHOLD || y < yDown - THRESHOLD;
    }

    /**
     * Listener for key presses. Cancels popup and drags, and forwards key
     * presses to the view that has the keyboard focus.
     * 
     * @see java.awt.event.KeyListener#keyPressed(KeyEvent)
     */
    public void keyPressed(KeyEvent ke) {
        LOG.debug("pressed " + KeyEvent.getKeyText(ke.getKeyCode()));
        try {
            if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
                if (drag != null) {
                    drag.cancel(viewer);
                    drag = null;
                }
                viewer.clearStatus();
                viewer.clearOverlayView();
            }
            keyboardManager.pressed(ke.getKeyCode(), ke.getModifiers());

            redraw();
        } catch (Exception e) {
            log(e);
        }
    }

    /**
     * Listener for key releases and forward them to the view that has the
     * keyboard focus.
     * 
     * @see java.awt.event.KeyListener#keyReleased(KeyEvent)
     */
    public void keyReleased(KeyEvent ke) {
        try {
            LOG.debug("released " + KeyEvent.getKeyText(ke.getKeyCode()));
            keyboardManager.released(ke.getKeyCode(), ke.getModifiers());
            redraw();
        } catch (Exception e) {
            log(e);
        }

    }

    /**
     * Listener for key press, and subsequent release, and forward it as one
     * event to the view that has the keyboard focus.
     * 
     * @see java.awt.event.KeyListener#keyTyped(KeyEvent)
     */
    public void keyTyped(KeyEvent ke) {
        LOG.debug("typed " + ke.getKeyChar());
       if (!ke.isActionKey()) {
            keyboardManager.typed(ke.getKeyChar());
            redraw();
        }
    }

    private void log(Exception e) {
        LOG.error("Error during user interaction", e);
    }

    /**
     * Responds to mouse click events by calling <code>firstClick</code>,
     * <code>secondClick</code>, and <code>thirdClick</code> on the view
     * that the mouse is over. Ignored if the mouse is not over a view.
     * 
     * @see java.awt.event.MouseListener#mouseClicked(MouseEvent)
     */
    public void mouseClicked(MouseEvent me) {
        try {
            Click click = new Click(previouslyIdentifiedView, downAt, me.getModifiers());
            spy.addAction("Mouse clicked " + click.getLocation());

            if (click.button3() && viewer.getOverlayView() == null) {
                if (previouslyIdentifiedView != null) {
                    spy.addAction(" popup " + downAt + " over " + previouslyIdentifiedView);
                    viewer.popupMenu(previouslyIdentifiedView, click);
                }

            } else {
                switch (me.getClickCount()) {
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
            redraw();
        } catch (Exception e) {
            log(e);
        }
    }

    /**
     * Responds to mouse dragged according to the button used. If the left
     * button then identified view is moved.
     * 
     * @see java.awt.event.MouseMotionListener#mouseDragged(MouseEvent)
     */
    public void mouseDragged(MouseEvent me) {
        try {
            if (canDrag) {
                // checked to ensure that dragging over a view doesn't start a
                // drag - it should only start when already over a view.

                spy.reset();
                viewer.translate(me);
                if (drag == null) {
                    // no drag in progress yet
                    dragStart(me);
                    redraw();
                } else {
                    drag(me);
                    redraw();
                }
            }
        } catch (Exception e) {
            log(e);
        }

    }

    /**
     * event ignored
     * 
     * @see java.awt.event.MouseListener#mouseEntered(MouseEvent)
     */
    public void mouseEntered(MouseEvent arg0) {}

    /**
     * event ignored
     * 
     * @see java.awt.event.MouseListener#mouseExited(MouseEvent)
     */
    public void mouseExited(MouseEvent arg0) {}

    /**
     * responds to mouse moved event by setting the view found underneath the
     * mouse as the idetified view. Views normally respond by changing the
     * colour of themselves so they are visual distinct and hence shows itself
     * as special compared to the rest.
     * 
     * @see java.awt.event.MouseMotionListener#mouseMoved(MouseEvent)
     */
    public void mouseMoved(MouseEvent me) {
        try {
            if (drag == null) {
                spy.reset();
                viewer.translate(me);
                Location location = new Location(me.getPoint());
                spy.setLocationInViewer(location);

                View overView = viewer.identifyView(new Location(location), true);
                spy.setOver(overView);

                if (overView != null) {
                    if (previouslyIdentifiedView == null) {
                        previouslyIdentifiedView = overView;
                    } else {
                        if (overView != previouslyIdentifiedView) {

                            if (overView == previouslyIdentifiedView) {
                                spy.addAction("moved into subview from " + previouslyIdentifiedView);
                                previouslyIdentifiedView.enteredSubview();
                            } else {
                                spy.addAction("exited " + previouslyIdentifiedView);
                                previouslyIdentifiedView.exited();
                            }

                            View previouslyIdentified = previouslyIdentifiedView;
                            previouslyIdentifiedView = overView;

                            if (overView != null) {
                                if (overView == previouslyIdentified) {
                                    spy.addAction("moved back to from " + previouslyIdentified);
                                    overView.exitedSubview();
                                } else {
                                    spy.addAction("entered " + overView);
                                    overView.entered();
                                }
                            }
                            redraw();
                        }
                    }

                    spy.addTrace("--> mouse moved");
                    viewer.mouseMoved(location);
                    spy.addTrace(overView, " mouse location", location);
                    if((me.getModifiers() & InputEvent.ALT_MASK) > 0) {
                        Naked object = overView.getContent().getNaked();
                        viewer.setStatus("Mouse over " + object);
                    }

                    
                    redraw();
                }
            }
        } catch (Exception e) {
            log(e);
        }

    }

    /**
     * Responds to the mouse pressed event (with the left button pressed) by
     * initiating a drag. This sets up the <code>View</code>'s dragging state
     * to the view that the mouse was over when the button was pressed.
     * 
     * @see java.awt.event.MouseListener#mousePressed(MouseEvent)
     */
    public void mousePressed(MouseEvent me) {
        try {
            spy.reset();
            viewer.translate(me);

            downAt = new Location(me.getPoint());
            spy.setDownAt(downAt);

            Location location = new Location(me.getPoint());
            spy.setLocationInViewer(location);

            View overView = viewer.identifyView(new Location(location), true);
            spy.setOver(overView);
            spy.addAction("Mouse pressed " + location);
            drag = null;

            // hide an overlay view when not being pointed to
            View overlayView = viewer.getOverlayView();
            if (overlayView != null && overlayView != overView && !overlayView.contains(overView)) {
                viewer.clearStatus();
                viewer.disposeOverlayView();
            }
            viewer.makeFocus(overView);
            // drag should not be valid after double/triple click
            canDrag = overView != null && me.getClickCount() == 1;
            previouslyIdentifiedView = overView;
        } catch (Exception e) {
            log(e);
        }

    }

    /**
     * Repsonds to the mouse released event (with the left button pressed) by
     * telling the identified view (the drop zone) that the dragged object is
     * being dropped on it (via the views <code>drop</code> method). If the
     * drop takes place outside of all of the other views then the
     * <code>workspaceDrop</code> method is called instead to indicate a drop
     * onto the workspace.
     * 
     * @see java.awt.event.MouseListener#mouseReleased(MouseEvent)
     */
    public void mouseReleased(MouseEvent me) {
        try {
            if (drag != null) {
                mouseDragged(me);

                Location location = new Location(me.getPoint());
                drag.drag(viewer, location, me.getModifiers());
                viewer.clearStatus();
                viewer.disposeOverlayView();
                drag.end(viewer);
                redraw();

                drag = null;
            }
        } catch (Exception e) {
            log(e);
        }
    }

    private void redraw() {
        viewer.repaint();
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
