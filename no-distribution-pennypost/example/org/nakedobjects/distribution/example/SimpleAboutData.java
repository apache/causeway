package org.nakedobjects.distribution.example;

import org.nakedobjects.distribution.HintData;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.utility.NotImplementedException;

public class SimpleAboutData implements HintData {
    private SimpleAbout serializableAbout;

    public SimpleAboutData(Hint about) {
       serializableAbout = new SimpleAbout(about);
    }
    
    public Hint recreateHint() {
        return serializableAbout;
    }

}

class SimpleAbout implements Hint {
    private String name;
    private String description;
    private String canAccessReason;
    private boolean canAccessAllowed;
    private String canUseReason;
    private boolean canUseAllowed;

    
    public SimpleAbout(Hint about) {
        Consent canAccess = about.canAccess();
        canAccessAllowed = canAccess.isAllowed();
        canAccessReason = canAccess.getReason();
        Consent canUse = about.canUse();
        canUseAllowed = canUse.isAllowed();
        canUseReason = canUse.getReason();
        name = about.getName();
        description = about.getDescription();
    }

    public Consent canAccess() {
      if(canAccessAllowed) {
          return new Allow(canAccessReason);
      } else {
          return new Veto(canAccessReason);
      }
    }

    public Consent canUse() {
        if(canUseAllowed) {
            return new Allow(canUseReason);
        } else {
            return new Veto(canUseReason);
        }
    }
    
    public Consent isValid() {
        throw new NotImplementedException();
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String debug() {
        return null;
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