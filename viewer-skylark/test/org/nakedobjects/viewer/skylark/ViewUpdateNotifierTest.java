package org.nakedobjects.viewer.skylark;

import org.nakedobjects.NakedObjectsClient;
import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.object.MockNakedObject;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.viewer.skylark.special.MockView;

import java.util.Vector;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class ViewUpdateNotifierTest extends TestCase {

    private ExposedViewUpdateNotifier notifier;
    private MockNakedObject object;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ViewUpdateNotifierTest.class);
    }

    protected void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);


        new NakedObjectsClient().setConfiguration(new Configuration());
        

        notifier = new ExposedViewUpdateNotifier();

        object = new MockNakedObject();
    }

    private MockView createView(NakedObject object) {
        MockView view = new MockView();
        view.setupContent(new RootObject(object));
        return view;
    }

    public void testAddViewWithNonObjectContent() {
        MockView view = createView(null);
        notifier.add(view);
        notifier.assertEmpty();
    }

    public void testAddViewWithObjectContent() {
        MockView view = createView(object);
        notifier.add(view);
        notifier.assertContainsViewForObject(view, object);
    }

    public void testRemoveView() {
        Vector vector = new Vector();
        MockView view = createView(object);
        vector.addElement(view);
        notifier.setupViewsForObject(object, vector);
        
        notifier.remove(view);
        notifier.assertEmpty();
    }

    public void testViewDirty() {
        
         Vector vector = new Vector();
        MockView view1 = createView(object);
        vector.addElement(view1);
        
        MockView view2 = createView(object);
        vector.addElement(view2);
        
        notifier.setupViewsForObject(object, vector);

        notifier.invalidateViewsForChangedObjects();
        assertFalse(view1.invalidateContentCalled);
        assertFalse(view2.invalidateContentCalled);

        notifier.addDirty(object);
        notifier.invalidateViewsForChangedObjects();
        assertTrue(view1.invalidateContentCalled);
        assertTrue(view2.invalidateContentCalled);
    }
}

class ExposedViewUpdateNotifier extends ViewUpdateNotifier {

    public void assertContainsViewForObject(View view, NakedObject object) {
        Assert.assertTrue(views.containsKey(object));
        Vector viewsForObject = (Vector) views.get(object);
        Assert.assertTrue(viewsForObject.contains(view));
    }

    public void setupViewsForObject(NakedObject object, Vector vector) {
        views.put(object, vector);
    }

    public void assertEmpty() {
        Assert.assertTrue("Not empty", views.isEmpty());
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */