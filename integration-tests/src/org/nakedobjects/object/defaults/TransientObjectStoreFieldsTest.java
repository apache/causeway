package org.nakedobjects.object.defaults;


import org.nakedobjects.object.NakedObjectStoreFieldsTestCase;
import org.nakedobjects.object.ObjectPerstsistenceException;
import org.nakedobjects.utility.configuration.ComponentException;
import org.nakedobjects.utility.configuration.ConfigurationException;

import test.org.nakedobjects.object.repository.object.NakedObjectStore;
import test.org.nakedobjects.object.repository.object.ObjectStoreException;

import junit.framework.TestSuite;


public class TransientObjectStoreFieldsTest extends NakedObjectStoreFieldsTestCase {

   public TransientObjectStoreFieldsTest(String name) {
      super(name);
   }

   public static void main(String[] args) {
      junit.textui.TestRunner.run(new TestSuite(TransientObjectStoreFieldsTest.class));
   }

	public NakedObjectStore installObjectStore() throws ObjectPerstsistenceException {
      return new TransientObjectStore();
   }
	
	protected void restartObjectStore() throws ObjectPerstsistenceException, Exception, ConfigurationException, ComponentException {
	    // override so the store is not restarted
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