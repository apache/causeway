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

import java.util.Vector;

import junit.framework.TestCase;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.viewer.lightweight.view.EmptyBorder;
	

public class AbstractViewTest extends TestCase {

	private AbstractView view;

	public static void main(String[] args) {
		junit.textui.TestRunner.run(AbstractViewTest.class);
	}

	protected void setUp() throws Exception {
		view = new AbstractView() {

			public Size getRequiredSize() {
				return new Size(80, 120);
			}

			public View makeView(Naked object, Field field) throws CloneNotSupportedException {
				// TODO Auto-generated method stub
				return null;
			}

			public void menuOptions(MenuOptionSet menuOptions) {
				// TODO Auto-generated method stub
				
			}
		};
	}
	
	public void testContains() {
		view.setLocation(new Location(40, 50));
		view.setSize(view.getRequiredSize());
		
		assertTrue(view.contains(new Location(45, 55)));
		assertFalse(view.contains(new Location(35, 55)));
		assertTrue(view.contains(new Location(115, 165)));
		assertFalse(view.contains(new Location(125, 175)));
	}
	
	public void testSetSize() {
		assertEquals("default size", new Size(-1, -1), view.getSize());
		
		view.setSize(new Size(90, 110));
		
		assertEquals(new Size(90, 110), view.getSize());
	}
	
	public void testRequiredSize() {
		assertEquals(new Size(80, 120), view.getRequiredSize());
		
		view.setSize(new Size(90, 110));
		
		assertEquals("required size is unaffected by setting the size", new Size(80, 120), view.getRequiredSize());
	}
	
	public void testPadding() {
		assertEquals("with no border there is no padding", new Padding(0, 0, 0, 0), view.getPadding());
		
		view.setBorder(new EmptyBorder(10, 5));
		
		assertEquals("With a border, the padding should be size of the border", new Padding(5, 10, 5, 10), view.getPadding());
	}
	
	public void testName() {
		assertEquals("AbstractViewTest$1", view.getName());
	}
	
