package org.nakedobjects.object;

import java.util.Vector;

import junit.framework.Assert;

public class MockLoadedObjects extends LoadedObjects {
    private Vector actions = new Vector();
    private boolean isLoaded;
    private Vector loadedObjects;
    private Object oid;

    public NakedObject getLoadedObject(Object oid) {
        if(loadedObjects == null) {
            Assert.fail("No loaded objects");
        }
        NakedObject loadedObject = (NakedObject) loadedObjects.elementAt(0);
        loadedObjects.remove(0);
        if(loadedObject != null) {
            Assert.assertEquals("Request", oid, loadedObject.getOid());
        }
        return loadedObject;
    }

    public boolean isLoaded(Object oid) {
        return isLoaded;
    }

    public void loaded(NakedObject object) throws ResolveException {
        actions.addElement("loaded " + object);
    }

    public void unloaded(NakedObject object) {
        actions.addElement("unloaded " + object);
   }
    
    public void setupLoadedObjects(NakedObject[] loadedObjects) {
        this.loadedObjects = new Vector();
        for (int i = 0; i < loadedObjects.length; i++) {
            this.loadedObjects.addElement(loadedObjects[i]);
        }
    }
    
    public void setupIsLoaded(boolean flag) {
        this.isLoaded = flag;
    }

    public void assertAction(int i, String string) {
        Assert.assertTrue(actions.size() > i);
        Assert.assertEquals("action " + i, actions.elementAt(i), string);
    }
}


/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2004 Naked Objects Group Ltd
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