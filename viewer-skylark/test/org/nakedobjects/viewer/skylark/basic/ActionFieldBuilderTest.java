package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.DummyNakedObject;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.special.MockView;
import org.nakedobjects.viewer.skylark.special.SubviewSpec;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import junit.framework.TestCase;


public class ActionFieldBuilderTest extends TestCase {

    private MockView view;
    private ActionFieldBuilder builder;
    private ActionHelper actionContent;

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

        MockView.next = 0;
        ClientSession.setSession(new Session());
        NakedObjects.setConfiguration(new Configuration());

        builder = new ActionFieldBuilder(subviewSpec);

        DummyNakedObject object = new DummyNakedObject();
        MockActionPeer mockActionPeer = new MockActionPeer();
        mockActionPeer.setUpParamterTypes(new NakedObjectSpecification[] { new DummyNakedObjectSpecification(), new DummyNakedObjectSpecification() });
        Action action = new Action("cls name", "method name", mockActionPeer);
        actionContent = new ActionHelper(object, action);

        view = new MockView();
        view.setUpContent(new ActionContent(actionContent));
    }

    public void testNewBuild() {
        view.setUpSubviews(new View[0]);

        view.addAction("add TextView0 null");
        view.addAction("add MockView1/LabelBorder");
        view.addAction("add MockView2/LabelBorder");

        builder.build(view);

        view.verify();
    }
    
    public void testUpdateBuild() {
        MockView[] views = new MockView[2];
        views[1] = new MockView();
        views[1].setUpContent(new ObjectParameter("name", null, null, 1, actionContent));
        view.setUpSubviews(views);

        builder.build(view);

        view.verify();
    }


    
    public void testUpdateBuildWhereParameterHasChangedFromNullToAnObject() {
        MockView[] views = new MockView[2];
        views[1] = new MockView();
        ObjectParameter objectParameter = new ObjectParameter("name", null, null, 1, actionContent);
        views[1].setUpContent(objectParameter);
        view.setUpSubviews(views);
        
        actionContent.setParameter(0, new DummyNakedObject());
        
        view.addAction("replace MockView1 with MockView2/LabelBorder");

        builder.build(view);

        view.verify();
    }

    
    public void testUpdateBuildWhereParameterHasChangedFromAnObjectToNull() {
        MockView[] views = new MockView[2];
        views[1] = new MockView();
        ObjectParameter objectParameter = new ObjectParameter("name", new DummyNakedObject(), null, 1, actionContent);
        views[1].setUpContent(objectParameter);
        view.setUpSubviews(views);
        
        objectParameter.setObject(null);

        view.addAction("replace MockView1 with MockView2/LabelBorder");

        builder.build(view);

        view.verify();
    }

    public void testUpdateBuildWhereParameterHasChangedFromOneObjectToAnother() {
        MockView[] views = new MockView[2];
        views[1] = new MockView();
        ObjectParameter objectParameter = new ObjectParameter("name", new DummyNakedObject(), null, 1, actionContent);
        views[1].setUpContent(objectParameter);
        view.setUpSubviews(views);
        
        objectParameter.setObject(new DummyNakedObject());

        view.addAction("replace MockView1 with MockView2/LabelBorder");

        builder.build(view);

        view.verify();
    }

    public void testUpdateBuildWhereParameterObjectSetButToSameObject() {
        MockView[] views = new MockView[2];
        views[1] = new MockView();
        DummyNakedObject dummyNakedObject = new DummyNakedObject();
        ObjectParameter objectParameter = new ObjectParameter("name", dummyNakedObject, null, 1, actionContent);
        views[1].setUpContent(objectParameter);
        view.setUpSubviews(views);
        
        actionContent.setParameter(0, dummyNakedObject);
 //       objectParameter.setObject(dummyNakedObject);

        builder.build(view);

        view.verify();
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