/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered
 * address of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking
 * GU21 1NR, UK).
 */

package org.nakedobjects.viewer.skylark;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class ViewManagerTest extends TestCase {
//	private final static Component COMPONENT = new Component() {
//	};

	public static void main(String[] args) {
		TestRunner.run(ViewManagerTest.class);
	}
	
	public void testNone() {}
	
/*
	public void testMouseClick() {
		MockWorkspace workspace = new MockWorkspace(null);
		MockView view = new MockView();
		workspace.setupIdentifyView(view);

		ViewManager manager = new ViewManager(workspace, null);

		view.setupGetAbsoluteLocation(new Location(10, 10));
		view.setupIndicatesForView(true);
		view.setExpectedFirstClickCalls(1);

		manager.mouseClicked(createMouseEvent(10, 20, 1, MouseEvent.BUTTON1_MASK));

		view.verify();
	}

	public void testPopupMouseClick() {
		MockWorkspace workspace = new MockWorkspace(null);
		MockView view = new MockView();
		workspace.setupIdentifyView(view);

		MockPopupMenu popup = new MockPopupMenu();
		ViewManager manager = new ViewManager(workspace, popup);

		view.setupGetAbsoluteLocation(new Location(10, 10));
		view.setupIndicatesForView(true);

		workspace.addExpectedSetIdentifiedViewValues(view);

		popup.addExpectedInitValues(view, true, true);
		view.setupGetAbsoluteLocation(new Location(10, 10));
		view.setupIndicatesForView(true);
//		popup.setupGetParent(null);
//		popup.setupIndicatesForView(true);

		manager.mouseClicked(createMouseEvent(10, 20, 1, MouseEvent.BUTTON3_MASK));

		popup.verify();
		view.verify();

		popup.setExpectedFirstClickCalls(1);
}

	private MouseEvent createMouseEvent(int x, int y, int count, int button) {
		return new MouseEvent(COMPONENT, 0, 0, button, x, y, count, false);
	}
	*/
}
