package org.nakedobjects.viewer.skylark.example;

import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.Drag;
import org.nakedobjects.viewer.skylark.DragStart;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.ViewSpecification;

import org.apache.log4j.Logger;

public class TestObjectViewWithDragging extends TestObjectView {

    private static final Logger LOG = Logger.getLogger(TestObjectViewWithDragging.class);
    
    public TestObjectViewWithDragging(Content content, ViewSpecification specification, ViewAxis axis, int width, int height, String label) {
        super(content, specification, axis, width, height, label);
    }

    public Drag dragStart(DragStart drag) {
        LOG.debug("drag start " + drag.getLocation());
        
        return super.dragStart(drag);
    }
    

    public void mouseMoved(Location location) {
        LOG.debug("mouse moved " + location);
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