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

import junit.framework.TestCase;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.viewer.lightweight.AbstractView;
import org.nakedobjects.viewer.lightweight.InternalView;
import org.nakedobjects.viewer.lightweight.LayoutTarget;
import org.nakedobjects.viewer.lightweight.ObjectView;
import org.nakedobjects.viewer.lightweight.Padding;
import org.nakedobjects.viewer.lightweight.Size;
import org.nakedobjects.viewer.lightweight.View;
import org.nakedobjects.viewer.lightweight.view.RowLayout;


public class RowLayoutTest extends TestCase {
    private LayoutTarget target;
    private RowLayout layout;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(RowLayoutTest.class);
    }

    public void testDefaultSizes() {
		Size size = layout.requiredSize(target); // must be called first
		int width = 3 * 90 + 3 * 5;
		assertEquals(new Size(width, 16), size);

		int[] positions = layout.getPositions();
        assertEquals(3, positions.length);
        assertEquals(95, positions[0]);
        assertEquals(190, positions[1]);
        assertEquals(285, positions[2]);
    }

	public void testExtendFirstColumn() {
		layout.layout(target); // must be called first
		layout.extendColumn(0, 45);
		Size size = layout.requiredSize(target);
		int width = 45 + 2 * 90 + 3 * 5; // total reduces
		assertEquals(new Size(width, 16), size);

		int[] positions = layout.getPositions();
		assertEquals(50, positions[0]);
		assertEquals(145, positions[1]);
		assertEquals(240, positions[2]);

		layout.extendColumn(0, 125);
		size = layout.requiredSize(target);
		width = 125 + 2 * 90 + 3 * 5; // total increases
		assertEquals(new Size(width, 16), size);

		positions = layout.getPositions();
		assertEquals(130, positions[0]);
		assertEquals(225, positions[1]);
		assertEquals(320, positions[2]);
	}

	public void testExtendMiddleColumn() {
		layout.layout(target); // must be called first
		layout.extendColumn(1, 140);
		Size size = layout.requiredSize(target);
		int width = 90 + 45 + 90 + 3 * 5; // total reduces
		assertEquals(new Size(width, 16), size);

		int[] positions = layout.getPositions();
		assertEquals(95, positions[0]);
		assertEquals(145, positions[1]);
		assertEquals(240, positions[2]);

		layout.extendColumn(1, 195);
		size = layout.requiredSize(target);
		width = 90 + 100 + 90 + 3 * 5; // total increases
		assertEquals(new Size(width, 16), size);

		positions = layout.getPositions();
		assertEquals(95, positions[0]);
		assertEquals(200, positions[1]);
		assertEquals(295, positions[2]);
	}
/*
	public void testExtendMiddleColumnTooFar() {
		layout.layout(target); // must be called first
		layout.extendColumn(1, 70); // is into previous colum
//		Size size = layout.requiredSize(target);
//		int width = 90 + 45 + 90 + 3 * 5; // total reduces
//		assertEquals(new Size(width, 16), size);

		int[] positions = layout.getPositions();
		assertEquals(65, positions[0]);
		assertEquals(80, positions[1]);
		assertEquals(175, positions[2]);

/*		layout.extendColumn(1, 195);
		size = layout.requiredSize(target);
		width = 90 + 100 + 90 + 3 * 5; // total increases
		assertEquals(new Size(width, 16), size);

		positions = layout.getPositions();
		assertEquals(95, positions[0]);
		assertEquals(200, positions[1]);
		assertEquals(295, positions[2]);
	* /}

*/
	
	public void testSetFirstColumnSize() {
		layout.layout(target); // must be called first
		layout.setColumnSize(0, 45);
		Size size = layout.requiredSize(target);
		int width = 3 * 90 + 3 * 5; // total remains same
		assertEquals(new Size(width, 16), size);

		int[] positions = layout.getPositions();
		assertEquals(50, positions[0]);
		assertEquals(190, positions[1]);
		assertEquals(285, positions[2]);

		layout.setColumnSize(0, 125);
		size = layout.requiredSize(target);
		width = 3 * 90 + 3 * 5; // total remains same
		assertEquals(new Size(width, 16), size);

		positions = layout.getPositions();
		assertEquals(130, positions[0]);
		assertEquals(190, positions[1]);
		assertEquals(285, positions[2]);
	}

    public void testSetLastColumnSize() {
		layout.layout(target); // must be called first
		layout.setColumnSize(2, 200);
        Size size = layout.requiredSize(target);
        int width = 90 + 90 + 200 + 3 * 5;
        assertEquals(new Size(width, 16), size);

        int[] positions = layout.getPositions();
        assertEquals(95, positions[0]);
        assertEquals(190, positions[1]);
        assertEquals(395, positions[2]);
    }

    protected void setUp() throws Exception {
        layout = new RowLayout(5);
        target = new LayoutTarget() {
                    public InternalView[] getComponents() {
                        InternalView[] views = new InternalView[3];
                        views[0] = new TestView(100, 10);
                        views[1] = new TestView(120, 16);
                        views[2] = new TestView(80, 15);

                        return views;
                    }

                    public void setLayoutValid() {
                    }

                    public Padding getPadding() {
                        return new Padding();
                    }
                };
    }

    private class TestView extends AbstractView implements InternalView {
        Size size;

        TestView(int w, int h) {
            this.size = new Size(w, h);
        }

        public Field getFieldOf() {
            return null;
        }

        public Size getRequiredSize() {
            return size;
        }

        public View getRoot() {
            return null;
        }

        public View makeView(Naked object, Field field)
            throws CloneNotSupportedException {
            return null;
        }

        public ObjectView parentObjectView() {
            return null;
        }
    }
}
