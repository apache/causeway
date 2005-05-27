package org.nakedobjects.viewer.skylark.tree;

import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.RootObject;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.example.ExampleObjectForView;
import org.nakedobjects.viewer.skylark.example.TestViews;
import org.nakedobjects.viewer.skylark.tree.CollectionLeafNodeSpecification;
import org.nakedobjects.viewer.skylark.tree.TreeBrowserFrame;


public class TreeLeafNodeExample extends TestViews {

    public static void main(String[] args) {
        new TreeLeafNodeExample();
    }

    protected void views(Workspace workspace) {
        ExampleObjectForView object = new ExampleObjectForView();
        ViewAxis axis = new TreeBrowserFrame(null, null);
        
        Content content = new RootObject(object);
        
        View view = new CollectionLeafNodeSpecification().createView(content, axis);
        view.setLocation(new Location(100, 20));
        view.setSize(view.getRequiredSize());
        workspace.addView(view);
        
        view = new CollectionLeafNodeSpecification().createView(content, axis);
        view.setLocation(new Location(100, 60));
        view.setSize(view.getRequiredSize());
        workspace.addView(view);
        view.getState().setObjectIdentified();

        view = new CollectionLeafNodeSpecification().createView(content, axis);
        view.setLocation(new Location(100, 100));
        view.setSize(view.getRequiredSize());
        workspace.addView(view);
        view.getState().setCanDrop();
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