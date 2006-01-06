package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.utility.NakedObjectConfiguration;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.example.TestViews;


public class CanvasExample extends TestViews {

    public static void main(String[] args) {
        new CanvasExample();

    }
    
    protected void configure(NakedObjectConfiguration configuration) {
        configuration.add("nakedobjects.viewer.skylark.ascent-adjust", "true");
    }

    protected void views(Workspace workspace) {
        // AbstractView.debug = true;

        View view = new TestCanvasView();
        view.setLocation(new Location(50, 60));
        view.setSize(new Size(216, 300));
        workspace.addView(view);

        view = new TestCanvasView();
        view.setLocation(new Location(300, 60));
        view.setSize(new Size(216, 300));
        workspace.addView(view);

        view = new TestCanvasView2();
        view.setLocation(new Location(570, 60));
        view.setSize(new Size(50, 70));
        workspace.addView(view);

        view = new TestCanvasView2();
        view.setLocation(new Location(570, 160));
        view.setSize(new Size(8, 5));
        workspace.addView(view);
    }

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