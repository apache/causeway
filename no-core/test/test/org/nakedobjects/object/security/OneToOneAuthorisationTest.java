package test.org.nakedobjects.object.security;

import org.nakedobjects.object.repository.NakedObjectsClient;
import org.nakedobjects.object.security.NullSession;
import org.nakedobjects.object.security.OneToOneAuthorisation;

import junit.framework.TestCase;
import test.org.nakedobjects.object.reflect.DummyOneToOnePeer;


public class OneToOneAuthorisationTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(OneToOneAuthorisationTest.class);

    }

    private MockAuthorisationManager manager;
    private OneToOneAuthorisation oneToOne;

    protected void setUp() throws Exception {
        new NakedObjectsClient();

        DummyOneToOnePeer peer = new DummyOneToOnePeer();
        manager = new MockAuthorisationManager();
        oneToOne = new OneToOneAuthorisation(peer, manager);
    }

    public void testGetHint() {

        manager.setupUsable(true);
        manager.setupVisible(true);

        assertTrue(oneToOne.isAuthorised(new NullSession()));
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
