package org.nakedobjects.object;

import org.nakedobjects.utility.DebugInfo;

public class NakedObjectContext implements DebugInfo {
    private static NakedObjectContext defaultContext;

    private NakedObjectManager objectManager;

    public NakedObjectContext(NakedObjectManager objectManager) {
        this.objectManager = objectManager;
        defaultContext = this;
    }
 
    public NakedObjectManager getObjectManager() {
        return objectManager;
    }
    
    public void makePersistent(NakedObject object) {
        objectManager.makePersistent(object);
    }

    public static NakedObjectContext getDefaultContext() {
        return defaultContext;
    }

    public String getDebugData() {
        StringBuffer sb = new StringBuffer();

 		sb.append("Other\n");
		sb.append("  Object manager: ");
		sb.append(objectManager);
		sb.append("\n\n");

		sb.append("Defaults\n");
		sb.append("  context: ");
		sb.append(getDefaultContext());
		sb.append("\n\n");

		sb.append("\n\n");
		
		return sb.toString();
    }

    public String getDebugTitle() {
        return "Naked Object Context: " + this;
    }
	
    public NakedObject createInstance(Class cls) {
        return createInstance(cls.getName());
    }

    /**
     * A utility method for creating new objects in the context of the system -
     * that is, it is added to the pool of objects the enterprise system
     * contains.
     */
    public NakedObject createInstance(String className) {
        return getObjectManager().createInstance(className);
    }



}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

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