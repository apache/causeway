package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.ContentDrag;
import org.nakedobjects.viewer.skylark.InternalDrag;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.MenuOptionSet;
import org.nakedobjects.viewer.skylark.Padding;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAreaType;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewDrag;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.ViewState;
import org.nakedobjects.viewer.skylark.ViewerAssistant;
import org.nakedobjects.viewer.skylark.Workspace;

public class MockView implements View
{

    public void addView(View view) {}

    public boolean canChangeValue() {
        return false;
    }

    public boolean canFocus() {
        return false;
    }

    public String debugDetails() {
        return null;
    }

    public void dispose() {}

    public void drag(InternalDrag drag) {}

    public void dragCancel(InternalDrag drag) {}

    public View dragFrom(InternalDrag drag) {
        return null;
    }

    public void dragIn(ContentDrag drag) {}

    public void dragOut(ContentDrag drag) {}

    public void dragTo(InternalDrag drag) {}

    public void draw(Canvas canvas) {}

    public void drop(ContentDrag drag) {}

    public void drop(ViewDrag drag) {}

    public void editComplete() {}

    public void entered() {}

    public void enteredSubview() {}

    public void exited() {}

    public void exitedSubview() {}

    public void firstClick(Click click) {}

    public void focusLost() {}

    public void focusRecieved() {}

    public int getBaseline() {
        return 0;
    }

    public Bounds getBounds() {
        return null;
    }

    public Content getContent() {
        return null;
    }

    public int getId() {
        return 0;
    }

    public Location getLocation() {
        return null;
    }

    public Location getLocationWithinViewer() {
        return null;
    }

    public Padding getPadding() {
        return null;
    }

    public View getParent() {
        return null;
    }

    public Size getRequiredSize() {
        return null;
    }

    public Size getSize() {
        return null;
    }

    public ViewSpecification getSpecification() {
        return null;
    }

    public ViewState getState() {
        return null;
    }

    public View[] getSubviews() {
        return null;
    }

    public View getView() {
        return null;
    }

    public ViewAxis getViewAxis() {
        return null;
    }

    public ViewerAssistant getViewManager() {
        return null;
    }

    public Workspace getWorkspace() {
        return null;
    }

    public boolean hasFocus() {
        return false;
    }

    public void invalidateContent() {}

    public void invalidateLayout() {}

    public void keyPressed(int keyCode, int modifiers) {}

    public void keyReleased(int keyCode, int modifiers) {}

    public void keyTyped(char keyCode) {}

    public void layout() {}

    public void markDamaged() {}

    public void markDamaged(Bounds bounds) {}

    public void menuOptions(MenuOptionSet menuOptions) {}

    public void mouseMoved(Location at) {}

    public void objectActionResult(Naked result, Location at) {}

    public View pickup(ContentDrag drag) {
        return null;
    }

    public View pickup(ViewDrag drag) {
        return null;
    }

    public void print(Canvas canvas) {}

    public void refresh() {}

    public void removeView(View view) {}

    public void replaceView(View toReplace, View replacement) {}

    public void secondClick(Click click) {}

    public void setBounds(Bounds bounds) {}

    public void setLocation(Location point) {}

    public void setParent(View view) {}

    public void setSize(Size size) {}

    public void setView(View view) {}

    public void thirdClick(Click click) {}

    public void update(NakedObject object) {}

    public ViewAreaType viewAreaType(Location mouseLocation) {
        return null;
    }

    public void setRequiredSize(Size size) {}

    public View identify(Location location) {
        return null;
    }

}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

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