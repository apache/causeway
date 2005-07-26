package org.nakedobjects.viewer.skylark.example.border;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.RootObject;
import org.nakedobjects.viewer.skylark.UserAction;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.example.ExampleViewSpecification;
import org.nakedobjects.viewer.skylark.example.TestObjectView;
import org.nakedobjects.viewer.skylark.example.TestViews;
import org.nakedobjects.viewer.skylark.metal.ButtonBorder;


public class ButtonBorderExample extends TestViews {

    public static void main(String[] args) {
        new ButtonBorderExample();
    }

    protected void views(Workspace workspace) {
        UserAction[] actions = new UserAction[] { new UserAction() {

            public Consent disabled(View view) {
                return Allow.DEFAULT;
            }

            public void execute(Workspace workspace, View view, Location at) {
                view.getViewManager().setStatus("Button 1 pressed");
            }

            public String getDescription(View view) {
                return "Button that can be pressed";
            }

            public String getName(View view) {
                return "Action";
            }
        },

        new UserAction() {

            public Consent disabled(View view) {
                return Veto.DEFAULT;
            }

            public void execute(Workspace workspace, View view, Location at) {
                view.getViewManager().setStatus("Button 2 pressed");
            }

            public String getDescription(View view) {
                return "Button that can't be pressed";
            }

            public String getName(View view) {
                return "Disabled";
            }
        } };

        NakedObject object = createExampleObjectForView();
        Content content = new RootObject(object);
        ViewSpecification specification = new ExampleViewSpecification();
        ViewAxis axis = null;

        View view = new ButtonBorder(actions, new TestObjectView(content, specification, axis, 200, 80, "VIEW in border"));

        view.setLocation(new Location(100, 100));
        view.setSize(view.getRequiredSize());
        workspace.addView(view);

    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */