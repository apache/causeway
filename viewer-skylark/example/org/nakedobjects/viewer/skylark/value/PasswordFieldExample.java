package org.nakedobjects.viewer.skylark.value;

import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.example.ExampleViewSpecification;
import org.nakedobjects.viewer.skylark.example.TestViews;


public class PasswordFieldExample extends TestViews {
    public static void main(String[] args) {
        new PasswordFieldExample();
    }

    protected void views(Workspace workspace) {
        View parent = new ParentView();

        Content content = new DummyValue("password");
        ViewSpecification specification = new ExampleViewSpecification();
        ViewAxis axis = null;

        PasswordField textField = new PasswordField(content, specification, axis, 20);
        textField.setParent(parent);
        //textField.setMaxWidth(200);
        textField.setLocation(new Location(50, 20));
        textField.setSize(textField.getRequiredSize());
        workspace.addView(textField);

        textField = new PasswordField(content, specification, axis, 20);
        textField.setParent(parent);
    //    textField.setMaxWidth(80);
        textField.setLocation(new Location(50, 80));
        textField.setSize(textField.getRequiredSize());
        workspace.addView(textField);

        content = new DummyValue("pa");
        PasswordField view = new PasswordField(content, specification, axis, 20);
        view.setParent(parent);
    //    view.setMaxWidth(200);
        view.setLocation(new Location(50, 140));
        view.setSize(view.getRequiredSize());
        workspace.addView(view);

        view = new PasswordField(content, specification, axis, 20);
        view.setParent(parent);
       // view.setMaxWidth(80);
        view.setLocation(new Location(50, 250));
        view.setSize(view.getRequiredSize());
        workspace.addView(view);
    }
    
    protected boolean showOutline() {
        return true;
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