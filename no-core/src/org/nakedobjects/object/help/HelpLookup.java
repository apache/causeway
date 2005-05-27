package org.nakedobjects.object.help;

import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.DefaultHint;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.MemberIdentifier;

public class HelpLookup {
    private final HelpManager helpManager;

    public HelpLookup(HelpManager helpManager) {
        this.helpManager = helpManager;
    }
    
     Hint createMergedHint(final MemberIdentifier identifier, final Hint hint) {
        final String help = helpManager == null ? "" : helpManager.help(identifier);
        Hint mergedHint = new DefaultHint() {
            
            public Consent isValid() {
                return hint.isValid();
            }

            public Consent canAccess() {
                return hint.canAccess();
            }

            public Consent canUse() {
                return hint.canUse();
            }

            public String getDescription() {
                if (help != null && help.length() > 0) {
                    return help;
                }

                String description = hint.getDescription();
                if (description == null || description.length() == 0) {
                    return "No help available";
                } else {
                    return description;
                }
            }

            public String getName() {
                String name = hint.getName();
                if(name == null || name.length() == 0) {
                    return null;
                } else {
                    return name;
                }
            }
        };
        return mergedHint;
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