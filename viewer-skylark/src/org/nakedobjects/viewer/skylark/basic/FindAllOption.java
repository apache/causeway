package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.MenuOption;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.util.ViewFactory;


public class FindAllOption extends MenuOption {
    public FindAllOption() {
        super("Find Matching Objects");
    }

    public void execute(Workspace workspace, View view, Location at) {
        NakedObjectManager objectManager = NakedObjectContext.getDefaultContext().getObjectManager();
        NakedObject pattern = ((ObjectContent) view.getContent()).getObject();
        NakedCollection instances = objectManager.findInstances(pattern);

        NakedObject object = (instances.size() == 1) ? (NakedObject) instances.elements().nextElement() : instances;

        View newView = ViewFactory.getViewFactory().createOpenRootView(object);

        newView.setLocation(at);

        workspace.addView(newView);
    }

    public String getDescription(View view) {
        return "Find all the matching objects";
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
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