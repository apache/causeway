package org.nakedobjects.viewer.skylark;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.apache.log4j.Logger;


public class InteractionHandler implements MouseMotionListener, MouseListener, KeyListener {
    private static final Logger LOG = Logger.getLogger(InteractionHandler.class);
    private boolean canDrag;
    private View currentlyIdentifiedView;

    /*
     * The location within the frame where the mouse button was pressed down.
     */
    private Location downAt;
    private Drag drag;
    private Location mouseLocation;
    private View previouslyIdentifiedView;
    private InteractionSpy spy;
    private final Viewer viewer;

    InteractionHandler(Viewer topView, InteractionSpy debugFrame) {
        this.viewer = topView;
        this.spy = debugFrame;

    }

    private void drag(MouseEvent me) {
        mouseDetails(me, false);

        View target = currentlyIdentifiedView;
        spy.addAction("Mouse dragged " + mouseLocation);
        spy.addAction("  target " + target);

        drag.update(mouseLocation, target);
        spy.addAction("before drag " + drag);
        drag.drag();
        spy.addAction("after drag  " + drag);

        if (viewer.getOverlayView() == target) {
            LOG.error("drag identified over overlay!!!! " + target);
        }

        previouslyIdentifiedView = target;
    }

    private void dragStart(MouseEvent me) {
        if (!isOverThreshold(downAt, me.getPoint())) {
            return;
        }
        Location locationInView = new Location(mouseLocation);
        Location locationOfView = previouslyIdentifiedView.getAbsoluteLocation();
        locationInView.subtract(locationOfView);

        mouseDetails(me, true);

        ViewAreaType type = previouslyIdentifiedView.viewAreaType(new Location(locationInView));
        spy.addAction("Drag start " + type + ": " + mouseLocation);
        spy.setLocationInView(locationInView);

        if (type == ViewAreaType.INTERNAL) {
            drag = InternalDrag.create(previouslyIdentifiedView, mouseLocation, me.getModifiers());
            spy.addAction("drag from " + drag);
        } else {
            saveCurrentFieldEntry();

            spy.addAction("pickup " + type + ": " + previouslyIdentifiedView);

            if (type == ViewAreaType.VIEW) {
                drag = ViewDrag.create(previouslyIdentifiedView, mouseLocation, me.getModifiers());
            } else {
                drag = ContentDrag.create(previouslyIdentifiedView, mouseLocation, me.getModifiers());
            }
        }
        if (drag == null) {
            // TODO this look unnecessary
            canDrag = false;
        }
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

        int threshold = 10;
        return x > xDown + threshold || x < xDown - threshold || y > yDown + threshold || y < yDown - threshold;
    }

    /**
     * Listener for key presses. Cancels popup and drags, and forwards key
     * presses to the view that has the keyboard focus.
     * 
     * @see java.awt.event.KeyListener#keyPressed(KeyEvent)
     */
    public void keyPressed(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (drag != null) {
                drag.cancel();
                drag = null;
            }
            viewer.clearOverlayView();
        }

        View keyboardFocus = viewer.getFocus();

        if (keyboardFocus != null) {
            keyboardFocus.keyPressed(ke.getKeyCode(), ke.getModifiers());
        }

