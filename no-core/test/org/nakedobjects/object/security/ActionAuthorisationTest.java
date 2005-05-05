package org.nakedobjects.object.security;

import org.nakedobjects.NakedObjectsClient;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.control.MockHint;
import org.nakedobjects.object.reflect.DummyNakedObject;
import org.nakedobjects.object.reflect.MemberIdentifier;

import junit.framework.TestCase;

public class ActionAuthorisationTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ActionAuthorisationTest.class);
    }

    public void testAlwaysHashint() {
        ActionAuthorisation oneToOne = new ActionAuthorisation(null, null);
        assertTrue(oneToOne.hasHint());
    }
    
    public void testGetHint() {
        new NakedObjectsClient();
        MockActionPeer peer = new MockActionPeer();
        MockAuthorisationManager manager = new MockAuthorisationManager();
        ActionAuthorisation action = new ActionAuthorisation(peer, manager);
        
        NakedObject object = new DummyNakedObject();
        MemberIdentifier identifier = new MemberIdentifier("cls", "action");
        
        peer.expect("getHint " + identifier + " " + object);
        Hint hint = new MockHint();
        peer.setupHint(hint);
        manager.setupUsable(true);
        manager.setupVisible(true);

        Hint returnedHint = action.getHint(identifier, object, new Naked[0]);
        assertEquals(hint, returnedHint);
        
        peer.verify();
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
