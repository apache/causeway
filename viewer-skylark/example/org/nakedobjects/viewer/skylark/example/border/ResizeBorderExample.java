package org.nakedobjects.viewer.skylark.example.border;

import org.nakedobjects.NakedObjectsClient;
import org.nakedobjects.container.configuration.Configuration;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.RootObject;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.example.ExampleViewSpecification;
import org.nakedobjects.viewer.skylark.example.TestObjectViewWithDragging;
import org.nakedobjects.viewer.skylark.example.TestViews;
import org.nakedobjects.viewer.skylark.metal.TextFieldResizeBorder;
import org.nakedobjects.viewer.skylark.tree.TreeBrowserResizeBorder;


public class ResizeBorderExample extends TestViews {

    public static void main(String[] args) {
        new ResizeBorderExample();
    }

    protected void views(Workspace workspace) {
        Configuration config = new Configuration();
        config.add("" , "");
        new NakedObjectsClient().setConfiguration(config);
        
        NakedObject object = createExampleObjectForView();
        Content content = new RootObject(object);
        ViewSpecification specification = new ExampleViewSpecification();
        ViewAxis axis = null;

        View view = new TextFieldResizeBorder(new TestObjectViewWithDragging(content, specification, axis, 400, 400, "resizing"));
        view.setLocation(new Location(50, 60));
        view.setSize(new Size(100, 24));
        workspace.addView(view);

        view = new TreeBrowserResizeBorder(new TestObjectViewWithDragging(content, specification, axis, 400, 400, "resizing"));
        view.setLocation(new Location(50, 120));
        view.setSize(new Size(200, 200));
        workspace.addView(view);
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