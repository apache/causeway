package org.nakedobjects.viewer.skylark;

import org.nakedobjects.object.NakedObjectRuntimeException;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;


public class KeyboardManager {
    private final Viewer viewer;

    public KeyboardManager(Viewer viewer) {
        this.viewer = viewer;
    }

    private View getFocus() {
        View focus = viewer.getFocus();
        return focus == null ? null : focus.getView();
    }

    /*
     * At the moment, as a fudge, the text field is calling its parent's keyPressed method for enter presses.
     */
    public void pressed(final int keyCode, final int modifiers) {
        View keyboardFocus = getFocus();
        if (keyboardFocus != null) {
            keyboardFocus.keyPressed(keyCode, modifiers);
        }

        int action = 0;

        if (keyCode == KeyEvent.VK_F1) {
            help(keyboardFocus);
        } else if (keyCode == KeyEvent.VK_TAB) {
            action = tab(modifiers);
        }

        switch (action) {
        case KeyboardAction.NEXT_VIEW:
            focusNextSubview(keyboardFocus);
            break;
        case KeyboardAction.PREVIOUS_VIEW:
            focusPreviousSubview(keyboardFocus);
            break;
        case KeyboardAction.NEXT_WINDOW:
            focusNextRootView(keyboardFocus);
            break;
        case KeyboardAction.PREVIOUS_WINDOW:
            break;
        }
    }

    private int tab(final int modifiers) {
        int action;
        if ((modifiers & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK) {
            if ((modifiers & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK) {
                action = KeyboardAction.PREVIOUS_WINDOW;
            } else {
                action = KeyboardAction.NEXT_WINDOW;
            }
        } else {
            if ((modifiers & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK) {
                action = KeyboardAction.PREVIOUS_VIEW;
            } else {
                action = KeyboardAction.NEXT_VIEW;
            }
        }
        return action;
    }

    public void released(final int keyCode, final int modifiers) {
        View keyboardFocus = getFocus();
        if (keyboardFocus != null) {
            //LOG.debug("released " + keyCode);
            keyboardFocus.keyReleased(keyCode, modifiers);
        }
    }

    public void typed(final char keyChar) {
        View keyboardFocus = getFocus();
        if (keyboardFocus != null) {
            if (!Character.isISOControl(keyChar)) {
                keyboardFocus.keyTyped(keyChar);
            }
        }
    }

    private void help(final View over) {
        if (over != null) {
            viewer.clearStatus();
            viewer.clearOverlayView();
            View helpView = new HelpView(over);
            helpView.setSize(helpView.getRequiredSize());
            Location location = over.getAbsoluteLocation();
            location.add(20, 20);
            helpView.setLocation(location);

            viewer.setOverlayView(helpView);
        }
    }

    private void focusNextRootView(final View focus) {
        View parent = focus.getParent();
        if (parent == null) {
            return;
        }
        
        View[] views = parent.getSubviews();
        for (int i = 0; i < views.length; i++) {
            if (views[i] == focus) {
                for (int j = i + 1; j < views.length; j++) {
                    if (views[j].canFocus()) {
                        viewer.makeFocus(views[j]);
                        return;
                    }
                }
                for (int j = 0; j < i; j++) {
                    if (views[j].canFocus()) {
                        viewer.makeFocus(views[j]);
                        return;
                    }
                }
                // no other focusable view; stick with the view we've got
                return;
            }
        }

        throw new NakedObjectRuntimeException();
    }

    private void focusNextSubview(final View focus) {
        View parent = focus.getParent();
        if (parent == null) {
            return;
        }
        
        View[] views = parent.getSubviews();
        for (int i = 0; i < views.length; i++) {
            if (views[i] == focus) {
                for (int j = i + 1; j < views.length; j++) {
                    if (views[j].canFocus()) {
                        viewer.makeFocus(views[j]);
                        return;
                    }
                }
                for (int j = 0; j < i; j++) {
                    if (views[j].canFocus()) {
                        viewer.makeFocus(views[j]);
                        return;
                    }
                }
                // no other focusable view; stick with the view we've got
                return;
            }
        }

        throw new NakedObjectRuntimeException();
    }

    private void focusPreviousSubview(final View focus) {
        View[] views = focus.getParent().getSubviews();

        for (int i = 0; i < views.length; i++) {
            if (views[i] == focus) {
                for (int j = i - 1; j >= 0; j--) {
                    if (views[j].canFocus()) {
                        viewer.makeFocus(views[j]);
                        return;
                    }
                }
                for (int j = views.length - 1; j > i; j--) {
                    if (views[j].canFocus()) {
                        viewer.makeFocus(views[j]);
                        return;
                    }
                }
                // no other focusable view; stick with the view we've got
                return;
            }
        }

        throw new NakedObjectRuntimeException();
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */