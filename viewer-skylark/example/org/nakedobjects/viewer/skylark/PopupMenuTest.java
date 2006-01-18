
package org.nakedobjects.viewer.skylark;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.Action.Type;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.viewer.skylark.core.AbstractView;
import org.nakedobjects.viewer.skylark.core.DefaultPopupMenu;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import junit.framework.TestCase;
import test.org.nakedobjects.object.TestSystem;

public class PopupMenuTest extends TestCase {
//	private MockWorkspace workspace;
	private MockPopup popup;
	private PopupTargetView view;
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(PopupMenuTest.class);
	}

	private Click click(int x, int y) {
		return new Click(popup, new Location(x, y), InputEvent.BUTTON1_MASK);
	}

	public void testClickFirstOption() {
		assertFalse(view.action1.executed);
		popup.firstClick(click(10, 6));
		assertTrue(view.action1.executed);
	}

	public void testClickSeparator() {
		popup.firstClick(click(10, 20));
		assertFalse(view.action1.executed);
		assertFalse(view.action3.executed);
		assertFalse(view.action4.executed);
	}

	public void testClickThirdAction() {
		popup.firstClick(click(10, 58));
		assertTrue(view.action4.executed);
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

		assertFalse(view.action1.executed);
		assertTrue(view.action3.executed);
		assertFalse(view.action4.executed);
	}

	public void testStatus() {
		popup.setOption(2);
		assertEquals("status option 3", popup.status);
	}

	protected void setUp() throws Exception {
        new TestSystem().init();
        
		Viewer viewer = new Viewer();
		
		viewer.setRenderingArea(new RenderingArea() {

            public Dimension getSize() {
                return new Dimension(100, 100);
            }

            public Image createImage(int w, int h) {
                return null;
            }

            public Insets getInsets() {
                return new Insets(0,0,0,0);
            }

            public void repaint() {}

            public void repaint(int x, int y, int width, int height) {}

            public void setCursor(Cursor cursor) {}

            public void dispose() {}

            public void setBounds(int i, int j, int k, int l) {}

            public void show() {}

            public void addMouseMotionListener(MouseMotionListener interactionHandler) {}

            public void addMouseListener(MouseListener interactionHandler) {}

            public void addKeyListener(KeyListener interactionHandler) {}}
		);
		
		popup = new MockPopup();
		view = new PopupTargetView(null);
        

        UserActionSet options = new UserActionSet(false, false, UserAction.USER);
       
        
        view.action1 = new MockUserAction("option 1", UserAction.DEBUG);
        options.add(view.action1);
        view.action3 = new MockUserAction("option 3", UserAction.USER);
        options.add(view.action3);
        view.action4 = new MockUserAction("option 4", UserAction.USER);
        options.add(view.action4);
		
		popup.init(view, new UserAction[] {view.action1, view.action3, view.action4}, null);
        
        popup.layout();
	}
		
	private static class MockPopup extends DefaultPopupMenu{
		String status;
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
		protected void showStatus(String status) {
			this.status = status;
		}

        public void dispose() {
        }
        
        public void markDamaged() {
        }
	}
	
	private static class PopupTargetView extends AbstractView {
		protected PopupTargetView(Content content) {
			super(content, new DummyViewSpecification(), null);
		}

		MockUserAction action1;
		MockUserAction action3;
		MockUserAction action4;

		public void contentMenuOptions(UserActionSet options) {
			
		}

		public Size getRequiredSize() {
			return null;
		}
        
        public ViewAreaType viewAreaType(Location location) {
            return ViewAreaType.CONTENT;
        }

		public View makeView(Naked object, NakedObjectField field) throws CloneNotSupportedException {
			return null;
		}
		
		public Workspace getWorkspace() {
            return new Workspace() {

                public View addIconFor(Naked naked, Location at) {
                    return null;
                }

                public View addOpenViewFor(Naked object, Location at) {
                    return null;
                }

                public View createSubviewFor(Naked object, boolean asIcon) {
                    return null;
                }
                
                public void lower(View view) {}

                public void raise(View view) {}

                public void removeViewsFor(NakedObject object) {}

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

                public void contentMenuOptions(UserActionSet menuOptions) {}

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

                public void exited() {}

                public void firstClick(Click click) {}

                public void focusLost() {}

                public void focusReceived() {}

                public Location getAbsoluteLocation() {
                    return new Location();
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
                    return this;
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

                public void viewMenuOptions(UserActionSet menuOptions) {}

                public void mouseDown(Click click) {}

                public void mouseUp(Click click) {}
            };
        }
	}
	

	private static class MockUserAction implements UserAction {
		String name;
		boolean executed = false;
        private final Type type;

		MockUserAction(String name, Type type) {
			this.name = name;
            this.type = type;
		}

		public String getName(View view) {
			return name;
		}

		public Consent disabled(View view) {
			return new Allow("status " + name);
		}

		public void execute(Workspace workspace, View view, Location at) {
			executed = true;
		}
        
        public Type getType() {
            return type;
        }

        public String getDescription(View view) {
            return null;
        }
	}
}

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
