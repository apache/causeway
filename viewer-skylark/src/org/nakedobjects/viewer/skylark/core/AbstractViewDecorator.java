package org.nakedobjects.viewer.skylark.core;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
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
import org.nakedobjects.viewer.skylark.ViewerAssistant;
import org.nakedobjects.viewer.skylark.Workspace;

public class AbstractViewDecorator implements View {
	protected View wrappedView;
    
	protected AbstractViewDecorator(View wrappedView) {
		this.wrappedView = wrappedView;
		wrappedView.setView(this);
	}

	public void addView(View view) {
		wrappedView.addView(view);
	}

	public boolean canChangeValue() {
		return wrappedView.canChangeValue();
	}

	public boolean canFocus() {
		return wrappedView.canFocus();
	}

    public final String debugDetails() {
        StringBuffer b = new StringBuffer();
        b.append("Decorator: ");
        debugDetails(b);
        b.append("\n           size " + getSize() + "\n");
        b.append("           req'd " + getRequiredSize() + "\n");
        b.append("           padding " + getPadding() + "\n");
        b.append("           baseline " + getBaseline() + "\n");

        b.append("\n");
        b.append(wrappedView.debugDetails());
        return b.toString();
    }

	protected void debugDetails(StringBuffer b) {
		String name = getClass().getName();
		b.append(name.substring(name.lastIndexOf('.') + 1));
	}

	public void dispose() {
		wrappedView.dispose();
	}

	public void drag(InternalDrag drag) {
		wrappedView.drag(drag);
	}

	public void dragCancel(InternalDrag drag) {
		wrappedView.dragCancel(drag);
	}

	public View dragFrom(Location location) {
		return wrappedView.dragFrom(location);
	}

	public void dragIn(ContentDrag drag) {
		wrappedView.dragIn(drag);
	}

	public void dragOut(ContentDrag drag) {
		wrappedView.dragOut(drag);
	}

	public Drag dragStart(DragStart drag) {
        return wrappedView.dragStart(drag);
    }
	
	public void dragTo(InternalDrag drag) {
		wrappedView.dragTo(drag);		
	}

	public void draw(Canvas canvas) {
		wrappedView.draw(canvas);
	}

	public void drop(ContentDrag drag) {
		wrappedView.drop(drag);
	}

	public void drop(ViewDrag drag) {
		wrappedView.drop(drag);
	}

	public void editComplete() {
		wrappedView.editComplete();
	}

	public void entered() {
		wrappedView.entered();
	}

	public void enteredSubview() {
		wrappedView.enteredSubview();
	}

	public void exited() {
		wrappedView.exited();
	}

	public void exitedSubview() {
		wrappedView.exitedSubview();
	}

	public void firstClick(Click click) {
		wrappedView.firstClick(click);
	}

	public void focusLost() {
		wrappedView.focusLost();
	}

	public void focusRecieved() {
		wrappedView.focusRecieved();		
	}

	public int getBaseline() {
		return wrappedView.getBaseline();
	}

	public final Bounds getBounds() {
		return new Bounds(getLocation(), getSize());
	}

	public Content getContent() {
		return wrappedView.getContent();
	}

	public int getId() {
		return wrappedView.getId();
	}
	
	public Location getLocation() {
		return wrappedView.getLocation();
	}

	public Padding getPadding() {
		return wrappedView.getPadding();
	}
    
	public View getParent() {
		return wrappedView.getParent();
	}

	public Size getRequiredSize() {
		return wrappedView.getRequiredSize();
	}

	public Size getSize() {
		return wrappedView.getSize();
	}

	public ViewSpecification getSpecification() {
		return wrappedView.getSpecification();
	}

	public ViewState getState() {
		return wrappedView.getState();
	}

	public View[] getSubviews() {
		return wrappedView.getSubviews();
	}

	public View getView() {
		return wrappedView.getView();
	}

	public ViewAxis getViewAxis() {
		return wrappedView.getViewAxis();
	}
	
	public ViewerAssistant getViewManager() {
		return wrappedView.getViewManager();
	}

	public Workspace getWorkspace() {
		return wrappedView.getWorkspace();
	}

	public boolean hasFocus() {
		return wrappedView.hasFocus();
	}
	
	public void invalidateContent() {
		wrappedView.invalidateContent();
	}

	public void invalidateLayout() {
		wrappedView.invalidateLayout();
	}

	public void keyPressed(int keyCode, int modifiers) {
		wrappedView.keyPressed(keyCode, modifiers);
		}

	public void keyReleased(int keyCode, int modifiers) {
		wrappedView.keyReleased(keyCode, modifiers);
	}

	public void keyTyped(char keyCode) {
		wrappedView.keyTyped(keyCode);
	}

	public void layout() {
		wrappedView.layout();
	}

	public void markDamaged() {
		wrappedView.markDamaged();
	}

	public void markDamaged(Bounds bounds) {
		wrappedView.markDamaged(bounds);
	}

	public void menuOptions(MenuOptionSet menuOptions) {
		wrappedView.menuOptions(menuOptions);
	}

	public void mouseMoved(Location at) {
		wrappedView.mouseMoved(at);
	}

	public void objectActionResult(Naked result, Location at) {
		wrappedView.objectActionResult(result, at);
	}

	public View pickupContent(Location location) {
		return wrappedView.pickupContent(location);
	}
	
	public View pickupView(Location location) {
		return wrappedView.pickupView(location);
	}

	public void print(Canvas canvas) {
		wrappedView.print(canvas);
	}
	
    public void refresh() {
        wrappedView.refresh();
    }

	public void removeView(View view) {
		wrappedView.removeView(view);
	}

	public void replaceView(View toReplace, View replacement) {
		wrappedView.replaceView(toReplace, replacement);
	}

	public void secondClick(Click click) {
		wrappedView.secondClick(click);
	}

	public final void setBounds(Bounds bounds) {
		wrappedView.setBounds(bounds);
	}

	public void setLocation(Location point) {
		wrappedView.setLocation(point);
	}

	public void setParent(View view) {
		wrappedView.setParent(view);
	}

	public void setSize(Size size) {
		wrappedView.setSize(size);
	}
	
	public void setRequiredSize(Size size) {
	    wrappedView.setRequiredSize(size);
	}
	
	public void setView(View view) {
		wrappedView.setView(view);
	}

	public void thirdClick(Click click) {
		wrappedView.thirdClick(click);
	}
    
    public String toString() {
		String name = getClass().getName();
		return wrappedView + "/" + name.substring(name.lastIndexOf('.') + 1);
	}

	public void update(NakedObject object) {
		wrappedView.update(object);
	}

	public ViewAreaType viewAreaType(Location mouseLocation) {
		return wrappedView.viewAreaType(mouseLocation);
	}
  
    public View identify(Location mouseLocation) {
        return wrappedView.identify(mouseLocation);
    }

    public Location getAbsoluteLocation() {
        return wrappedView.getAbsoluteLocation();
    }

    public boolean contains(View view) {
        return wrappedView.contains(view);
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