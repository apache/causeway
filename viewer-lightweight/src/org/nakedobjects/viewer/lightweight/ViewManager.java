/*
    Naked Objects - a framework that exposes behaviourally complete
    business objects directly to the user.
    Copyright (C) 2000 - 2003  Naked Objects Group Ltd

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

    The authors can be contacted via www.nakedobjects.org (the
    registered address of Naked Objects Group is Kingsway House, 123 Goldworth
    Road, Woking GU21 1NR, UK).
*/
package org.nakedobjects.viewer.lightweight;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.apache.log4j.Logger;
import org.nakedobjects.utility.Configuration;


/*
 * the go-between for the frame and workspace view
 */
public class ViewManager implements MouseMotionListener, MouseListener, KeyListener {
    private static final Logger LOG = Logger.getLogger(ViewManager.class);
    public KeyboardAccessible keyboardFocus;
    private DragHandler drag;

    /*
     * The location within the frame where the mouse button was pressed down.
     */
    private Location downAt;
    private Location mouseLocation;
    private final PopupMenu popup;
    private final Workspace workspace;
    private boolean canDrag;
	private boolean explorationMode;

    ViewManager(Workspace workspace, PopupMenu popup) {
        this.workspace = workspace;
        this.popup = popup;
        
        explorationMode = Configuration.getInstance().getBoolean(
            	"viewer.lightweight.show-exploration");
    }

    public DragHandler getDragHandler() {
        return drag;
    }

    /**
     * Listener for key presses.  Cancels popup and drags, and forwards key
     * presses to the view that has the keyboard focus.
     * @see java.awt.event.KeyListener#keyPressed(KeyEvent)
     */
    public void keyPressed(KeyEvent ke) {
        if ((getDraggingView() != null) && (ke.getKeyCode() == KeyEvent.VK_ESCAPE)) {
            drag.getDragging().dragCancel(drag);

            if (drag instanceof ObjectDrag) {
                DragTarget target = ((ObjectDrag) drag).getTarget();

                if (target != null) {
                    target.dragObjectOut((ObjectDrag) drag);
                }
            }

            workspace.clearOverlayView();
            canDrag = false;
            drag = null;
        }

        if (keyboardFocus == null) {
//            if (ke.getKeyCode() != KeyEvent.VK_SHIFT) {
//                //	            popup.setVisible(false);
//                View view = identify(mouseLocation);
//                move(view);
//            }
        } else {
            keyboardFocus.keyPressed(ke.getKeyCode(), ke.getModifiers());
        }
    }

    /**
     * Listener for key releases and forward them to the view that has the
     * keyboard focus.
     * @see java.awt.event.KeyListener#keyReleased(KeyEvent)
     */
    public void keyReleased(KeyEvent ke) {
        if (keyboardFocus != null) {
            keyboardFocus.keyReleased(ke.getKeyCode(), ke.getModifiers());
        }
    }

    /**
     * Listener for key press, and subsequent release, and forward it as one
     * event to the view that has the keyboard focus.
     * @see java.awt.event.KeyListener#keyTyped(KeyEvent)
     */
    public void keyTyped(KeyEvent ke) {
        if (keyboardFocus != null) {
            if (!ke.isActionKey() && !Character.isISOControl(ke.getKeyChar())) {
                keyboardFocus.keyTyped(ke.getKeyChar());
            }
        }
    }

    public void makeFocus(KeyboardAccessible view) {
        if ((keyboardFocus != null) && (keyboardFocus != view)) {
            keyboardFocus.focusLost();
            keyboardFocus.redraw();
        }

        keyboardFocus = view;
        keyboardFocus.focusRecieved();
    }

    /**
     * Responds to mouse click events by calling <code>firstClick</code>,
     * <code>secondClick</code>, and <code>thirdClick</code> on the view that
     * the mouse is over.  Ignored if the mouse is not over a view.
     *
     * @see java.awt.event.MouseListener#mouseClicked(MouseEvent)
     */
    public void mouseClicked(MouseEvent me) {
        View over = identify(new Location(me.getPoint())); //workspace.identifyView(new Location(me.getPoint()), null);

        Click click = new Click(over, me);

        if (click.isButton3()) {
            saveCurrentFieldEntry();
            popupMenu(click, over);
        } else if (over != null) {
            if (me.getClickCount() == 1) {
                over.firstClick(click);
            } else if (me.getClickCount() == 2) {
                over.secondClick(click);
            } else if (me.getClickCount() == 3) {
                over.thirdClick(click);
            }
        }
    }

    /**
     * Responds to mouse dragged according to the button used. If the left
     * button then identified view is moved.
     *
     * @see java.awt.event.MouseMotionListener#mouseDragged(MouseEvent)
     */
    public void mouseDragged(MouseEvent me) {
        mouseLocation = new Location(me.getPoint());

        if (canDrag) { // checked to ensure that dragging over a view doesn't start a drag - it should only start when already over a view.

            if (drag == null) { // no drag in progress yet

                View identified = workspace.getIdentifiedView();

                if (identified instanceof DragSource) {
                    saveCurrentFieldEntry();

                    Location p = new Location(downAt);
                    Location offsetFrom = identified.getAbsoluteLocation();
                    p.translate(-offsetFrom.x, -offsetFrom.y);

                    if (identified.indicatesForView(p)) {
                        drag = new ViewDrag((DragSource) identified, me, downAt);
                    } else {
                        drag = new ObjectDrag((DragSource) identified, me, downAt);
                    }
                    workspace.setOverlayView(drag.getDragging());

                    identified.exited();
                    LOG.debug("ObjectView.pickupObject/View " + drag);
                } else if (identified instanceof AbstractValueView) {
                    drag = new InternalDrag((AbstractValueView) identified, me, downAt);
                    LOG.debug("ValueView.dragFrom " + drag);
                } else {
                    LOG.debug("invalid drag start; not a drag source: " + identified);
                }
            } else {
                drag.update(me);

                if (drag instanceof InternalDrag) {
                    drag.dragIn(null);
                } else {
                    if (getDraggingView() != null) {
                        getDraggingView().dragging(drag);
                    }

                    View over = workspace.identifyView(new Location(me.getPoint()),
                            getDraggingView());
                    View identified = workspace.getIdentifiedView();

                    if (over != identified) {
                        if (identified != null) {
                            drag.dragOut(identified);
                        }

                        workspace.setIdentifiedView(over);

                        if (over != null) {
                            drag.dragIn(over);
                        }
                    }
                }
            }
        }
    }

