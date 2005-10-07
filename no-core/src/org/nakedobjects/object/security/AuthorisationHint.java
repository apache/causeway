package org.nakedobjects.object.security;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.DefaultHint;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.object.reflect.MemberIdentifier;

class AuthorisationHint {
    static Hint hint(MemberIdentifier identifier, final Hint originalHint, AuthorisationManager authorisationManager) {
        Session session = NakedObjects.getCurrentSession();
        boolean isUsable = authorisationManager.isUsable(session, identifier);
        boolean isVisible = authorisationManager.isVisible(session, identifier);
        if (!isVisible) {
            return new DefaultHint() {
                private static final long serialVersionUID = 219219947009299041L;

                public Consent canAccess() {
                    return new Veto("Cannot be seen for security reasons");
                }
            };
        }
        
        if (!isUsable) {
            return new DefaultHint() {
                private static final long serialVersionUID = 4381086950339064075L;

                public Consent canAccess() {
                    return originalHint.canAccess();
                }

                public Consent canUse() {
                    return new Veto("Cannot use for security reasons");
                }

                public String getDescription() {
                    return originalHint.getDescription();
                }

                public String getName() {
                    return originalHint.getName();
                }
            };
        }
        return originalHint;
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