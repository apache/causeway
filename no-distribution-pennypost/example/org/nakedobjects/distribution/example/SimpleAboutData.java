package org.nakedobjects.distribution.example;

import org.nakedobjects.distribution.AboutData;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.Permission;
import org.nakedobjects.object.control.defaults.Allow;
import org.nakedobjects.object.control.defaults.Veto;

public class SimpleAboutData implements AboutData {
    private SimpleAbout serializableAbout;

    public SimpleAboutData(About about) {
       serializableAbout = new SimpleAbout(about);
    }
    
    public About recreateAbout() {
        return serializableAbout;
    }

}

class SimpleAbout implements About {
    private String name;
    private String description;
    private String canAccessReason;
    private boolean canAccessAllowed;
    private String canUseReason;
    private boolean canUseAllowed;

    
    public SimpleAbout(About about) {
        Permission canAccess = about.canAccess();
        canAccessAllowed = canAccess.isAllowed();
        canAccessReason = canAccess.getReason();
        Permission canUse = about.canUse();
        canUseAllowed = canUse.isAllowed();
        canUseReason = canUse.getReason();
        name = about.getName();
        description = about.getDescription();
    }

    public Permission canAccess() {
      if(canAccessAllowed) {
          return new Allow(canAccessReason);
      } else {
          return new Veto(canAccessReason);
      }
    }

    public Permission canUse() {
        if(canUseAllowed) {
            return new Allow(canUseReason);
        } else {
            return new Veto(canUseReason);
        }
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