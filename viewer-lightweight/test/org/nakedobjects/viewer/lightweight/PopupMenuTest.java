/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
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
package org.nakedobjects.viewer.lightweight;

import java.awt.event.KeyEvent;

import junit.framework.TestCase;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.viewer.lightweight.Style.Text;

public class PopupMenuTest extends TestCase {
	private MockUserAction action1;
	private MockUserAction action3;
	private MockUserAction action4;
	private MockWorkspace workspace;
	private PopupMenu popup;

	public static void main(String[] args) {
		junit.textui.TestRunner.run(PopupMenuTest.class);
	}

	public void testClickFirstOption() {
		assertFalse(action1.executed);
		popup.firstClick(new MockClick(popup, 10, 8));
		assertTrue(action1.executed);
	}

	public void testClickSeparator() {
		popup.firstClick(new MockClick(popup, 10, 20));
		assertFalse(action1.executed);
		assertFalse(action3.executed);
		assertFalse(action4.executed);
	}

	public void testClickThirdAction() {
		popup.firstClick(new MockClick(popup, 10, 58));
		assertTrue(action4.executed);
	}

	public void testDefaultSelectedAtStartup() {
		assertEquals(0, popup.getOption());
	}

	public void testMouseMoveOutsideBounds() {
		popup.mouseMoved(new Location(10, 70));
		assertEquals(3, popup.getOption());

		popup.mouseMoved(new Location(10, -10));
		assertEquals(0, popup.getOption());
	}

	public void testMouseMoveOverFirstOption() {
		popup.mouseMoved(new Location(10, 2));
		assertEquals(0, popup.getOption());
	}

	public void testMouseMoveOverSecondOption() {
		popup.mouseMoved(new Location(10, 18));
		assertEquals(1, popup.getOption());
	}

	public void testOptionCount() {
		assertEquals(4, popup.getOptionCount());
	}

	public void testKeyUpDown() {
		popup.keyPressed(KeyEvent.VK_DOWN, 0);
		assertEquals(2, popup.getOption());

		popup.keyPressed(KeyEvent.VK_DOWN, 0);
		assertEquals(3, popup.getOption());

		popup.keyPressed(KeyEvent.VK_DOWN, 0);
		assertEquals(3, popup.getOption());

		popup.keyPressed(KeyEvent.VK_UP, 0);
		assertEquals(2, popup.getOption());

		popup.keyPressed(KeyEvent.VK_UP, 0);
		assertEquals(0, popup.getOption());

		popup.keyPressed(KeyEvent.VK_UP, 0);
		assertEquals(0, popup.getOption());
	}

	public void testKeyInvoke() {
		popup.keyPressed(KeyEvent.VK_DOWN, 0);
		popup.keyPressed(KeyEvent.VK_ENTER, 0);

		assertFalse(action1.executed);
		assertTrue(action3.executed);
		assertFalse(action4.executed);
	}

	public void testStatus() {
		popup.setOption(2);
		assertEquals("status option 3", workspace.status);
	}

	protected void setUp() throws Exception {
		popup = new PopupMenu(){
			protected Color normalColor() {
				return Color.NULL;
			}
			protected Color reverseColor() {
				return Color.NULL;
			}
			protected Color disabledColor() {
				return Color.NULL;
			}
			protected Text style() {
				return Style.DEBUG;
			}
		};
		workspace = new MockWorkspace();
		PopupMenu.setWorkspace(workspace);

		View view = new AbstractView() {
			public void menuOptions(MenuOptionSet options) {
				action1 = new MockUserAction("option 1");
				options.add(MenuOptionSet.OBJECT, action1);
				options.add(MenuOptionSet.OBJECT, null); // effectively
																			 // action
																			 // 4
				action3 = new MockUserAction("option 3");
				options.add(MenuOptionSet.OBJECT, action3);
				action4 = new MockUserAction("option 4");
				options.add(MenuOptionSet.OBJECT, action4);
			}

			public Size getRequiredSize() {
				// TODO Auto-generated method stub
				return null;
			}

			public View makeView(Naked object, Field field) throws CloneNotSupportedException {
				// TODO Auto-generated method stub
				return null;
			}
		};
		
		popup.init(view, new Location(0,0), true, true, true);
	}

	private static class MockWorkspace extends Workspace {
		Bounds bounds;
		String status;

		MockWorkspace() {
			super(null);
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public void repaint(int x, int y, int width, int height) {
			bounds = new Bounds(x, y, width, height);
		}
	}

	private static class MockUserAction implements UserAction {
		String name;
		boolean executed;

		MockUserAction(String name) {
			this.name = name;
		}

		public String getName(Workspace workspace, View view, Location at) {
			return name;
		}

		public Permission disabled(Workspace workspace, View view, Location location) {
			return new Allow("status " + name);
		}

		public void execute(Workspace workspace, View view, Location at) {
			executed = true;
		}
	}
}