	public void testBounds() {
		view.setLocation(new Location(30, 50));
		view.setSize(new Size(20, 30));
		
		assertEquals(new Location(30, 50), view.getAbsoluteLocation());
		
		assertEquals(new Bounds(30, 50, 20, 30), view.getBounds());
		
		CompositeView parent = new CompositeView(){

			public InternalView[] getComponents() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setLayout(Layout layout) {
				// TODO Auto-generated method stub
				
			}

			public Layout getLayout() {
				// TODO Auto-generated method stub
				return null;
			}

			public void addView(InternalView view) {
				// TODO Auto-generated method stub
				
			}

			public void focusNext(InternalView view) {
				// TODO Auto-generated method stub
				
			}

			public void focusPrevious(InternalView view) {
				// TODO Auto-generated method stub
				
			}

			public void removeView(InternalView view) {
				// TODO Auto-generated method stub
				
			}

			public void replaceView(InternalView toReplace, InternalView replacement) {
				// TODO Auto-generated method stub
				
			}

			public NakedObject getObject() {
				// TODO Auto-generated method stub
				return null;
			}

			public boolean isRoot() {
				// TODO Auto-generated method stub
				return false;
			}

			public void setRootViewIdentified() {
				// TODO Auto-generated method stub
				
			}

			public ObjectViewState getState() {
				// TODO Auto-generated method stub
				return null;
			}

			public void clearRootViewIdentified() {
				// TODO Auto-generated method stub
				
			}

			public void collectionAddUpdate(Object collection, NakedObject element) {
				// TODO Auto-generated method stub
				
			}

			public void collectionRemoveUpdate(Object collection, NakedObject element) {
				// TODO Auto-generated method stub
				
			}

			public void dropView(ViewDrag drag) {
				// TODO Auto-generated method stub
				
			}

			public String objectInfo() {
				// TODO Auto-generated method stub
				return null;
			}

			public boolean objectLocatedAt(Location mouseLocation) {
				// TODO Auto-generated method stub
				return false;
			}

			public void objectMenuOptions(MenuOptionSet options) {
				// TODO Auto-generated method stub
				
			}

			public void objectUpdate(NakedObject object) {
				// TODO Auto-generated method stub
				
			}

			public DragView pickupObject(ObjectDrag drag) {
				// TODO Auto-generated method stub
				return null;
			}

			public DragView pickupView(ViewDrag drag) {
				// TODO Auto-generated method stub
				return null;
			}

			public void removeViewsFor(NakedObject object, Vector toRemove) {
				// TODO Auto-generated method stub
				
			}

			public ObjectView topView() {
				// TODO Auto-generated method stub
				return null;
			}

			public void viewMenuOptions(MenuOptionSet options) {
				// TODO Auto-generated method stub
				
			}

			public Location getAbsoluteLocation() {
				return new Location(100, 160);
			}

			public int getBaseline() {
				// TODO Auto-generated method stub
				return 0;
			}

			public void setBorder(Border border) {
				// TODO Auto-generated method stub
				
			}

			public Border getBorder() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setBounds(Location point, Size size) {
				// TODO Auto-generated method stub
				
			}

			public Bounds getBounds() {
				// TODO Auto-generated method stub
				return null;
			}

			public int getId() {
				// TODO Auto-generated method stub
				return 0;
			}

			public boolean isLayoutInvalid() {
				// TODO Auto-generated method stub
				return false;
			}

			public void setLocation(Location point) {
				// TODO Auto-generated method stub
				
			}

			public Location getLocation() {
				return null;
			}

			public String getName() {
				// TODO Auto-generated method stub
				return null;
			}

			public boolean isOpen() {
				// TODO Auto-generated method stub
				return false;
			}

			public Padding getPadding() {
				// TODO Auto-generated method stub
				return null;
			}

			public CompositeView getParent() {
				// TODO Auto-generated method stub
				return null;
			}

			public boolean isReplaceable() {
				// TODO Auto-generated method stub
				return false;
			}

			public Size getRequiredSize() {
				// TODO Auto-generated method stub
				return null;
			}

			public void setSize(Size size) {
				// TODO Auto-generated method stub
				
			}

			public Size getSize() {
				// TODO Auto-generated method stub
				return null;
			}

			public Workspace getWorkspace() {
				// TODO Auto-generated method stub
				return null;
			}

			public void calculateRepaintArea() {
				// TODO Auto-generated method stub
				
			}

			public boolean contains(Location point) {
				// TODO Auto-generated method stub
				return false;
			}

			public String debugDetails() {
				// TODO Auto-generated method stub
				return null;
			}

			public void dispose() {
				// TODO Auto-generated method stub
				
			}

			public void draw(Canvas canvas) {
				// TODO Auto-generated method stub
				
			}

			public void entered() {
				// TODO Auto-generated method stub
				
			}

			public void enteredSubview() {
				// TODO Auto-generated method stub
				
			}

			public void exited() {
				// TODO Auto-generated method stub
				
			}

			public void exitedSubview() {
				// TODO Auto-generated method stub
				
			}

			public void firstClick(Click click) {
				// TODO Auto-generated method stub
				
			}

			public View identifyView(Location mouseLocationer, View current) {
				// TODO Auto-generated method stub
				return null;
			}

			public boolean indicatesForView(Location mouseLocation) {
				// TODO Auto-generated method stub
				return false;
			}

			public void invalidateLayout() {
				// TODO Auto-generated method stub
				
			}

			public void layout() {
				// TODO Auto-generated method stub
				
			}

			public View makeView(Naked object, Field field) throws CloneNotSupportedException {
				// TODO Auto-generated method stub
				return null;
			}

			public void menuOptions(MenuOptionSet menuOptions) {
				// TODO Auto-generated method stub
				
			}

			public void mouseMoved(Location at) {
				// TODO Auto-generated method stub
				
			}

			public void print(Canvas canvas) {
				// TODO Auto-generated method stub
				
			}

			public void redraw() {
				// TODO Auto-generated method stub
				
			}

			public void secondClick(Click click) {
				// TODO Auto-generated method stub
				
			}

			public void thirdClick(Click click) {
				// TODO Auto-generated method stub
				
			}

			public void validateLayout() {
				// TODO Auto-generated method stub
				
			}

			public void objectMenuReturn(NakedObject object, Location at) {
				// TODO Auto-generated method stub
				
			}
		};
		view.setParent(parent);
		
		assertEquals(new Location(130, 210), view.getAbsoluteLocation());
	}
}
