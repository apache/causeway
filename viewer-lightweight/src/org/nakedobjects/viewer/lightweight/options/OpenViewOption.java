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
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.viewer.lightweight.Location;
import org.nakedobjects.viewer.lightweight.ObjectView;
import org.nakedobjects.viewer.lightweight.RootView;
import org.nakedobjects.viewer.lightweight.Workspace;


public class OpenViewOption extends AbstractObjectOption {
    private static final Logger LOG = Logger.getLogger(OpenViewOption.class);
    private RootView prototype;

    public OpenViewOption(RootView prototype) {
        super("Open " + prototype.getName());
        this.prototype = prototype;
    }

    public Permission disabled(Workspace frame, ObjectView view, Location location) {
        return new Allow("Open '" + view.getObject().title() + "' in a " + prototype.getName().toLowerCase() + " window");
    }

    public void execute(Workspace frame, ObjectView view, Location at) {
        try {
            RootView newView = (RootView) prototype.makeView(view.getObject(), null);
            LOG.debug("open view " + newView);
            newView.setLocation(at);
            frame.addRootView(newView);
        } catch (CloneNotSupportedException e) {
            LOG.error("failed to create view", e);
        }
    }
    
    public String toString() {
		return super.toString() + " [prototype=" + prototype.getName() + "]";
	}
}
