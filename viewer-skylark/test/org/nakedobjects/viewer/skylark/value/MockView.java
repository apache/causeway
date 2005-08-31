package org.nakedobjects.viewer.skylark.value;

import org.nakedobjects.object.Naked;
import org.nakedobjects.viewer.skylark.Bounds;
import org.nakedobjects.viewer.skylark.Canvas;
import org.nakedobjects.viewer.skylark.Click;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.ContentDrag;
import org.nakedobjects.viewer.skylark.Drag;
import org.nakedobjects.viewer.skylark.DragStart;
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
import org.nakedobjects.viewer.skylark.Viewer;
import org.nakedobjects.viewer.skylark.Workspace;

public class MockView implements View {

    public MockView() {
        super();
    }

    public void addView(View view) {}

    public boolean canChangeValue() {
        return false;
    }

    public boolean canFocus() {
        return false;
    }

    public boolean contains(View view) {
        return false;
    }

    public void contentMenuOptions(MenuOptionSet menuOptions) {}

    public String debugDetails() {
        return null;
    }

    public void dispose() {}

    public void drag(InternalDrag drag) {}

    public void dragCancel(InternalDrag drag) {}

    public View dragFrom(Location location) {
        return null;
    }

    public void dragIn(ContentDrag drag) {}

    public void dragOut(ContentDrag drag) {}

    public Drag dragStart(DragStart drag) {
        return null;
    }

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

    public void focusReceived() {}

    public Location getAbsoluteLocation() {
        return null;
    }

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

    public Padding getPadding() {
        return new Padding();
    }

    public View getParent() {
        return null;
    }

    public Size getRequiredSize() {
        return new Size();
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

    public Viewer getViewManager() {
        return null;
    }

    public Workspace getWorkspace() {
        return null;
    }

    public boolean hasFocus() {
        return false;
    }

    public View identify(Location mouseLocation) {
        return null;
    }

    public void invalidateContent() {}

    public void invalidateLayout() {}

    public void keyPressed(int keyCode, int modifiers) {}

    public void keyReleased(int keyCode, int modifiers) {}

    public void keyTyped(char keyCode) {}

    public void layout() {}

    public void limitBoundsWithin(Bounds bounds) {}

    public void markDamaged() {}

    public void markDamaged(Bounds bounds) {}

    public void mouseMoved(Location location) {}

    public void objectActionResult(Naked result, Location at) {}

    public View pickupContent(Location location) {
        return null;
    }

    public View pickupView(Location location) {
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

    public void setRequiredSize(Size size) {}

    public void setSize(Size size) {}

    public void setView(View view) {}

    public View subviewFor(Location location) {
        return null;
    }
    
    public void thirdClick(Click click) {}

    public void update(Naked object) {}

    public void updateView() {}

    public ViewAreaType viewAreaType(Location mouseLocation) {
        return null;
    }

    public void viewMenuOptions(MenuOptionSet menuOptions) {}

}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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