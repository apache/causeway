package org.nakedobjects.persistence.cache;

import org.nakedobjects.object.NakedObjectStore;
import org.nakedobjects.object.NakedObjectStoreAdvancedTestCase;
import org.nakedobjects.object.ObjectStoreException;

import java.io.File;


public class CacheObjectStoreAdvancedTest extends NakedObjectStoreAdvancedTestCase {
    String DIR = "tmp/test";

    public CacheObjectStoreAdvancedTest(String name) {
		super(name);
	}
	
	public NakedObjectStore installObjectStore() throws ObjectStoreException {
        return new CacheObjectStore(DIR);
	}
	
	protected void setUp() throws Exception {
        String dir[] = new File(DIR).list();
        for (int i = 0; dir != null && i < dir.length; i++) {
            new File(DIR, dir[i]).delete();
        }
       
        super.setUp();
    }
}

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