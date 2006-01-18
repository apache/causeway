package org.nakedobjects.viewer.skylark.tree;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.Action.Type;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.viewer.skylark.Location;
import org.nakedobjects.viewer.skylark.UserActionSet;
import org.nakedobjects.viewer.skylark.UserAction;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.Workspace;


public class TreeDisplayRules {
    private static boolean showCollectionsOnly = false;

    private TreeDisplayRules() {}

    public static void menuOptions(UserActionSet options) {
        // TODO fix and remove  following line
        if(true) return;
        
        UserAction option = new UserAction() {
            public void execute(Workspace workspace, View view, Location at) {
                showCollectionsOnly = !showCollectionsOnly;
            }

            public String getName(View view) {
                return showCollectionsOnly ? "Show collections only" : "Show all references";
            }

            public Consent disabled(View view) {
                return Allow.DEFAULT;
            }

            public String getDescription(View view) {
                return "This option makes the system only show collections within the trees, and not single elements";
            }
            
            public Type getType() {
                return USER;
            }
        };
        options.add(option);
    }
    
    public static boolean isCollectionsOnly() {
        return showCollectionsOnly;
    }

    public static boolean canDisplay(Naked object) {
		boolean lookupView = object != null && object.getSpecification().isLookup();
		boolean showNonCollections = !TreeDisplayRules.isCollectionsOnly();
        boolean objectView = object instanceof NakedObject && showNonCollections ;
        boolean collectionView = object instanceof NakedCollection;
        return (objectView || collectionView) && !lookupView;
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