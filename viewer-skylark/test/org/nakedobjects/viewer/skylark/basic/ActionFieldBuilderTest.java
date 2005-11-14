package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.reflect.ActionImpl;
import org.nakedobjects.object.repository.NakedObjectsClient;
import org.nakedobjects.utility.configuration.PropertiesConfiguration;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.special.MockView;
import org.nakedobjects.viewer.skylark.special.SubviewSpec;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.easymock.MockControl;

import test.org.nakedobjects.object.DummyNakedObjectSpecification;
import test.org.nakedobjects.object.reflect.DummyActionPeer;
import test.org.nakedobjects.object.reflect.DummyNakedObject;


public class ActionFieldBuilderTest extends TestCase {
    private ActionFieldBuilder builder;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ActionFieldBuilderTest.class);
    }

    protected void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        SubviewSpec subviewSpec = new SubviewSpec() {
            public View createSubview(Content content, ViewAxis axis) {
                return new MockView();
            }

            public View decorateSubview(View view) {
                return null;
            }
        };

        new NakedObjectsClient().setConfiguration(new PropertiesConfiguration());

        builder = new ActionFieldBuilder(subviewSpec);

    }
    
    public void testUpdateBuild() {
        MockControl control = MockControl.createControl(View.class);
        View view = (View) control.getMock();
        
        control.expectAndDefaultReturn(view.getView(), view);
        control.expectAndDefaultReturn(view.getContent(), null);
        
        /*MockView[] views = new MockView[2];
        views[1] = new MockView();
        views[1].setupContent(new ObjectParameter("name", null, null, false, 1, actionContent));
        view.setupSubviews(views);
        */
        
        control.replay();
        
 //       builder.build(view);
        
        control.verify();
    }
    

/*    // TODO fails on server as cant load X11 for Text class
    public void xxxtestNewBuild() {
        view.setupSubviews(new View[0]);

        view.addAction("add TextView0 null");
        view.addAction("add MockView1/LabelBorder");
        view.addAction("add MockView2/LabelBorder");

        builder.build(view);

        view.verify();
    }
    public void xxxtestUpdateBuildWhereParameterHasChangedFromNullToAnObject() {
        MockView[] views = new MockView[2];
        views[1] = new MockView();
        ObjectParameter objectParameter = new ObjectParameter("name", null, null, false, 1, actionContent);
        views[1].setupContent(objectParameter);
        view.setupSubviews(views);

        actionContent.setParameter(0, new DummyNakedObject());

        view.addAction("replace MockView1 with MockView2/LabelBorder");

        builder.build(view);

        view.verify();
    }

    public void xxxtestUpdateBuildWhereParameterHasChangedFromAnObjectToNull() {
        MockView[] views = new MockView[2];
        views[1] = new MockView();
        ObjectParameter objectParameter = new ObjectParameter("name", new DummyNakedObject(), null, false, 1, actionContent);
        views[1].setupContent(objectParameter);
        view.setupSubviews(views);

        objectParameter.setObject(null);

        view.addAction("replace MockView1 with MockView2/LabelBorder");

        builder.build(view);

        view.verify();
    }

    public void xxxtestUpdateBuildWhereParameterHasChangedFromOneObjectToAnother() {
        MockView[] views = new MockView[2];
        views[1] = new MockView();
        ObjectParameter objectParameter = new ObjectParameter("name", new DummyNakedObject(), null, false, 1, actionContent);
        views[1].setupContent(objectParameter);
        view.setupSubviews(views);

        objectParameter.setObject(new DummyNakedObject());

        view.addAction("replace MockView1 with MockView2/LabelBorder");

        builder.build(view);

        view.verify();
    }

    public void xxtestUpdateBuildWhereParameterObjectSetButToSameObject() {
        MockView[] views = new MockView[2];
        views[1] = new MockView();
        DummyNakedObject dummyNakedObject = new DummyNakedObject();
        ObjectParameter objectParameter = new ObjectParameter("name", dummyNakedObject, null, false, 1, actionContent);
        views[1].setupContent(objectParameter);
        view.setupSubviews(views);

        actionContent.setParameter(0, dummyNakedObject);
        // objectParameter.setObject(dummyNakedObject);

        builder.build(view);

        view.verify();
    }

}

class MockActionHelper extends ActionHelper {

    protected MockActionHelper(
            NakedObject target,
            Action action,
            String[] labels,
            Naked[] parameters,
            NakedObjectSpecification[] parameterTypes,
            boolean[] required) {
        super(target, action, labels, parameters, parameterTypes, required);
    }
*/
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