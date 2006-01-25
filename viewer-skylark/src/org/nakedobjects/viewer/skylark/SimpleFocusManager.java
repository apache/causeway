package org.nakedobjects.viewer.skylark;

import org.nakedobjects.utility.NakedObjectRuntimeException;


public class SimpleFocusManager implements FocusManager {
    private View focus;

    public View firstView() {
        return null;
    }

    public void focusFirstChildView() {
        View[] views = focus.getSubviews();
        for (int j = 0; j < views.length; j++) {
            if (views[j].canFocus()) {
                setFocus(views[j]);
                return;
            }
        }

        // no other focusable view; stick with the view we've got
        return;
    }

    public void focusNextView() {
        View parent = focus.getParent();
        if (parent == null) {
            return;
        }

        View[] views = parent.getSubviews();
        for (int i = 0; i < views.length; i++) {
            if (views[i] == focus) {
                for (int j = i + 1; j < views.length; j++) {
                    if (views[j].canFocus()) {
                        setFocus(views[j]);
                        return;
                    }
                }
                for (int j = 0; j < i; j++) {
                    if (views[j].canFocus()) {
                        setFocus(views[j]);
                        return;
                    }
                }
                // no other focusable view; stick with the view we've got
                return;
            }
        }

        throw new NakedObjectRuntimeException();
    }

    public void focusParentView() {
        View parent = focus.getParent();
        if (parent == null) {
            return;
        }

        setFocus(parent);
    }

    public void focusPreviousView() {
        View parent = focus.getParent();
        if (parent == null) {
            return;
        }

        View[] views = parent.getSubviews();

        for (int i = 0; i < views.length; i++) {
            if (views[i] == focus) {
                for (int j = i - 1; j >= 0; j--) {
                    if (views[j].canFocus()) {
                        setFocus(views[j]);
                        return;
                    }
                }
                for (int j = views.length - 1; j > i; j--) {
                    if (views[j].canFocus()) {
                        setFocus(views[j]);
                        return;
                    }
                }
                // no other focusable view; stick with the view we've got
                return;
            }
        }

        throw new NakedObjectRuntimeException("Can't move to previous peer from " + focus);
    }

    public View getFocus() {
        return focus;
    }

    public View initialView() {
        return null;
    }

    public View lastView() {
        return null;
    }

    // copied From viewer - remove from there
    public void setFocus(View view) {
        if (view != null && view.canFocus()) {
            if ((focus != null) && (focus != view)) {
                focus.focusLost();
                focus.markDamaged();
            }

            focus = view;
            focus.focusReceived();

            view.markDamaged();
        }
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