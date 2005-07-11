package org.nakedobjects.object.security;

import org.nakedobjects.NakedObjectsClient;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.control.MockHint;
import org.nakedobjects.object.control.NoOpVeto;

import junit.framework.TestCase;

public class AuthorisationHintTest extends TestCase {

    private MockHint originalHint;
    private MockAuthorisationManager manager;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(AuthorisationHintTest.class);
    }

    protected void setUp() throws Exception {
        new NakedObjectsClient();
        originalHint = new MockHint();
        manager = new MockAuthorisationManager();
    }
    
    public void testVisibleAndUsable() {
        manager.setupVisible(true);
        manager.setupUsable(true);
        Hint hint = AuthorisationHint.hint(null, originalHint, manager);
        assertEquals("original hint passed through", originalHint, hint);
    }
    
    public void testNotVisible() {
        manager.setupVisible(false);
        Hint hint = AuthorisationHint.hint(null, originalHint, manager);
        assertEquals("Cannot be seen for security reasons", hint.canAccess().getReason());
    }
    
    public void testNotUsable() {
        manager.setupVisible(true);
        manager.setupUsable(false);
        Hint hint = AuthorisationHint.hint(null, originalHint, manager);
        
        Consent canAccess = new NoOpVeto();
        originalHint.setupCanAccess(canAccess );
        originalHint.setupName("name");
        originalHint.setupDescription("desc");
        
        assertEquals(canAccess, hint.canAccess());
        assertEquals("Cannot use for security reasons", hint.canUse().getReason());
        assertEquals("name", hint.getName());
        assertEquals("desc", hint.getDescription());
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