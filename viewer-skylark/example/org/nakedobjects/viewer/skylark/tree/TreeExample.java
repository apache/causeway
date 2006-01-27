package org.nakedobjects.viewer.skylark.tree;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.Session;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.reflect.MemberIdentifier;
import org.nakedobjects.object.reflect.OneToOneAssociationImpl;
import org.nakedobjects.object.reflect.OneToOnePeer;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.RootObject;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.example.TestViews;
import org.nakedobjects.viewer.skylark.metal.FormSpecification;
import org.nakedobjects.viewer.skylark.metal.TreeBrowserSpecification;

import test.org.nakedobjects.object.DummyNakedObjectSpecification;
import test.org.nakedobjects.object.reflect.DummyNakedObject;


public class TreeExample extends TestViews {

    public static void main(String[] args) {
        new TreeExample();
    }

    protected void views(Workspace workspace) {
        DummyNakedObject object = new DummyNakedObject();
        object.setupFields(fields());
        object.setupLabel("label");
        object.setupSpecification(new DummyNakedObjectSpecification());

        ViewAxis axis = new TreeBrowserFrame(null, null);
             
        Content content = new RootObject(object);
        
        View view = new TreeBrowserSpecification().createView(content, axis);
        view.setLocation(new Location(100, 50));
        view.setSize(view.getRequiredSize());
        workspace.addView(view);
        
        
        view = new FormSpecification().createView(content, axis);
        view.setLocation(new Location(100, 200));
        view.setSize(view.getRequiredSize());
        workspace.addView(view);
        
    }

    private NakedObjectField[] fields() {
        final DummyNakedObjectSpecification spec = new DummyNakedObjectSpecification();

        OneToOnePeer peer = new OneToOnePeer() {

            public void clearAssociation(NakedObject inObject, NakedObject associate) {}

            public Naked getAssociation(NakedObject inObject) {
                return null;
            }

            public Object getExtension(Class cls) {
                return null;
            }

            
            public MemberIdentifier getIdentifier() {
                return null;
            }

            public NakedObjectSpecification getType() {
                return spec;
            }

            public void initAssociation(NakedObject inObject, NakedObject associate) {}

            public void initValue(NakedObject inObject, Object associate) {}

            public boolean isDerived() {
                return false;
            }

            public boolean isMandatory() {
                return false;
            }
            
            public boolean isEmpty(NakedObject inObject) {
                return false;
            }

            public void setAssociation(NakedObject inObject, NakedObject associate) {}

            public void setValue(NakedObject inObject, Object associate) {}
         
            public Class[] getExtensions() {
                return new Class[0];
            }

            public boolean isObject() {
                return false;
            }

            public Consent isValueValid(NakedObject inObject, NakedValue value) {
                return null;
            }

            public Consent isAssociationValid(NakedObject inObject, NakedObject value) {
                return null;
            }

            public Consent isAvailable(NakedObject target) {
                return null;
            }
            
            public Consent isUsable(NakedObject inObject) {
                return null;
            }

            public String getDescription() {
                return null;
            }

            public Consent isVisible(NakedObject target) {
                return null;
            }

            public String getName() {
                return null;
            }

            public boolean isAuthorised(Session session) {
                return false;
            }

            public boolean isHidden() {
                return false;
            }
        };

        NakedObjectField[] fields = new NakedObjectField[] { new OneToOneAssociationImpl("cls", "fld", spec, peer),
        /* new OneToManyAssociation() */
        };
        return fields;
        
    }

    
    
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