        redraw();
    }

    /**
     * Listener for key releases and forward them to the view that has the
     * keyboard focus.
     * 
     * @see java.awt.event.KeyListener#keyReleased(KeyEvent)
     */
    public void keyReleased(KeyEvent ke) {
        View keyboardFocus = viewer.getFocus();

        if (keyboardFocus != null) {
            keyboardFocus.keyReleased(ke.getKeyCode(), ke.getModifiers());
        }
        redraw();
    }

    /**
     * Listener for key press, and subsequent release, and forward it as one
     * event to the view that has the keyboard focus.
     * 
     * @see java.awt.event.KeyListener#keyTyped(KeyEvent)
     */
    public void keyTyped(KeyEvent ke) {
        View keyboardFocus = viewer.getFocus();
        if (keyboardFocus != null) {
            if (!ke.isActionKey() && !Character.isISOControl(ke.getKeyChar())) {
                keyboardFocus.keyTyped(ke.getKeyChar());
            }
        }
        redraw();
    }

    /**
     * Responds to mouse click events by calling <code>firstClick</code>,
     * <code>secondClick</code>, and <code>thirdClick</code> on the view
     * that the mouse is over. Ignored if the mouse is not over a view.
     * 
     * @see java.awt.event.MouseListener#mouseClicked(MouseEvent)
     */
    public void mouseClicked(MouseEvent me) {

        if (currentlyIdentifiedView != null) {
            Click click = new Click(currentlyIdentifiedView, mouseLocation, me.getModifiers());
	        spy.addAction("Mouse clicked " + click.getMouseLocationRelativeToView());

            if (click.isButton3() && viewer.getOverlayView() == null) {
                saveCurrentFieldEntry();
                if (currentlyIdentifiedView != null) {
                    spy.addAction(" popup " + mouseLocation + " over " + currentlyIdentifiedView);
                    viewer.popupMenu(click, currentlyIdentifiedView);
                    previouslyIdentifiedView = currentlyIdentifiedView;
                }

            } else if (currentlyIdentifiedView != null) {
                if (me.getClickCount() == 1) {
                    currentlyIdentifiedView.firstClick(click);
                } else if (me.getClickCount() == 2) {
                    currentlyIdentifiedView.secondClick(click);
                } else if (me.getClickCount() == 3) {
                    currentlyIdentifiedView.thirdClick(click);
                }
            }
            redraw();
        }
    }

    /**
     * Calculates all the relevant information about locations and views based
     * on the current mouse location.
     */
    private void mouseDetails(MouseEvent me, boolean includeOverlay) {
        spy.reset();
        viewer.translate(me);
        mouseLocation = new Location(me.getPoint());
        spy.setLocationInViewer(mouseLocation);

        currentlyIdentifiedView = viewer.identifyView(new Location(mouseLocation), includeOverlay);
        spy.setOver(currentlyIdentifiedView);
    }

    /**
     * Responds to mouse dragged according to the button used. If the left
     * button then identified view is moved.
     * 
     * @see java.awt.event.MouseMotionListener#mouseDragged(MouseEvent)
     */
    public void mouseDragged(MouseEvent me) {
        if (canDrag) {
            // checked to ensure that dragging over a view doesn't start a
            // drag - it should only start when already over a view.

            if (drag == null) {
                // no drag in progress yet
                dragStart(me);
                redraw();
            } else {
                drag(me);
                redraw();
            }
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
        if (drag == null) {
            mouseDetails(me, true);
            if (currentlyIdentifiedView != null) {
                if (previouslyIdentifiedView == null) {
                    previouslyIdentifiedView = currentlyIdentifiedView;
                } else {
                    if (currentlyIdentifiedView != previouslyIdentifiedView) {

                        if (currentlyIdentifiedView == previouslyIdentifiedView) {
                            spy.addAction("moved into subview from " + previouslyIdentifiedView);
                            previouslyIdentifiedView.enteredSubview();
                        } else {
                            spy.addAction("exited " + previouslyIdentifiedView);
                            previouslyIdentifiedView.exited();
                        }

                        View previouslyIdentified = previouslyIdentifiedView;
                        previouslyIdentifiedView = currentlyIdentifiedView;

                        if (currentlyIdentifiedView != null) {
                            if (currentlyIdentifiedView == previouslyIdentified) {
                                spy.addAction("moved back to from " + previouslyIdentified);
                                currentlyIdentifiedView.exitedSubview();
                            } else {
                                spy.addAction("entered " + currentlyIdentifiedView);
                                currentlyIdentifiedView.entered();
                            }
                        }
                        redraw();
                    }
                }

                spy.addTrace("--> mouse moved");
                spy.addTrace(currentlyIdentifiedView, " mouse location", mouseLocation);
                Location locationInView = new Location(mouseLocation);
                Location locationOfView = currentlyIdentifiedView.getAbsoluteLocation();
                spy.addTrace(currentlyIdentifiedView, " view location", locationOfView);
                locationInView.subtract(locationOfView);
                spy.addTrace(currentlyIdentifiedView, " mouse location within view", locationInView);
               spy.setType(currentlyIdentifiedView.viewAreaType(new Location(locationInView)));
                spy.addAction("mouseMoved " + locationInView);
                currentlyIdentifiedView.mouseMoved(locationInView);

                redraw();
            }
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
        downAt = new Location(me.getPoint());
        spy.setDownAt(downAt);
        mouseDetails(me, true);
        spy.addAction("Mouse pressed " + mouseLocation);
        drag = null;

        // hide an overlay view when not being pointed to
        if (currentlyIdentifiedView != viewer.getOverlayView()) {
            viewer.disposeOverlayView();
        }

        viewer.makeFocus(currentlyIdentifiedView);
        canDrag = currentlyIdentifiedView != null && me.getClickCount() == 1;
        // drag should not be valid after double/triple click
        previouslyIdentifiedView = currentlyIdentifiedView;
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
        if (drag != null) {
            mouseDragged(me);

            View target = currentlyIdentifiedView;

            drag.update(mouseLocation, target);
            spy.addAction("drag ended at " + mouseLocation + " over " + target);
            drag.end();

            viewer.disposeOverlayView();

            drag = null;

            redraw();
        }
    }

    private void redraw() {
        viewer.repaint();
    }

    public void saveCurrentFieldEntry() {
        View keyboardFocus = viewer.getFocus();

        if (keyboardFocus != null) {
            View focus = keyboardFocus;
            keyboardFocus = null;
            focus.editComplete();
            focus.markDamaged();
        }
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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
