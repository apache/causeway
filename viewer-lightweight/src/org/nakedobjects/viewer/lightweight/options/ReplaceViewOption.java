/*
    Naked Objects - a framework that exposes behaviourally complete
    business objects directly to the user.
    Copyright (C) 2000 - 2003  Naked Objects Group Ltd

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

package org.nakedobjects.viewer.lightweight.options;

import org.apache.log4j.Logger;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.viewer.lightweight.CompositeView;
import org.nakedobjects.viewer.lightweight.InternalView;
import org.nakedobjects.viewer.lightweight.Location;
import org.nakedobjects.viewer.lightweight.ObjectView;
import org.nakedobjects.viewer.lightweight.RootView;
import org.nakedobjects.viewer.lightweight.View;
import org.nakedobjects.viewer.lightweight.Workspace;


public class ReplaceViewOption extends AbstractObjectOption {
    private static final Logger LOG = Logger.getLogger(ReplaceViewOption.class);
    private ObjectView prototype;

    public ReplaceViewOption(ObjectView prototype) {
        super("View as " + prototype.getName());
        this.prototype = prototype;
    }

    public Permission disabled(Workspace workspace, ObjectView view, Location location) {
        return new Allow("Replace this " + view.getName().toLowerCase() + " view with a " +
            prototype.getName().toLowerCase() + " view");
    }

    public void execute(Workspace workspace, ObjectView view, Location at) {
        try {
        	NakedObject object = view.getObject();
        	
            if(view instanceof RootView) {
            	workspace.removeView(view);
            	RootView replacement = (RootView) prototype.makeView(object, null);
            	replacement.setLocation(view.getAbsoluteLocation());
				workspace.limitBounds(replacement);
				workspace.addRootView(replacement);
				
            } else if(view instanceof InternalView) {
            	Field field = ((InternalView) view).getFieldOf();
				View replacement = (View) prototype.makeView(object, field);
				LOG.debug("replacement view " + replacement);
            	((CompositeView)view.getParent()).replaceView((InternalView)view, (InternalView) replacement);
            	workspace.limitBounds(view);
            	
            }
        } catch (CloneNotSupportedException e) {
            LOG.error("failed to create view", e);
        }
    }
    
	public String toString() {
		return super.toString() + " [prototype=" + prototype.getName() + "]";
	}

}