    /**
     * event ignored
     * @see java.awt.event.MouseListener#mouseEntered(MouseEvent)
     */
    public void mouseEntered(MouseEvent arg0) {
    }

    /**
     * event ignored
     * @see java.awt.event.MouseListener#mouseExited(MouseEvent)
     */
    public void mouseExited(MouseEvent arg0) {
    }

    /**
     * responds to mouse moved event by setting the view found underneath the
     * mouse as the idetified view.  Views normally respond by changing the
     * colour of themselves so they are visual distinct and hence shows itself
     * as special compared to the rest.
     *
     * @see java.awt.event.MouseMotionListener#mouseMoved(MouseEvent)
     */
    public void mouseMoved(MouseEvent me) {
        mouseLocation = new Location(me.getPoint());

        if (drag == null) {
            View view = identify(mouseLocation);
            move(view);

            Location p = mouseLocation;
            Location o = view.getAbsoluteLocation();
            p.translate(-o.x, -o.y);
            view.mouseMoved(p);
        }
    }

    /**
     * Responds to the mouse pressed event (with the left button pressed) by
     * initiating a drag.  This sets up the <code>View</code>'s dragging state
     * to the view that the mouse was over when the button was pressed.
     *
     * @see java.awt.event.MouseListener#mousePressed(MouseEvent)
     */
    public void mousePressed(MouseEvent me) {
        drag = null;

        downAt = new Location(me.getPoint());

        // hide a visible popup
        if (isPopupShowing() && ! popup.contains(downAt)) {
        	workspace.clearOverlayView();
            identify(downAt);
        }
        View identified = workspace.identifyView(downAt, null);

        if (identified instanceof AbstractValueView) {
            makeFocus((AbstractValueView) identified);
        }

        canDrag = identified != null && me.getClickCount() == 1; // drag should not be valid after double/triple click
        workspace.setIdentifiedView(identified);
    }

    private boolean isPopupShowing() {
		return workspace.getOverlayView() == popup;
	}

	/**
     * Repsonds to the mouse released event (with the left button pressed) by
     * telling the identified view (the drop zone) that the dragged object is
     * being dropped on it (via the views <code>drop</code> method).  If the
     * drop takes place outside of all of the other views then the
     * <code>workspaceDrop</code> method is called instead to indicate a drop onto the
     * workspace.
     *
     * @see java.awt.event.MouseListener#mouseReleased(MouseEvent)
     */
    public void mouseReleased(MouseEvent me) {
        if (drag != null) {
            View identified = workspace.getIdentifiedView();

            drag.update(me);
            drag.dragEnd(identified);

            workspace.clearOverlayView();

            drag = null;
        }
    }

    public void saveCurrentFieldEntry() {
        if (keyboardFocus != null) {
            KeyboardAccessible focus = keyboardFocus;
            keyboardFocus = null;
            focus.editComplete();
            focus.redraw();
        }
    }

    protected DragView getDraggingView() {
        return (drag == null) ? null : drag.getDragging();
    }

    private View identify(Location location) {
        if (isPopupShowing() && popup.contains(location)) {
            return popup;
        } else {
            return workspace.identifyView(location, null);
        }
    }

    private void move(View mouseOver) {
        View current = workspace.getIdentifiedView();

        if (mouseOver != current) {
            View parent = mouseOver instanceof InternalView
                ? ((InternalView) mouseOver).getParent() : null;

            if (parent == current) {
                LOG.debug("moved into subview from " + current);
                current.enteredSubview();
            } else {
                LOG.debug("exited " + current);
                current.exited();
            }

            workspace.setIdentifiedView(mouseOver);

            if (parent == current) {
                LOG.debug("moved back to from " + current);
                mouseOver.exitedSubview();
            } else {
                LOG.debug("entered " + mouseOver);
                mouseOver.entered();
            }
        }
    }

    private void popupMenu(Click click, View over) {
        Location at = click.getAbsoluteLocation();

        if (over != null) {
            workspace.setIdentifiedView(over);
        }

        LOG.debug("popup over " + over + " " + at);

        Location mouseAt = new Location(at);

        Location p = new Location(at);
        Location l = over.getAbsoluteLocation();
        p.translate(-l.x, -l.y);
        boolean forView = over.indicatesForView(p);

        forView = click.isCtrl() ^ forView;
        boolean extendedSet = click.isShift();
        boolean includeExploration = extendedSet || explorationMode;
        popup.init(over, mouseAt, forView, includeExploration, extendedSet);
        workspace.setOverlayView(popup);

        makeFocus(popup);
    }
}
